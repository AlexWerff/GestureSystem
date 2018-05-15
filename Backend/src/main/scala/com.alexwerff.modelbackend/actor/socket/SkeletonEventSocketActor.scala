package com.alexwerff.modelbackend.actor.socket

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.data.{Event, SkeletonLostEvent, SkeletonNewEvent, SkeletonUpdatedEvent}

/**
  * Created by alexwerff on 30.08.17.
  */

class SkeletonEventSocketActor() extends Actor with ActorLogging{

   override def receive: Receive = {
      case SkeletonEventSocketActor.Connected(outActor) => context.become(connected(outActor))
      case _ =>
   }

   def connected(outActor: ActorRef):Receive={
      case event:SkeletonUpdatedEvent => outActor ! event
      case event:SkeletonNewEvent => outActor ! event
      case event:SkeletonLostEvent => outActor ! event
      case _ =>
   }

   override def preStart(): Unit = {
      println("SkeletonEventSocket connected.")
      BroadcastActor.getDefault(context)! BroadcastActor.AddParticipant(self)
      super.preStart()
   }

   override def postStop(): Unit = {
      println("SkeletonEventSocket disconnected.")
      BroadcastActor.getDefault(context) ! BroadcastActor.RemoveParticipant(self)
      super.postStop()
   }
}

object SkeletonEventSocketActor {
   case class Connected(actor:ActorRef){}
   def props(): Props = Props(new SkeletonEventSocketActor())
}
