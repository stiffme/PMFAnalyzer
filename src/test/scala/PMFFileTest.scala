import akka.actor.{Props, ActorSystem}
import com.esipeng.MainApp
import com.esipeng.pmf._
import com.typesafe.config.ConfigFactory
import org.junit._
import org.junit.Assert._
import scala.io.Source

@Test
class PMFFileTest {

  var actorSystem:ActorSystem = ActorSystem("testSystem",ConfigFactory.load("application"))

  @Test
  def testCmdLine() = {
    MainApp.main(Array[String]("-h"))
  }


}


