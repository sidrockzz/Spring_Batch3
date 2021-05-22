package com.linkedin.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Step storePackageStep(){
		return this.stepBuilderFactory
				.get("storePackageStep")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
						System.out.println("Storing the package while the customer address is located");
						return RepeatStatus.FINISHED;
					}
				}).build();
	}

	@Bean
	public Step givePackageToCustomerStep(){
		return this.stepBuilderFactory
				.get("givePackageToCustomerStep")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
						System.out.println("Given Package to the customer.");
						return RepeatStatus.FINISHED;
					}
				}).build();
	}

	@Bean
	public Step driveToAddressStep(){

		boolean GOT_LOST = false;
		return this.stepBuilderFactory
				.get("driveToAddressStep")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
						if(GOT_LOST){
							throw new RuntimeException("Got lost driving to the address");
						}
						System.out.println("Successfully arrived at the address.");
						return RepeatStatus.FINISHED;
					}
				}).build();
	}

	@Bean
	public Step packageItemStep() {
		return this.stepBuilderFactory
				.get("packageItemStep")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
						String item= chunkContext.getStepContext().getJobParameters().get("item").toString();
						//String date= chunkContext.getStepContext().getJobParameters().get("run.date").toString();
						Date date = Calendar.getInstance().getTime();
						DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
						String strDate = dateFormat.format(date);
						System.out.println("The "+item+ " has been package on "+strDate+".");
						return RepeatStatus.FINISHED;
					}
				})
                .build();
	}

	@Bean
	public Job deliverPackageJob(){
		return this.jobBuilderFactory
				.get("deliveryPackageJob")
				.start(packageItemStep())
				.next(driveToAddressStep())
				.on("FAILED").
						to(storePackageStep())
				.from(driveToAddressStep())
				.on("*")
				.to(givePackageToCustomerStep())
				.end()
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}

}
