CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
mvn clean package -D maven.test.skip=true;
java -jar ./target/linkedin-batch-*-*-0.0.1-SNAPSHOT.jar "item=man1" "run.date(date)=$CURRENT_DATE" "lesson=$LESSON";
read;
