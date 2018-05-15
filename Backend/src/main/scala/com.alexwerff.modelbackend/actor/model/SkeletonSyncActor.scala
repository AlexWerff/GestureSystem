package com.alexwerff.modelbackend.actor.model

import java.util.concurrent.TimeUnit

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import com.alexwerff.modelbackend.data._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.alexwerff.modelbackend.utils.SkeletonUtils

import scala.concurrent.Future


/**
  * Created by alexwerff on 22.03.18.
  */
class SkeletonSyncActor extends Actor with ActorLogging{
   implicit val timeout = Timeout(3, TimeUnit.SECONDS)
   implicit val materializer = ActorMaterializer()
   implicit val executionContext = context.system.dispatcher

   override def receive: Receive = {
      case SkeletonSyncActor.Connected(dataModel,analyseActor) =>{
         context.become(connected(dataModel,Map(),Map(),analyseActor))
      }
      case _=> sender() ! ErrorResponse("SkeletonSync not initialised.")
   }

   def pending(dataModel: DataModel,skeletonActors:Map[String,ActorRef],skeletons:Map[String,SkeletonGroup],analyseActor: ActorRef):Receive={
      case s:SkeletonSyncActor.NewSkeletonGroup => {
         if(!skeletons.contains(s.skeletonGroup._1)) analyseActor ! SkeletonNewEvent(s.skeletonGroup._1)
         context.become(connected(dataModel,skeletonActors,skeletons++ Map(s.skeletonGroup._1 -> s.skeletonGroup._2),analyseActor))
         skeletonActors.foreach(_._2 ! s)
      }
      case _=> //PENDING. NEED TO BE RESOLVED TO ACCEPT NEW SKELETONS. NECESSARY FOR CORRECT SYNC
   }

   def connected(dataModel: DataModel,skeletonActors:Map[String,ActorRef],skeletons:Map[String,SkeletonGroup],analyseActor: ActorRef):Receive={
      case req:PostSkeletonRequest => {
         val key = SkeletonUtils.getSkeletonUID(req.config,req.skeleton)
         if(!skeletonActors.contains(key)){
            val actor = context.actorOf(SkeletonAnalysisActor.props())
            context.watch(actor)
            context.become(pending(dataModel,skeletonActors ++ Map(key -> actor),skeletons,analyseActor))
            actor ! SkeletonAnalysisActor.Connected(dataModel,analyseActor,self,skeletons)
            actor ! req
         }
         else{
            skeletonActors.filter(p=> p._1 == key).foreach(_._2 ! req)
         }
      }
      case req:GetSkeletonsRequest => {
         if(skeletonActors.isEmpty) sender() ! SkeletonsResponse (List())
         else skeletonActors.head._2.forward(req)
      }
      case s:SkeletonSyncActor.NewSkeletonGroup => {
         if(!skeletons.contains(s.skeletonGroup._1)) analyseActor ! SkeletonNewEvent(s.skeletonGroup._1)
         context.become(connected(dataModel,skeletonActors,skeletons++ Map(s.skeletonGroup._1 -> s.skeletonGroup._2),analyseActor))
         skeletonActors.foreach(_._2 ! s)
      }
      case s:SkeletonSyncActor.LostSkeletonGroup =>{
         context.become(connected(dataModel,skeletonActors,skeletons - s.id,analyseActor))
         skeletonActors.foreach(_._2 ! s)
         analyseActor ! SkeletonLostEvent(s.id)
      }
      case evt:SkeletonSyncActor.ModelUpdated => skeletonActors.foreach(_._2 ! evt)
      case req:PostSkeletonNewRequest => //Doesn't matter because PostSkeletonRequest handles it
      case req:PostSkeletonLostRequest => {
         val key = SkeletonUtils.getSkeletonUID(req.config,req.identifier)
         skeletonActors.get(key).foreach(f=>{
            f ! req
            context.stop(f)
            context.become(connected(dataModel,skeletonActors-key,skeletons,analyseActor))
         })
      }
      case Terminated(killedActor) => context.become(connected(dataModel,skeletonActors.filter(_._2== killedActor),skeletons,analyseActor))
      case m => sender() !ErrorResponse("Unknown message")
   }



   override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
      case d => Stop //On error stop actor and remove it. If new skeleton comes in it will be restarted.
   }
}

object SkeletonSyncActor {
   case class Connected(dataModel: DataModel,analyseActor: ActorRef) {}
   case class ModelUpdated(dataModel:DataModel){}
   case class NewSkeletonGroup(skeletonGroup: (String,SkeletonGroup)){}
   case class LostSkeletonGroup(id:String){}
   def props(): Props = Props(new SkeletonSyncActor())
}
