import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.HashMap
import java.util.Map
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment
import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext
import org.apache.hadoop.yarn.api.records.LocalResource
import org.apache.hadoop.yarn.api.records.LocalResourceType
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility
import org.apache.hadoop.yarn.api.records.Resource
import org.apache.hadoop.yarn.api.records.YarnApplicationState
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.client.api.YarnClientApplication
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Apps
import org.apache.hadoop.yarn.util.ConverterUtils
import org.apache.hadoop.yarn.util.Records


object Client extends App {
    val c: Client = new Client
    c.run(args)
}

class Client {
  val conf: Configuration = new YarnConfiguration

  @throws(classOf[Exception])
  def run(args: Array[String]) {
    val command: String = args(0)
    val n: Int = Integer.valueOf(args(1))
    val jarPath: Path = new Path(args(2))
    val conf: YarnConfiguration = new YarnConfiguration
    val yarnClient: YarnClient = YarnClient.createYarnClient
    yarnClient.init(conf)
    yarnClient.start
    val app: YarnClientApplication = yarnClient.createApplication
    val amContainer: ContainerLaunchContext = Records.newRecord(classOf[ContainerLaunchContext])
    amContainer.setCommands(Collections.singletonList("$JAVA_HOME/bin/java" + " -Xmx256M" + " ApplicationMaster" + " " + command + " " + String.valueOf(n) + " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"))
    val appMasterJar: LocalResource = Records.newRecord(classOf[LocalResource])
    setupAppMasterJar(jarPath, appMasterJar)
    amContainer.setLocalResources(Collections.singletonMap("simpleapp.jar", appMasterJar))
    val appMasterEnv: Map[String, String] = new HashMap[String, String]
    setupAppMasterEnv(appMasterEnv)
    amContainer.setEnvironment(appMasterEnv)
    val capability: Resource = Records.newRecord(classOf[Resource])
    capability.setMemory(256)
    capability.setVirtualCores(1)
    val appContext: ApplicationSubmissionContext = app.getApplicationSubmissionContext
    appContext.setApplicationName("simple-yarn-app")
    appContext.setAMContainerSpec(amContainer)
    appContext.setResource(capability)
    appContext.setQueue("default")
    val appId: ApplicationId = appContext.getApplicationId
    System.out.println("Submitting application " + appId)
    yarnClient.submitApplication(appContext)
    var appReport: ApplicationReport = yarnClient.getApplicationReport(appId)
    var appState: YarnApplicationState = appReport.getYarnApplicationState
    while (appState != YarnApplicationState.FINISHED && appState != YarnApplicationState.KILLED && appState != YarnApplicationState.FAILED) {
      Thread.sleep(100)
      appReport = yarnClient.getApplicationReport(appId)
      appState = appReport.getYarnApplicationState
    }
    println("Application " + appId + " finished with" + " state " + appState + " at " + appReport.getFinishTime)
  }

  @throws(classOf[IOException])
  private def setupAppMasterJar(jarPath: Path, appMasterJar: LocalResource) {
    val jarStat: FileStatus = FileSystem.get(conf).getFileStatus(jarPath)
    appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath))
    appMasterJar.setSize(jarStat.getLen)
    appMasterJar.setTimestamp(jarStat.getModificationTime)
    appMasterJar.setType(LocalResourceType.FILE)
    appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC)
  }

  private def setupAppMasterEnv(appMasterEnv: Map[String, String]) {
    for (c <- conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH:_*)) {
      Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name, c.trim)
    }
    Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name, Environment.PWD.$ + File.separator + "*")
  }
}

