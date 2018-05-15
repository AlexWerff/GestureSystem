package com.alexwerff.modelbackend.actor.consumer

import java.util.concurrent.TimeUnit

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.stream.ActorMaterializer
import com.alexwerff.modelbackend.data._
import akka.pattern.ask
import akka.util.Timeout
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.utils.IOUtils

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by alexwerff on 21.11.17.
  */
class ConsumerSyncActor extends Actor with ActorLogging{
   implicit val timeout = Timeout(1, TimeUnit.SECONDS)
   implicit val materializer = ActorMaterializer()
   implicit val executionContext = context.system.dispatcher

   override def receive: Receive = {
      case ConsumerSyncActor.Connected(configPath,modelActor)=>{
         IOUtils.loadConfigs(configPath).onComplete{
            case Success(configs:List[Config]) =>
               val actors = configs.map(config=> (config,ConsumerFactory.configToActor((config,context)))).toMap
               actors.foreach(f=> {
                  f._2 ! ConsumerSyncActor.ConsumerConnected(f._1,self,modelActor)
                  context.watch(f._2)
               })
               context.become(connected(configPath,actors.map(f=> (f._1.identifier,f._2)),modelActor))
            case Failure(ex)=> println(ex)
         }
      }
      case _ =>
   }



   def connected(configPath:String,consumer:Map[String,ActorRef],modelActor:ActorRef):Receive = {
      case event:Event => for(value <- consumer) value._2 ! event
      case PostConsumerConfigRequest(config)=>{
         consumer.get(config.identifier) match{
            case Some(actorRef) => {
               val s = sender ()
               for{
                  response <- actorRef ? PostConsumerConfigRequest(config)
                  configs <- getConfigs(consumer.toList)
                  result <- IOUtils.saveConfigs(configPath,configs)
               }yield {
                  s ! response
                  BroadcastActor.getDefault(context) ! ConsumerConfigEvent(config)
               }
            }
            case None => {
               val newConfig = config.updateIdentifier(java.util.UUID.randomUUID.toString)
               val providerActor = ConsumerFactory.configToActor(config,context)
               providerActor ! ConsumerSyncActor.ConsumerConnected(config,self,modelActor)
               context.watch(providerActor)
               context.become(connected(configPath,consumer + (newConfig.identifier->providerActor),modelActor))
               for{
                  configs <- getConfigs(consumer.toList)
                  result <- IOUtils.saveConfigs(configPath,configs)
               } yield {
                  sender() ! ConsumerConfigResponse(newConfig)
                  BroadcastActor.getDefault(context) ! ConsumerConfigEvent(newConfig)
               }
            }
         }
      }
      case DeleteConsumerConfigRequest(identifier)=>{
         consumer.get(identifier) match{
            case Some(actorRef) => {
               context.system.stop(actorRef)
               context.become(connected(configPath,consumer - identifier,modelActor))
               sender() ! DeleteConsumerConfigResponse(identifier)
               BroadcastActor.getDefault(context) ! ConsumerConfigDeletedEvent(identifier)
            }
            case None => sender() ! ErrorResponse("Unable to find Consumer for identifier")
         }
      }
      case GetConsumerConfigsRequest()=> {
         val s = sender()
         for{
            configs <- getConfigs(consumer.toList)
         }yield {
            s ! ConsumerConfigsResponse(configs)
         }
      }

      case ConsumerSyncActor.ReInit() =>{
         for{
            configs <- IOUtils.loadConfigs(configPath)
         } configs.foreach((f:Config) => consumer.get(f.identifier).foreach(p=> p ! ConsumerSyncActor.ConsumerConnected(f,self,modelActor)))
      }
      case Terminated(killedConsumer) => {
         val newConsumer = consumer.filter(p=> p._2 != killedConsumer)
         for{
            configs <- getConfigs(newConsumer.toList)
            success <- IOUtils.saveConfigs(configPath,configs)
            if success
         }yield context.become(connected(configPath,newConsumer,modelActor))
      }
      case default => sender() ! ErrorResponse("Unable to process request")

   }

   def getConfigs(actors:List[(String,ActorRef)]):Future[List[ConsumerConfig]]= {
      Future.sequence(actors.map(f=> f._2 ? GetConsumerConfigRequest(f._1)).map(_.recover{
         case e => None
      })).map(l=> l.collect{case a:ConsumerConfig => a})
   }

   override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3){
      case ex => { //No specific Actor therefore try to reinit all of them (the restarted Actor will reinit)
         self ! ConsumerSyncActor.ReInit()
         Restart
      }
   }
}

object ConsumerSyncActor{
   case class Connected(configPath:String,modelActor:ActorRef){}
   case class ReInit(){}
   case class ConsumerConnected(config:Config, syncActor:ActorRef,modelActor:ActorRef){}
   def props():Props= Props(new ConsumerSyncActor())
}
