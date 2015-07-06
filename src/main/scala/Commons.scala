import java.io.{PrintWriter, BufferedOutputStream, File}
import java.nio.ByteBuffer
import java.util.Map

import org.apache.hadoop.fs.{FileSystem, FileStatus, Path}
import org.apache.hadoop.io.DataOutputBuffer
import org.apache.hadoop.security.{Credentials, UserGroupInformation}
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment
import org.apache.hadoop.yarn.api.records.{LocalResourceVisibility, LocalResourceType, LocalResource}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.{Apps, ConverterUtils}
import org.apache.hadoop.conf.Configuration

object AppContainerSetup {
  
  def setupContainerJar(jarPath: Path, appMasterJar: LocalResource)(implicit conf: Configuration) {
    val jarStat: FileStatus = FileSystem.get(conf).getFileStatus(jarPath)
    appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath))
    appMasterJar.setSize(jarStat.getLen)
    appMasterJar.setTimestamp(jarStat.getModificationTime)
    appMasterJar.setType(LocalResourceType.FILE)
    appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC)
  }

  def setupContainerEnv(appMasterEnv: Map[String, String])(implicit conf: Configuration) {
    Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name, Environment.PWD.$ + File.separator + "*")
    for (c <- conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH:_*)) {
      Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name, c.trim)
    }
  }

  def setupDelegationToken(conf: Configuration, fs: FileSystem): Option[ByteBuffer] = {
    if(UserGroupInformation.isSecurityEnabled()) {
      println("Security is enabled")
      val credentials = new Credentials()
      val tokenRenewer = conf.get(YarnConfiguration.RM_PRINCIPAL)
      require(tokenRenewer != null)
      val tokens = fs.addDelegationTokens(tokenRenewer, credentials)
      require(tokens != null)

      println(s"${fs.getUri} has delegation tokens: ${tokens.mkString(",")}")
      val dob = new DataOutputBuffer()
      credentials.writeTokenStorageToStream(dob)
      Some(ByteBuffer.wrap(dob.getData, 0, dob.getLength))
    } else {
      None
    }
  }
}

class Log(val logger: Class[_], conf: Configuration) {
  def open = {
    val fs = FileSystem.get(conf)
    val fileName = logger.getSimpleName + "_" + System.currentTimeMillis
    new PrintWriter(fs.create(new Path(fileName)))
  }

  def info(msg: String) {
    val log = open
    val formatted = s"[${logger.getSimpleName}] $msg"
    println(formatted)
    log.println(formatted)
    log.flush()
    log.close
  }
}
