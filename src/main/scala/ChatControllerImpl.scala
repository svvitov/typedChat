import akka.actor.typed.ActorSystem
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import javafx.application.Platform
import javafx.event.ActionEvent

class ChatControllerImpl extends ChatController{

  var login: String = _ // var, потому что сюда передается login из окна входа
  var system: ActorSystem[String] = _
  override def onSendMessageButton(event: ActionEvent): Unit = {
    val nickname = login // создаю val на основе var login, чтобы не отправлять var
    val message = messageInput.getText.trim

    system ! message


    messageInput.clear()
  }

  def start(port: String): Unit = {

    if (!port.equals("")) {
          val config = ConfigFactory.parseString(s"""
            akka.remote.artery.canonical.port=$port
            akka.cluster.seed-nodes = ["akka://ClusterSystem@localhost:2551", "akka://ClusterSystem@localhost:$port"]
            """).withFallback(ConfigFactory.load())
      this.system = ActorSystem(Main(), "ClusterSystem", config)
      val cluster = Cluster(this.system)

    }
  }


  override def onExitButton(event: ActionEvent): Unit = {
    Platform.exit()
    System.exit(0)
  }
}
