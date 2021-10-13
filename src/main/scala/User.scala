import User.MySerializable
import akka.actor.typed.Behavior
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Main{
  def apply(chatControllerImpl: ChatControllerImpl): Behavior[MySerializable] = start(chatControllerImpl)

 private def start(chatControllerImpl: ChatControllerImpl):Behavior[MySerializable] = Behaviors.setup({context =>
    val actor = context.spawn(User(chatControllerImpl), "user")
    val topic = context.spawn(Topic[MySerializable]("chat"), "chat")
    topic ! Topic.Subscribe(actor)

    Behaviors.receiveMessage{
      case msg: MySerializable => topic ! Topic.Publish(msg)
        Behaviors.same
    }
  })
}


  object User {
    trait MySerializable
    final case class PublicMessage(from: String, text: String) extends MySerializable
    final case class PrivateMessage(from: String, to: String, text: String) extends MySerializable

    def apply(chatControllerImpl: ChatControllerImpl): Behavior[MySerializable] = createActor(chatControllerImpl)

    private def createActor(controllerImpl: ChatControllerImpl): Behavior[MySerializable] = {
      Behaviors.setup(context => new UserBehavior(context, controllerImpl))
    }


    class UserBehavior(context: ActorContext[MySerializable], chatControllerImpl: ChatControllerImpl) extends AbstractBehavior[MySerializable](context) {
      override def onMessage(msg: MySerializable): Behavior[MySerializable] = {
        msg match {
          case PublicMessage(from, text) => chatControllerImpl.messagesField.appendText(s"[$from]: $text\n")
            this
          case PrivateMessage(from, to, text) => if (from.equals(chatControllerImpl.login) | to.equals(chatControllerImpl.login))
            chatControllerImpl.messagesField.appendText(s"[Private] [$from]: $text")
            this
        }
      }
    }

  }
