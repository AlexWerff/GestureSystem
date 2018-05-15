package com.alexwerff.modelbackend.actor.model

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.data.{DataModel, MetaData, SkeletonGroup, SkeletonUpdatedEvent}
import com.alexwerff.modelbackend.utils.{GestureUtils, SyncUtils}

/**
  * Created by alexwerff on 22.03.18.
  */
class GestureAnalysisActor extends Actor with ActorLogging{
   override def receive: Receive = {
      case GestureAnalysisActor.Connected(dataModel,gestures,consumerSyncActor)=>
         context.become(connected(dataModel,gestures,consumerSyncActor,BroadcastActor.getDefault(context)))
   }

   def connected(dataModel: DataModel,gestures:List[GestureAnalysis],consumerSyncActor:ActorRef,broadcastActor:ActorSelection):Receive={
      case GestureAnalysisActor.Update(newModel,newGestures) => context.become(connected(newModel,newGestures,consumerSyncActor,broadcastActor))
      case GestureAnalysisActor.AnalyseSkeleton (id,skeletonGroup)=>{
         SyncUtils.generateSkeleton(id,skeletonGroup).foreach(f=>{
            broadcastActor ! SkeletonUpdatedEvent(id,f)
            consumerSyncActor ! SkeletonUpdatedEvent(id,f)
            //if(f.metaData.data.getOrElse(MetaData.KEY_TIMESTAMP,"0").toLong % 2 == 0){
               GestureUtils.checkForGestures(dataModel,f,gestures).foreach(event =>{
                  broadcastActor ! event
                  consumerSyncActor ! event
               })
            //}
         })
      }
   }
}

object GestureAnalysisActor {
   case class Connected(dataModel: DataModel,gestures:List[GestureAnalysis],consumerSyncActor: ActorRef) {}
   case class Update(dataModel:DataModel,gestures:List[GestureAnalysis]){}
   case class AnalyseSkeleton(id:String,skeletonGroup: SkeletonGroup){}
   def props(): Props = Props(new GestureAnalysisActor())
}
