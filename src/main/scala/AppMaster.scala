import java.util.{HashMap, Map, Collections}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.{Credentials, UserGroupInformation}
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.client.api.{AMRMClient, NMClient}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records
import AppContainerSetup._

object ApplicationMaster extends App {
  implicit val conf: Configuration = new YarnConfiguration
  val LOG = new Log(this.getClass, conf)

  val command: String = args(0)
  val n: Int = Integer.valueOf(args(1))
  val jarPath: Path = new Path(args(2))


  LOG.info(s"ClassPath: ${System.getProperty("java.class.path")}")
  val hadoopToken = System.getenv("HADOOP_TOKEN_FILE_LOCATION")
  LOG.info(s"HADOOP_TOKEN_FILE_LOCATION=${hadoopToken}")

  val rmClient: AMRMClient[AMRMClient.ContainerRequest] = AMRMClient.createAMRMClient.asInstanceOf[AMRMClient[AMRMClient.ContainerRequest]]
  rmClient.init(conf)
  rmClient.start
  val nmClient: NMClient = NMClient.createNMClient
  nmClient.init(conf)
  nmClient.start
  LOG.info("registerApplicationMaster 0")
  rmClient.registerApplicationMaster("", 0, "")
  LOG.info("registerApplicationMaster 1")
  val priority: Priority = Records.newRecord(classOf[Priority])
  priority.setPriority(0)
  val capability: Resource = Records.newRecord(classOf[Resource])
  capability.setMemory(128)
  capability.setVirtualCores(1)

  var i: Int = 0
  while (i < n) {
    {
      val containerAsk: AMRMClient.ContainerRequest = new AMRMClient.ContainerRequest(capability, null, null, priority)
      LOG.info("Making res-req " + i)
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
      val appMasterJar: LocalResource = Records.newRecord(classOf[LocalResource])
      setupContainerJar(jarPath, appMasterJar)
      ctx.setLocalResources(Collections.singletonMap("simpleapp.jar", appMasterJar))
      val appMasterEnv: Map[String, String] = new HashMap[String, String]
      setupContainerEnv(appMasterEnv)
      ctx.setEnvironment(appMasterEnv)
      implicit val fs = FileSystem.get(conf)
      setupDelegationToken(conf, fs).foreach(ctx.setTokens)

      val cmdLine = s"$$JAVA_HOME/bin/java SimpleApp 1> ${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stdout 2> ${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stderr"
      LOG.info(s"executing $cmdLine")
      ctx.setCommands(Seq(cmdLine))

      LOG.info("Launching container " + container.getId)
      nmClient.startContainer(container, ctx)
    }
    import scala.collection.JavaConversions._
    for (status <- response.getCompletedContainersStatuses) {
      completedContainers += 1
      LOG.info("Completed container " + status.getContainerId)
    }
  }
  rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "")
}


