
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

//import org.apache.spark.{SparkConf, SparkContext}

object SimpleApp {
  val conf = HBaseConfiguration.create()

  val LOG = new Log(this.getClass, conf)
  def main(args: Array[String]) {
//    LOG.info("Creating a new SparkContext ...")
//    val sc = new SparkContext(new SparkConf().setAppName("Simple Application"))
//    LOG.info(s"Created: $sc")

//    LOG.info("Connecting to HBase ...")
//    val conn = HConnectionManager.createConnection(conf)
//    val admin = new HBaseAdmin(conf)
//    LOG.info(s"Connected: conn=$conn admin=$admin")

    val system = ActorSystem("Hello", ConfigFactory.load("simple-app"))
    val cluster = Cluster.get(system)
    val a = system.actorOf(Props[HelloWorld], "helloWorld")
    system.actorOf(Props(classOf[Terminator], a), "terminator")

    a ! "Hello"
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