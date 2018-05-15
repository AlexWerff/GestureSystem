package com.alexwerff.modelbackend.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import com.alexwerff.modelbackend.data._


/**
  * Sync Actor for a DataModel
  * Synchronizes Requests (multiple InputDeviceActors sending similar informations)
  * Works as a router for incoming requests
  */
class RESTActor extends Actor with ActorLogging{

   override def receive: Receive = {
      case RESTActor.Connected(outActor,modelActor,providerActor,consumerActor:ActorRef)=> context.become(connected(outActor,modelActor,providerActor,consumerActor))
      case _ => sender() ! ErrorResponse("REST not initialised")
   }

   def connected(outActor:ActorRef,modelActor: ActorRef,providerActor: ActorRef,consumerActor:ActorRef):Receive={
      case req @ (PostModelRequest(_)|GetModelRequest()|PostObjectRequest(_,_,_)|DeleteObjectRequest(_)) => modelActor.forward(req)
      case req @ (GetGesturesRequest()|PostGestureRequest(_)|GetSkeletonsRequest() | GetObjectRequest(_)) => modelActor.forward(req)
      case req @ (PostProviderConfigRequest(_)|GetProviderConfigsRequest()|DeleteProviderConfigRequest(_)|GetSpeechesRequest()) => providerActor.forward(req)
      case req @ (PostConsumerConfigRequest(_)|GetConsumerConfigsRequest()|DeleteConsumerConfigRequest(_)) => consumerActor.forward(req)
      case ErrorRequest(error)=> sender() ! ErrorResponse(error)
      case resp:Response => outActor.forward(resp)
      case _ => sender() ! ErrorResponse("Invalid REST Request")
   }
}

object RESTActor{
   case class Connected(outActor:ActorRef,modelActor:ActorRef,providerActor:ActorRef,consumerActor:ActorRef){}
   def props():Props= Props(new RESTActor())
}
