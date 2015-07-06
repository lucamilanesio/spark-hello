import java.util.Collections

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.{Credentials, UserGroupInformation}
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse
import org.apache.hadoop.yarn.api.records.{ContainerLaunchContext, FinalApplicationStatus, Priority, Resource}
import org.apache.hadoop.yarn.client.api.{AMRMClient, NMClient}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records

object ApplicationMaster extends App {

  val command: String = args(0)
  val n: Int = Integer.valueOf(args(1))
  val conf: Configuration = new YarnConfiguration

  println(s"ClassPath: ${System.getProperty("java.class.path")}")
  val hadoopToken = System.getenv("HADOOP_TOKEN_FILE_LOCATION")
  println(s"HADOOP_TOKEN_FILE_LOCATION=${hadoopToken}")

  val rmClient: AMRMClient[AMRMClient.ContainerRequest] = AMRMClient.createAMRMClient.asInstanceOf[AMRMClient[AMRMClient.ContainerRequest]]
  rmClient.init(conf)
  rmClient.start
  val nmClient: NMClient = NMClient.createNMClient
  nmClient.init(conf)
  nmClient.start
  System.out.println("registerApplicationMaster 0")
  rmClient.registerApplicationMaster("", 0, "")
  System.out.println("registerApplicationMaster 1")
  val priority: Priority = Records.newRecord(classOf[Priority])
  priority.setPriority(0)
  val capability: Resource = Records.newRecord(classOf[Resource])
  capability.setMemory(128)
  capability.setVirtualCores(1)

  var i: Int = 0
  while (i < n) {
    {
      val containerAsk: AMRMClient.ContainerRequest = new AMRMClient.ContainerRequest(capability, null, null, priority)
      System.out.println("Making res-req " + i)
      rmClient.addContainerRequest(containerAsk)
    }
    ({
      i += 1;
      i
    })
  }

  var responseId: Int = 0
  var completedContainers: Int = 0
  while (completedContainers < n) {
    val response: AllocateResponse = rmClient.allocate(({
      responseId += 1;
      responseId - 1
    }))
    import scala.collection.JavaConversions._
    for (container <- response.getAllocatedContainers) {
      val ctx: ContainerLaunchContext = Records.newRecord(classOf[ContainerLaunchContext])
      val ourJar = getOurJarsFromClasspath
      println(s"Our JAR in classpath is: $ourJar")
      val cmdLine = s"$$JAVA_HOME/bin/java -cp $ourJar SimpleApp 1> ${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stdout 2> ${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stderr"
      ctx.setCommands(Seq(cmdLine))
      System.out.println("Launching container " + container.getId)
      nmClient.startContainer(container, ctx)
    }
    import scala.collection.JavaConversions._
    for (status <- response.getCompletedContainersStatuses) {
      completedContainers += 1
      System.out.println("Completed container " + status.getContainerId)
    }
    Thread.sleep(100)
  }
  rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "")

  def getOurJarsFromClasspath = {
    val fullClassPath = System.getProperty("java.class.path").split(":")
    (fullClassPath.filter(_.contains("simpleapp.jar")) ++ fullClassPath.filterNot(_.contains("simpleapp.jar"))).mkString(":")
  }
}


