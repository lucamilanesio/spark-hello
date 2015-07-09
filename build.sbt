name := "Simple Project"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"


//libraryDependencies ++= Seq(
//  "org.apache.spark" %% "spark-core" % "1.3.0" % "provided",
//  "org.apache.hbase" % "hbase-client" % "1.0.0",
//  "org.apache.hbase" % "hbase-common" % "1.0.0",
//  "org.apache.hadoop" % "hadoop-common" % "2.2.0" % "provided",
//  "com.typesafe.akka" %% "akka-actor" % "2.2.3" % "provided",
//  "com.typesafe.akka" %% "akka-cluster" % "2.2.3",
//  "org.apache.hadoop" % "hadoop-yarn-client" % "2.2.0" % "provided")
//  .map(
//    _ exclude("log4j", "log4j")
//      exclude("com.typesafe.akka", "akka-actor_2.10")
//      exclude("com.typesafe.akka", "akka-remote_2.10")
//      exclude("io.netty", "netty-all"))

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.3.0" % "provided",
  "org.apache.hbase" % "hbase-client" % "1.0.0",
  "org.apache.hbase" % "hbase-common" % "1.0.0",
  "org.apache.hadoop" % "hadoop-common" % "2.2.0" % "provided",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4" ,
  "com.typesafe.akka" %% "akka-cluster" % "2.3.4",
  "org.apache.hadoop" % "hadoop-yarn-client" % "2.2.0" % "provided")
  .map(
    _ exclude("log4j", "log4j")

//      exclude("com.typesafe.akka", "akka-actor_2.10")
//      exclude("com.typesafe.akka", "akka-remote_2.10")
//      exclude("io.netty", "netty-all")
  )

assemblyJarName in assembly := "simple-project-shaded.jar"

//assemblyMergeStrategy in assembly := {
//  case PathList("reference.conf") => MergeStrategy.discard
//  case x =>
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}
