import User.{MySerializable, PrivateMessage, PublicMessage, Start}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object User{

   trait MySerializable
    //сообщения внутри чата
  final case class PublicMessage(from: String, text: String) extends MySerializable
  final case class PrivateMessage(from: String, to: String, text: String) extends MySerializable
  final case object Start extends MySerializable

  def apply(): Behavior[MySerializable] = Behaviors.setup(context => new User(context, new ChatControllerImpl))
}

class User(context: ActorContext[MySerializable], chatControllerImpl: ChatControllerImpl) extends AbstractBehavior[MySerializable](context) {

  override def onMessage(msg: MySerializable): Behavior[MySerializable] = {
    msg match {
      case PublicMessage(from, text) => chatControllerImpl.messagesField.appendText(s"[$from]: $text\n")
        this
      case PrivateMessage(from, to, text) => if (from.equals(chatControllerImpl.login) | to.equals(chatControllerImpl.login))
        chatControllerImpl.messagesField.appendText(s"[Private] [$from]: $text\n")
        this
      case Start => _
    }

  }
}

