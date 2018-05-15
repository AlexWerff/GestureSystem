package com.alexwerff.modelbackend.actor.provider

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.Timeout
import akka.pattern.ask
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.JSONParser

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by alexwerff on 19.11.17.
  */
class ProviderActor extends Actor with ActorLogging{
   import context.system
   implicit val timeout = Timeout(10, TimeUnit.SECONDS)
   implicit val executionContext = system.dispatcher
   val SKELETON_LIFETIME_MS = 2000

   override def receive: Receive = {
      case ProviderActor.Connected(syncActor,providerConfig:ProviderConfig)=>{
         IO(Udp) ! Udp.Bind(self, new InetSocketAddress(providerConfig.remoteAddress,providerConfig.port))
         context.become(connected(syncActor,providerConfig))
      }
      case _=> sender() ! ErrorResponse("Provider not initialised.")
   }

   def connected(syncActor:ActorRef,config: ProviderConfig):Receive={
      case Udp.Bound(local) => {
         context.become(udpConnected(syncActor,sender(),config,Map()))
         context.system.scheduler.schedule(4 second,SKELETON_LIFETIME_MS millisecond, self, ProviderActor.Check())
      }
      case Udp.CommandFailed(ex) => throw ProviderActor.ConnectionFailedException(self,config)
      case GetProviderConfigRequest(_)=> sender() ! config
      case PostProviderConfigRequest(providerConfig) => {
         IO(Udp) ! Udp.Bind(self, new InetSocketAddress(providerConfig.remoteAddress,providerConfig.port))
         sender() ! ProviderConfigResponse(providerConfig)
      }
      case _=> sender() ! ErrorResponse("Provider not initialised.")
   }

   def udpConnected(syncActor: ActorRef,udpActor:ActorRef,config: ProviderConfig,skeletons:Map[String,Skeleton]):Receive={
      case message:Udp.Received => {
         JSONParser.decodeOscJson(message.data.utf8String) match{
            case Some(speech:SpeechInput) => syncActor ! PostSpeechRequest(config,speech)
            case Some(skeleton:Skeleton) => {
               val s = (skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID,"-"),skeleton)
               context.become(udpConnected(syncActor,udpActor,config,skeletons ++ Map(s._1->s._2)))
               syncActor ! PostSkeletonRequest(config,skeleton)
            }
            case Some(newSkeleton:SkeletonNewEvent) => syncActor ! PostSkeletonNewRequest(config,newSkeleton.identifier)
            case Some(lostSkeleton:SkeletonLostEvent) => syncActor ! PostSkeletonLostRequest(config,lostSkeleton.identifier)
            case None => //Print error
         }
      }
      case GetProviderConfigRequest(_) => sender() ! config
      case PostProviderConfigRequest(providerConfig) => {
         val s = sender()
         udpActor.ask(Udp.Unbind).onComplete{
            case Success(v) =>  {
               context.become(receive)
               self ! ProviderActor.Connected(syncActor,providerConfig)
               s ! ProviderConfigResponse(providerConfig)
            }
            case Failure(ex) => ErrorResponse("Unable to unbind UDP")
         }
      }
      case ProviderActor.Check()=>{
         skeletons.headOption.foreach(f=>{
            val current = System.currentTimeMillis()
            val lastTime = f._2.metaData.data.getOrElse(MetaData.KEY_TIMESTAMP,current).toString.toLong
            if((current - SKELETON_LIFETIME_MS) > lastTime) {
               println("No updates for skeletons from provider therefore delete skeletons")
               skeletons.foreach(f=> syncActor ! PostSkeletonLostRequest(config,f._1))
               context.become(udpConnected(syncActor,udpActor,config,Map()))
            }
         })

      }
      case _ => sender() ! ErrorResponse("Unknown Request for PA.")

   }
}

object ProviderActor{
   case class Connected(syncActor:ActorRef,providerConfig: Config){}
   case class Stopped(actor:ActorRef){}
   case class ConnectionFailedException(providerActor: ActorRef,providerConfig: Config) extends Exception
   case class Check(){}

   def props():Props={
      Props(new ProviderActor())
   }
}
