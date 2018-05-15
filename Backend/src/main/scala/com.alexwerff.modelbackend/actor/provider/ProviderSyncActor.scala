package com.alexwerff.modelbackend.actor.provider

import java.util.concurrent.TimeUnit

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Terminated}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.{IOUtils, SyncUtils}

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * Created by alexwerff on 13.09.17.
  */
class ProviderSyncActor() extends Actor with ActorLogging{

   implicit val timeout = Timeout(10, TimeUnit.SECONDS)
   implicit val materializer = ActorMaterializer()
   implicit val executionContext = context.system.dispatcher

   override def receive: Receive = {
      case ProviderSyncActor.Connected(modelActor,configPath:String)=>{
         IOUtils.loadConfigs(configPath).onComplete{
            case Success(configs) =>
               val resultMap = configs.flatMap(config => createProvider(config)).toMap
               context.become(syncActorConnected(configPath,resultMap,Map(),Map(),modelActor))
            case Failure(ex) => println(ex)
         }
      }
      case _ =>
   }

   def createProvider:PartialFunction[Config,Option[(String,ActorRef)]]={
      case config:ProviderConfig =>{
         val providerActor = context.actorOf(ProviderActor.props())
         providerActor ! ProviderActor.Connected(self,config)
         context.watch(providerActor)
         Some((config.identifier,providerActor))
      }
      case _ => None
   }


   def syncActorConnected(configPath:String,provider:Map[String,ActorRef],speeches:Map[String,SpeechModel],skeletons:Map[String,Skeleton],modelActor: ActorRef):Receive={
      case r:PostSkeletonRequest => {
         val skeletonID = r.skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID,"-")
         skeletons.get(skeletonID) match{
            case Some(skeleton) => {
               val timeBefore = skeleton.metaData.data.getOrElse(MetaData.KEY_TIMESTAMP,"0").toFloat
               val timeNow = r.skeleton.metaData.data.getOrElse(MetaData.KEY_TIMESTAMP,"0").toFloat
               val timeDelta = (timeNow-timeBefore) / 1000 //To Seconds
               val newSkeleton = Skeleton(r.skeleton.joints,r.skeleton.models,SyncUtils.generateMovement(skeleton,r.skeleton,timeDelta),r.skeleton.metaData)
               modelActor ! PostSkeletonRequest(r.config,newSkeleton)
            }
            case None =>  modelActor ! r
         }
         val newSkeletons = skeletons ++ Map(skeletonID -> r.skeleton)
         context.become(syncActorConnected(configPath,provider,speeches,newSkeletons,modelActor))
      }
      case s:PostSkeletonNewRequest => modelActor ! s
      case s:PostSkeletonLostRequest => modelActor ! s
      case PostSpeechRequest(config,speech:SpeechModel) => {
         val speechModel = SyncUtils.syncSpeech(speech,config,speeches)
         modelActor ! PostSpeechRequest(config,speechModel._2)
         context.become(syncActorConnected(configPath,provider,speeches ++ Map(speechModel),skeletons,modelActor))
      }
      case PostProviderConfigRequest(config)=>{
         provider.get(config.identifier) match{
            case Some(actorRef) => {
               val s = sender()
               for {
                  response <- actorRef.ask(PostProviderConfigRequest(config))
                  configs <- getConfigs(provider.toList)
                  success <- IOUtils.saveConfigs(configPath, configs)
               } yield {
                  if(success) {
                     s ! response
                     BroadcastActor.getDefault(context) ! ProviderConfigEvent(config)
                  } else s ! ErrorResponse("Unable to update config.")
               }
            }
            case None => {
               val newConfig = config.updateIdentifier(java.util.UUID.randomUUID.toString)
               createProvider(newConfig).foreach(p=>{
                  context.become(syncActorConnected(configPath,provider + p,speeches,skeletons,modelActor))
                  sender() ! ProviderConfigResponse(newConfig)
                  BroadcastActor.getDefault(context) ! ProviderConfigEvent(newConfig)
               })
            }
         }
      }
      case DeleteProviderConfigRequest(identifier)=>{
         provider.get(identifier) match{
            case Some(actorRef) => {
               context.system.stop(actorRef)
               context.become(syncActorConnected(configPath,provider - identifier,speeches,skeletons,modelActor))
               sender() ! DeleteProviderConfigResponse(identifier)
               BroadcastActor.getDefault(context) ! ProviderConfigDeletedEvent(identifier)
            }
            case None => sender() ! ErrorResponse("Unable to find Provider for identifier")
         }
      }
      case GetProviderConfigsRequest()=>{
         val s = sender()
         getConfigs(provider.toList).foreach(l=> s ! ProviderConfigsResponse(l))
      }
      case GetSpeechesRequest() => sender() ! SpeechesResponse(speeches)
      case ProviderSyncActor.ReInit() =>{
         for{
            configs <- IOUtils.loadConfigs(configPath)
         } configs.foreach(f=> provider.get(f.identifier).foreach(p=> p ! ProviderActor.Connected(self,f)))
      }
      case Terminated(killedProvider) => {
         val newProvider = provider.filter(p => p._2 != killedProvider)
         for{
            configs <- getConfigs(newProvider.toList)
            success <- IOUtils.saveConfigs(configPath,configs)
            if success
         }yield context.become(syncActorConnected(configPath,newProvider,speeches,skeletons,modelActor))
      }
      case default => println(default)
   }



   def getConfigs(actors:List[(String,ActorRef)]):Future[List[ProviderConfig]]= {
      Future.sequence(actors.map(f=> f._2 ? GetProviderConfigRequest(f._1)).map(_.recover{
         case e => None
      })).map(l=> l.collect{case a:ProviderConfig => a})
   }


   override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3) {
      case ProviderActor.ConnectionFailedException(actorRef,providerConfig:ProviderConfig) => {
         val newConfig = ProviderConfig(providerConfig.identifier,
            providerConfig.modelIdentifier,
            IOUtils.getLocalIP(),
            providerConfig.port+Random.nextInt(20), //Add random to port number (possible fix for closed port)
            providerConfig.sceneIdentifier,
            providerConfig.providerTypes)
         actorRef ! ProviderActor.Connected(self,newConfig) //Try reconnect and additional handling here
         Restart
      }
      case ex => { //No specific Actor therefore try to reinit all of them (only the restarted Actor will reinit)
         self ! ProviderSyncActor.ReInit()
         Restart
      }
   }
}


object ProviderSyncActor{
   case class Connected(modelActor:ActorRef,configPath:String){}
   case class ReInit(){}
   def props():Props= Props(new ProviderSyncActor())
}
