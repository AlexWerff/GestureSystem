package com.alexwerff.modelbackend.actor.model

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, Props, _}
import com.alexwerff.modelbackend.actor.consumer.ConsumerSyncActor
import com.alexwerff.modelbackend.data
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.{IOUtils, ModelUtils, SkeletonUtils, SyncUtils}

import scala.util.{Failure, Success}


class DataModelActor extends Actor with ActorLogging {

   implicit val executionContext = context.system.dispatcher

   override def receive(): Receive = {
      case c:DataModelActor.Connected => {
         IOUtils.loadModel(c.path).onComplete{
            case Success(dataModel:DataModel)=>{
               val modelAnalyseActor = context.actorOf(ModelAnalyseActor.props())
               context.watch(modelAnalyseActor)
               modelAnalyseActor ! ModelAnalyseActor.Connected(self,c.consumerSyncActor,dataModel,c.gesturesPath)
               val skeletonActor = context.actorOf(SkeletonSyncActor.props())
               skeletonActor ! SkeletonSyncActor.Connected(dataModel,modelAnalyseActor)
               context.become(initialised(dataModel,modelAnalyseActor,skeletonActor,c))
            }
            case Failure(ex) => sender() ! ErrorResponse ("Unable to load model")
         }
      }
      case _ => sender() ! ErrorResponse("Model not ready.") //Model not ready. Ignore other messages than LoadModel
   }

   def initialised(dataModel: DataModel,analyseActor: ActorRef,skeletonActor:ActorRef,connected:DataModelActor.Connected): Receive = {
      case req:PostSkeletonRequest => skeletonActor ! req
      case req:PostSkeletonNewRequest => skeletonActor ! req
      case req:PostSkeletonLostRequest => skeletonActor ! req
      case req:GetSkeletonsRequest => skeletonActor.forward(req)
      case req:PostSpeechRequest => analyseActor.forward(req)
      case req:GetGesturesRequest => analyseActor.forward(req)
      case req:PostGestureRequest => analyseActor.forward(req)
      case req:GetModelRequest => sender() ! ModelResponse(dataModel)
      case req:PostModelRequest => {
         val s = sender()
         IOUtils.saveModel(connected.path,req.model).onComplete{
            case Success(true)=>{
               s ! ModelResponse(req.model)
               context.become(initialised(req.model,analyseActor,skeletonActor,connected))
               skeletonActor ! SkeletonSyncActor.ModelUpdated(req.model)
            }
            case _ => s ! ErrorResponse("Unable to save new model.")
         }
      }
      case GetObjectRequest(identifier) => {
         ModelUtils.findModelByIdentifier(dataModel.scenes.toList,identifier) match {
            case Some(model) => sender() ! ObjectResponse(identifier, model)
            case None => sender() ! ErrorResponse("Unable to find requested object")
         }
      }
      case DeleteObjectRequest(identifier) =>{
         val s = sender()
         ModelUtils.deleteModel(dataModel,identifier) match {
            case Success(newModel:DataModel) =>{
               IOUtils.saveModel(connected.path,newModel)
               analyseActor.forward(ModelAnalyseActor.ModelObjectDeleted(identifier,dataModel,newModel))
               context.become(initialised(newModel,analyseActor,skeletonActor,connected))
               skeletonActor ! SkeletonSyncActor.ModelUpdated(newModel)
               s ! DeleteObjectResponse(identifier)
            }
            case _=> s ! ErrorResponse("Unable to delete object.")
         }
      }
      case PostObjectRequest(identifier,parentIdentifier,model:ModelObject) => {
         val s = sender()
         ModelUtils.updateModel(dataModel,parentIdentifier,(identifier,model)) match {
            case Success(newModel: DataModel) => {
               IOUtils.saveModel(connected.path, newModel)
               model match{
                  case s:StateObject => analyseActor.forward(ModelAnalyseActor.ModelObjectStateUpdated(identifier, s.state, dataModel, newModel))
                  case _ => analyseActor.forward(ModelAnalyseActor.ModelObjectUpdated(identifier, dataModel, newModel))
               }

               context.become(initialised(newModel,analyseActor,skeletonActor,connected))
               skeletonActor ! SkeletonSyncActor.ModelUpdated(newModel)
               s ! UpdateObjectResponse(identifier)
            }
            case _ => s ! ErrorResponse("Unable to update object.")
         }
      }
      case req:DataModelActor.SaveModel => IOUtils.saveModel(connected.path, dataModel)
      case DataModelActor.ReInit => analyseActor ! ModelAnalyseActor.Connected(self,connected.consumerSyncActor,dataModel,connected.gesturesPath)
      case Terminated(modelAnalyseActor) => throw new Exception("Model Analyse Actor terminated") //Let this escalate to supervision
      case _ => ErrorResponse("Invalid request for Model.")
   }


   override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 3) {
      case ex => {
         self ! DataModelActor.ReInit() //Try ReInit for DataModel with initial config
         Restart
      }
   }
}




object DataModelActor {
   case class Connected(path: String,gesturesPath:String,consumerSyncActor:ActorRef) {}
   case class SaveModel() {}
   case class ReInit(){}
   def props(): Props = Props(new DataModelActor())
}
