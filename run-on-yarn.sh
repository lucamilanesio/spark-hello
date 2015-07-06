JAR=simple-project-shaded.jar
CLIENT_CLASS=Client
NAME_NODE=nameservice1
hadoop fs -rm /user/$USER/$JAR
hadoop fs -copyFromLocal target/scala-2.10/$JAR /user/$USER/.
hadoop jar target/scala-2.10/$JAR $CLIENT_CLASS /bin/date 1 hdfs://$NAME_NODE/user/$USER/$JAR
