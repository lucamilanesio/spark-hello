
import java.net.{URL, URI, URLClassLoader}

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.cluster.Cluster
import akka.actor.Terminated
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HBaseAdmin, HConnectionManager}
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConversions._

//import org.apache.spark.{SparkConf, SparkContext}

object SimpleApp {
  val conf = HBaseConfiguration.create()

  val LOG = new Log(this.getClass, conf)
  def main(args: Array[String]) {


//    LOG.info("Connecting to HBase ...")
//    val conn = HConnectionManager.createConnection(conf)
//    val admin = new HBaseAdmin(conf)
//    LOG.info(s"Connected: conn=$conn admin=$admin")

    val system = ActorSystem("Hello", ConfigFactory.load("simple-app"))
    val cluster = Cluster.get(system)
    val a = system.actorOf(Props[HelloWorld], "helloWorld")
    system.actorOf(Props(classOf[Terminator], a), "terminator")

    a ! "Hello"

    val currentClassPath: Seq[URL] = System.getProperty("java.class.path").split(":")
      .filterNot(_.isEmpty)
      .map((x: String) => new URI("file://" + x).toURL)

    LOG.info(currentClassPath.map(_.toString).mkString("\n"))

    val currentClassPathArray: Array[URL] = currentClassPath.toArray

    val origClassLoader = new URLClassLoader(currentClassPathArray, null)


    val thisClassLoader = this.getClass.getClassLoader
    LOG.info(s"OurClassLoader is: ${thisClassLoader.toString}")
    LOG.info(s"OrigClassLoader is: ${origClassLoader}")

    Thread.currentThread().setContextClassLoader(origClassLoader)

    LOG.info("Creating a new SparkContext ...")
//    val sc = new SparkContext(new SparkConf().setAppName("Simple Application"))
    val sparkContextClass = origClassLoader.loadClass("org.apache.spark.SparkContext")
    val sparkConfClass = origClassLoader.loadClass("org.apache.spark.SparkConf")
    val sparkConf: Object = sparkConfClass.newInstance().asInstanceOf[Object]
    val setAppNameMethod = sparkConfClass.getMethod("setAppName", classOf[java.lang.String])
    setAppNameMethod.invoke(sparkConf, new java.lang.String("Simple Application"))
    LOG.info(s"Created sparkConf: $sparkConf")

    val sparkContextConstructor = sparkContextClass.getConstructor(sparkConfClass)
    val sc = sparkContextConstructor.newInstance(sparkConf)
    LOG.info(s"Created: $sc")
  }

  class HelloWorld extends Actor with ActorLogging {
    def receive = {
      case _ => {
        log.info("Hello world")
      }
    }
  }

  class Terminator(ref: ActorRef) extends Actor with ActorLogging {
    context watch ref
    def receive = {
      case Terminated(_) =>
        log.info("{} has terminated, shutting down system", ref.path)
        context.system.awaitTermination()
    }
  }
}