package com.alexwerff.modelbackend.actor.model

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.{SkeletonUtils, SyncUtils}

/**
  * Created by alexwerff on 22.03.18.
  */
class SkeletonAnalysisActor extends Actor with ActorLogging{
   override def receive: Receive = {
      case SkeletonAnalysisActor.Connected(dataModel,analyseActor,modelActor,groups) =>{
         context.become(connected(dataModel,groups,analyseActor,modelActor))
         sender() ! SkeletonAnalysisActor.Connected(dataModel,analyseActor,modelActor,groups)
      }
      case _=> sender() ! ErrorResponse("SkeletonAnalysis not initialised.")
   }

   def connected(dataModel: DataModel,skeletonGroups:Map[String,SkeletonGroup],analyseActor: ActorRef,syncActor:ActorRef):Receive={
      case PostSkeletonRequest(config,skeleton) =>{
         val transformed = SkeletonUtils.transformToModel(skeleton,config,dataModel)
         val key = SkeletonUtils.getSkeletonUID(config,skeleton)
         val group = SyncUtils.syncSkeleton((key,transformed),skeletonGroups)
         context.become(connected(dataModel,skeletonGroups ++ Map(group._1 -> group._2),analyseActor,syncActor))
         syncActor ! SkeletonSyncActor.NewSkeletonGroup(group)
         analyseActor ! ModelAnalyseActor.AnalyseSkeleton(group._1,group._2)
      }
      case SkeletonSyncActor.NewSkeletonGroup(newGroup) => context.become(connected(dataModel,skeletonGroups ++ Map(newGroup._1 -> newGroup._2),analyseActor,syncActor))
      case SkeletonSyncActor.LostSkeletonGroup(identifier) => context.become(connected(dataModel,skeletonGroups - identifier,analyseActor,syncActor))
      case req:PostSkeletonLostRequest => {
         val key = SkeletonUtils.getSkeletonUID(req.config,req.identifier)
         val newGroups = skeletonGroups.map(f=> (f._1,SkeletonGroup(f._2.skeletons.filterNot(_._1 == key))))
         newGroups.filter(_._2.skeletons.isEmpty).foreach(f=> syncActor ! SkeletonSyncActor.LostSkeletonGroup(f._1))
         context.become(connected(dataModel,newGroups.filterNot(_._2.skeletons.isEmpty),analyseActor,syncActor))
      }
      case GetSkeletonsRequest() => sender() ! SkeletonsResponse(skeletonGroups.toList.flatMap(f=> SyncUtils.generateSkeleton(f._1,f._2)))
      case SkeletonSyncActor.ModelUpdated(newModel) => context.become(connected(newModel,skeletonGroups,analyseActor,syncActor))
      case m => sender() ! ErrorResponse("Unknown message")

   }
}

object SkeletonAnalysisActor {
   case class Connected(dataModel: DataModel,analyseActor: ActorRef,modelActor: ActorRef,skeletonGroups:Map[String,SkeletonGroup]) {}
   def props(): Props = Props(new SkeletonAnalysisActor())
}
