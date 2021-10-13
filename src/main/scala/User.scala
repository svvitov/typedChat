import User.{MySerializable}
import akka.actor.typed.{Behavior}
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Main{
  def apply(chatControllerImpl: ChatControllerImpl): Behavior[MySerializable] = start(chatControllerImpl)

 private def start(chatControllerImpl: ChatControllerImpl):Behavior[MySerializable] = Behaviors.setup({context =>
    val actor = context.spawn(User(chatControllerImpl), "user")
    val topic = context.spawn(Topic[MySerializable]("chat"), "chat")
    topic ! Topic.Subscribe(actor)

    Behaviors.receiveMessage{
      case msg => topic ! Topic.Publish(msg)
        Behaviors.same
    }
  })
}


  object User {
    trait MySerializable
    final case class PublicMessage(from: String, text: String) extends MySerializable
    final case class PrivateMessage(from: String, to: String, text: String) extends MySerializable
    final case class WhatsYourName() extends MySerializable
    final case class MyName(nickname: String) extends MySerializable
    final case class Bye(nickname: String) extends MySerializable

    def apply(chatControllerImpl: ChatControllerImpl): Behavior[MySerializable] = createActor(chatControllerImpl)

    private def createActor(controllerImpl: ChatControllerImpl): Behavior[MySerializable] = {
      Behaviors.setup(context => new UserBehavior(context, controllerImpl))
    }


    class UserBehavior(context: ActorContext[MySerializable], chatControllerImpl: ChatControllerImpl) extends AbstractBehavior[MySerializable](context) {

      val nickname = chatControllerImpl.login // создаю val на основе var login, чтобы не отправлять var

      override def onMessage(msg: MySerializable): Behavior[MySerializable] = {
        msg match {
          case PublicMessage(from, text) => chatControllerImpl.messagesField.appendText(s"[$from]: $text\n")
            this
          case PrivateMessage(from, to, text) => if (from.equals(chatControllerImpl.login) | to.equals(chatControllerImpl.login))
            chatControllerImpl.messagesField.appendText(s"[Private] [$from --> $to]: $text\n")
            this
          case WhatsYourName() => chatControllerImpl.system ! MyName(nickname)
            this
          case MyName(nickname) => if(!chatControllerImpl.onlineUsers.getText.contains(nickname)) chatControllerImpl.onlineUsers.appendText(s"\n$nickname")
            this
          case Bye(nickname) => chatControllerImpl.onlineUsers.setText(chatControllerImpl.onlineUsers.getText.replace(s"\n${nickname}", "").trim)
          this
          case _ => this
        }
      }
    }

  }
