package com.alexwerff.modelbackend.actor

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import com.alexwerff.modelbackend.data.Event

/**
  * Created by alexwerff on 05.09.17.
  */
class BroadcastActor extends Actor with ActorLogging{
   override def receive: Receive = process(List())

   def process(participants: List[ActorRef]):Receive={
      case BroadcastActor.AddParticipant(actorRef: ActorRef) => context.become(process(participants ++ List(actorRef)))
      case BroadcastActor.RemoveParticipant(actorRef: ActorRef) => context.become(process(participants.filter( p => p != actorRef)))
      case event:Event => for(actor <- participants) actor ! event
   }
}

object BroadcastActor{
   case class AddParticipant(actorRef: ActorRef){}
   case class RemoveParticipant(actorRef: ActorRef){}
   case class BroadcastEvent(message:Event){}

   def props():Props= Props(new BroadcastActor())

   def getDefault(context:ActorContext):ActorSelection= context.actorSelection("akka://modelbackend/user/broadcastActor")
}