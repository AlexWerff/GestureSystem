package com.alexwerff.modelbackend.actor.model

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.alexwerff.modelbackend.actor.BroadcastActor
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.{GestureUtils, IOUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by alexwerff on 06.09.17.
  */
class ModelAnalyseActor extends Actor with ActorLogging{
   override def receive: Receive = {
      case ModelAnalyseActor.Connected(modelActor,consumerSyncActor,dataModel,gesturesPath:String)=>
         IOUtils.loadGestures(gesturesPath).foreach(f=> context.become(updated(gesturesPath,consumerSyncActor,dataModel,Map(),f,BroadcastActor.getDefault(context))))
      case _=>
   }

   def updated(path:String,consumerSyncActor: ActorRef, model:DataModel,gestureActors:Map[String,ActorRef],gestures:List[GestureAnalysis], broadcastActor:ActorSelection):Receive={
      case ModelAnalyseActor.ModelObjectUpdated(identifier,oldModel,newModel) =>{
         consumerSyncActor ! ObjectUpdatedEvent(identifier)
         broadcastActor ! ObjectUpdatedEvent(identifier)
         context.become(updated(path,consumerSyncActor,newModel,gestureActors,gestures,broadcastActor))
         gestureActors.foreach(_._2 ! GestureAnalysisActor.Update(newModel,gestures))
      }

      case ModelAnalyseActor.ModelObjectStateUpdated(identifier,state:State,oldModel,newModel) =>{
         consumerSyncActor ! ObjectStateUpdated(identifier,state)
         broadcastActor ! ObjectStateUpdated(identifier,state)
         context.become(updated(path,consumerSyncActor,newModel,gestureActors,gestures,broadcastActor))
         gestureActors.foreach(_._2 ! GestureAnalysisActor.Update(newModel,gestures))
      }

      case ModelAnalyseActor.ModelObjectDeleted(identifier,oldModel,newModel) =>{
         consumerSyncActor ! ObjectDeletedEvent(identifier)
         broadcastActor ! ObjectDeletedEvent(identifier)
         context.become(updated(path,consumerSyncActor,newModel,gestureActors,gestures,broadcastActor))
         gestureActors.foreach(_._2 ! GestureAnalysisActor.Update(newModel,gestures))
      }

      case ModelAnalyseActor.ModelUpdated(oldModel,newModel) =>{
         consumerSyncActor ! ModelUpdatedEvent()
         context.become(updated(path,consumerSyncActor,newModel,gestureActors,gestures,broadcastActor))
         gestureActors.foreach(_._2 ! GestureAnalysisActor.Update(newModel,gestures))
         broadcastActor ! ModelUpdatedEvent()
      }
      case ModelAnalyseActor.AnalyseSkeleton(id,skeletonGroup) =>{
         if(!gestureActors.contains(id)){
            val actor = context.actorOf(GestureAnalysisActor.props())
            actor ! GestureAnalysisActor.Connected(model,gestures,consumerSyncActor)
            context.become(updated(path,consumerSyncActor,model,gestureActors ++ Map(id -> actor),gestures,broadcastActor))
            actor ! GestureAnalysisActor.AnalyseSkeleton(id,skeletonGroup)
         }
         gestureActors.get(id).foreach(_ ! GestureAnalysisActor.AnalyseSkeleton(id,skeletonGroup))
      }
      case PostSpeechRequest(config,speechModel)=>{
         broadcastActor ! SpeechEvent(config.identifier,speechModel)
         consumerSyncActor ! SpeechEvent(config.identifier,speechModel)
      }
      case s:SkeletonLostEvent =>{
         consumerSyncActor ! SkeletonLostEvent(s.identifier)
         broadcastActor ! SkeletonLostEvent(s.identifier)
      }
      case s:SkeletonNewEvent => {
         consumerSyncActor ! SkeletonNewEvent(s.identifier)
         broadcastActor ! SkeletonNewEvent(s.identifier)
      }
      case req:GetGesturesRequest => sender() ! GesturesResponse(gestures)
      case req:PostGestureRequest =>{
         val s = sender()
         val newGestures = gestures.map(g => if(g.name.equals(req.gesture.name)) req.gesture else g)
         IOUtils.saveGestures(path,newGestures).onComplete{
            case Success(b)=>{
               context.become(updated(path,consumerSyncActor,model,gestureActors,newGestures,broadcastActor))
               gestureActors.foreach(_._2 ! GestureAnalysisActor.Update(model,newGestures))
               s ! GestureResponse(req.gesture)
            }
            case Failure(ex)=>
         }
      }
   }

   override def aroundPreRestart(reason: Throwable, message: Option[Any]) = {
      message match{
         case Some(m)=> self ! m
         case _ =>
      }
   }

}


object ModelAnalyseActor{
   case class Connected(modelActor:ActorRef,consumerSyncActor:ActorRef, dataModel: DataModel,gesturesPath:String)
   case class ModelUpdated(oldModel:DataModel,dataModel: DataModel)
   case class ModelObjectUpdated(identifier:String,oldModel:DataModel,dataModel: DataModel)
   case class ModelObjectStateUpdated(identifier:String,state:State,oldModel:DataModel,dataModel: DataModel)
   case class ModelObjectDeleted(identifier:String,oldModel:DataModel,dataModel: DataModel)
   case class AnalyseSkeleton(id:String,skeletonGroup: SkeletonGroup){}

   def props():Props={
      Props(new ModelAnalyseActor())
   }
}