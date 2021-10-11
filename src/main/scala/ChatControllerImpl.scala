import User.{MySerializable, PublicMessage}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.pubsub.Topic
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import javafx.application.Platform
import javafx.event.ActionEvent

class ChatControllerImpl extends ChatController{

  var login: String = _ // var, потому что сюда передается login из окна входа
  var topic: ActorRef[Topic.Command[MySerializable]] = _


  override def onSendMessageButton(event: ActionEvent): Unit = {
    val nickname = login // создаю val на основе var login, чтобы не отправлять var
    val message = messageInput.getText.trim
  }

  def start(port: String): Unit = {

    if (!port.equals("")) {
          val config = ConfigFactory.parseString(s"""
            akka.remote.artery.canonical.port=$port
            akka.cluster.seed-nodes = ["akka://ClusterSystem@localhost:2551", "akka://ClusterSystem@localhost:$port"]
            """).withFallback(ConfigFactory.load())
      val system: ActorSystem[MySerializable] = ActorSystem(User(), "user", config)
      val cluster = Cluster(system)
    }
  }


  override def onExitButton(event: ActionEvent): Unit = {
    Platform.exit()
    System.exit(0)
  }
}
