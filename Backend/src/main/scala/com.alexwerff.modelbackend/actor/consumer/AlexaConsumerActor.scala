package com.alexwerff.modelbackend.actor.consumer

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.alexwerff.modelbackend.data.{ConsumerConfig, GetConsumerConfigRequest, SkeletonUpdatedEvent, SpeechEvent}

/**
  * Created by alexwerff on 21.11.17.
  */
class AlexaConsumerActor extends Actor with ActorLogging{
   override def receive: Receive = {
      case  ConsumerSyncActor.ConsumerConnected(config:ConsumerConfig,syncActor,_) => context.become(connected(config,syncActor))
      case _=> println("Not Connected")
   }

   def connected(config:ConsumerConfig, syncActor:ActorRef): Receive ={
      case GetConsumerConfigRequest(identifier)=> sender() ! config
      case skeletonEvent: SkeletonUpdatedEvent =>{

      }
      case speechEvent:SpeechEvent =>{

      }
      case _=>
   }
}

object AlexaConsumerActor{
   case class Connected(config:ConsumerConfig, syncActor:ActorRef){}
   def props():Props = Props(new AlexaConsumerActor())
}

