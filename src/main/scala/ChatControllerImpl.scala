import User.{MySerializable, PrivateMessage, PublicMessage}
import akka.actor.typed.ActorSystem
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import javafx.application.Platform
import javafx.event.ActionEvent

class ChatControllerImpl extends ChatController{

  var login: String = _ // var, потому что сюда передается login из окна входа
  var system: ActorSystem[MySerializable] = _

  override def onSendMessageButton(event: ActionEvent): Unit = {
    val nickname = login // создаю val на основе var login, чтобы не отправлять var
    val message = messageInput.getText.trim
    if (message != "") {
      message match {
        //приватное сообщение /private;получатель; текст
        case input: String if message.contains("/private") =>
          val command = input.split(";").toVector
          val to = command(1)
          val text = command(2)
          system ! PrivateMessage(nickname, to, text)

        case _ => system ! PublicMessage(nickname, message)
      }
      messageInput.clear()
    }


  }

  def start(port: String): Unit = {

    if (!port.equals("")) {
      val config = ConfigFactory.parseString(s"""
            akka.remote.artery.canonical.port=$port
            akka.cluster.seed-nodes = ["akka://ClusterSystem@localhost:2551", "akka://ClusterSystem@localhost:$port"]
            """).withFallback(ConfigFactory.load())
      this.system = ActorSystem(Main(this), "ClusterSystem", config)
      val cluster = Cluster(this.system)
      messagesField.appendText(s"Вы онлайн как: $login\nЧтобы отправить приватное сообщение используйте команду:\n/private;NicknameПолучателя;Текст сообщения\n\n")
    }
  }


  override def onExitButton(event: ActionEvent): Unit = {
    this.system.terminate()
    Platform.exit()
    System.exit(0)
  }

}