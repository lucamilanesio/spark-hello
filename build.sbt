name := "Simple Project"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"

//libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.0"

libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.0.0" % "provided"

libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.0.0" % "provided"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.2.0" % "provided"

//
//libraryDependencies += "org.apache.hbase" % "hbase-client" % "0.98.6-cdh5.3.2"
//
//libraryDependencies += "org.apache.hbase" % "hbase-common" % "0.98.6-cdh5.3.2"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4" exclude ("com.google.protobuf" , "protobuf-java")

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.3.4" exclude ("com.google.protobuf" , "protobuf-java")

assemblyJarName in assembly := "simple-project-shaded.jar"
