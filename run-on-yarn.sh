JAR=simple-project-shaded.jar
CLIENT_CLASS=Client
hadoop fs -rm /user/root/$JAR
hadoop fs -copyFromLocal $JAR /user/root/.
hadoop jar $JAR $CLIENT_CLASS /bin/date 1 hdfs://localhost:8020/user/root/$JAR
