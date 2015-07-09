
## How to use?

```bash
sbt assembly
./run-on-yarn.sh
```

## How to check the job on Yarn

From command line it's possible to display the log:
```bash
yarn logs -applicationId <application ID>
```

From the Yarn UI, browse:
http://<yarn node manager host>:8088/


## How to kill the job

```
yarn application -kill <application ID>
```

## How to execute through spark-submit 

To execute the local driver:

```
spark-submit --master yarn-client --class SimpleApp --verbose --conf spark.driver.userClassPathFirst=true ./simple-project-shaded.jar
```

To execute the driver through YARN

```
spark-submit --master yarn-client --class SimpleApp --verbose --conf spark.driver.userClassPathFirst=true ./simple-project-shaded.jar
```

## How to include HBase configuration

HBase configuration is automatically taken from the Java classpath: just add /etc/hbase/conf as driver-class-path on Spark

```
spark-submit --master yarn-client --class SimpleApp --driver-class-path "/etc/hbase/conf" --verbose --conf spark.driver.userClassPathFirst=true ./simple-project-shaded.jar
```