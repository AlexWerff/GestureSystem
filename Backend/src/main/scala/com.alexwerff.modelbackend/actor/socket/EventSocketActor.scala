package com.alexwerff.modelbackend.actor.socket

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.data._

/**
  * Created by alexwerff on 30.08.17.
  */

class EventSocketActor() extends Actor with ActorLogging{

   override def receive: Receive = {
      case EventSocketActor.Connected(outActor) => context.become(initialised(outActor,None,None))
      case _ =>
   }


   def initialised(outActor:ActorRef,lastGestureEvent:Option[GestureEvent],lastSpeechEvent:Option[SpeechEvent]):Receive={
      case event:SpeechEvent => context.become(initialised(outActor,lastGestureEvent,Some(event))); outActor ! event
      case event:GestureEvent => context.become(initialised(outActor,Some(event),lastSpeechEvent)); outActor ! event
      case event:SkeletonNewEvent => outActor ! event
      case event:SkeletonLostEvent => outActor ! event
      case event:SkeletonUpdatedEvent =>
      case event:Event => outActor ! event
      case _=>
   }

   override def preStart(): Unit = {
      println("EventSocket connected.")
      BroadcastActor.getDefault(context) ! BroadcastActor.AddParticipant(self)
      super.preStart()
   }

   override def postStop(): Unit = {
      println("EventSocket disconnected.")
      BroadcastActor.getDefault(context) ! BroadcastActor.RemoveParticipant(self)
      super.postStop()
   }
}

object EventSocketActor {
   case class Connected(actor:ActorRef){}
   def props(): Props = Props(new EventSocketActor())
}
