import User.{MySerializable, PublicMessage}
import akka.actor.typed.Behavior
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Main{
  def apply(chatControllerImpl: ChatControllerImpl): Behavior[String] = start(chatControllerImpl)

  def start(chatControllerImpl: ChatControllerImpl):Behavior[String] = Behaviors.setup({context =>
    val actor = context.spawn(User(chatControllerImpl), "user")
    val topic = context.spawn(Topic[MySerializable]("chat"), "chat")
    topic ! Topic.Subscribe(actor)

    Behaviors.receiveMessage{
      case string: String => topic ! Topic.Publish(PublicMessage(from = "test", string))
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
          case PublicMessage(from, text) => chatControllerImpl.messagesField.appendText(s"$from: $text\n")
            this
          case PrivateMessage(from, to, text) => println(s"$from: $text")
            this
        }
      }
    }

  }
