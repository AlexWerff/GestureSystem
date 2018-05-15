package com.alexwerff.modelbackend.actor

import akka.actor.SupervisorStrategy.{Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, AllForOneStrategy, Props, SupervisorStrategy}
import com.alexwerff.modelbackend.actor.consumer.ConsumerSyncActor
import com.alexwerff.modelbackend.actor.model.DataModelActor
import com.alexwerff.modelbackend.actor.provider.ProviderSyncActor
import com.alexwerff.modelbackend.data.{ErrorResponse}

/**
  * Created by alexwerff on 14.03.18.
  */
class SystemSupervisorActor extends Actor with ActorLogging{
   override def receive: Receive = {
      case init:SystemSupervisorActor.Initialised => {
         val modelActor = context.actorOf(DataModelActor.props(),"modelActor")
         val restActor = context.actorOf(RESTActor.props(),"restActor")
         val providerActor = context.actorOf(ProviderSyncActor.props(),"providerActor")
         val consumerActor = context.actorOf(ConsumerSyncActor.props(),"consumerActor")
         consumerActor ! ConsumerSyncActor.Connected(init.consumerConfigPath,modelActor)
         modelActor ! DataModelActor.Connected(init.modelPath,init.gesturesConfigPath,consumerActor)
         restActor ! RESTActor.Connected(self,modelActor,providerActor,consumerActor)
         providerActor ! ProviderSyncActor.Connected(modelActor,init.providerConfigPath)
         context.become(initialised(init,modelActor,restActor,providerActor,consumerActor))
      }
      case _=> {
         println("SystemSupervisionActor not initialised.")
         sender () ! ErrorResponse("Internal Server Error (SSVA not initialsed)")
      }
   }

   def initialised(init:SystemSupervisorActor.Initialised,modelActor: ActorRef,restActor: ActorRef,providerActor:ActorRef,consumerActor:ActorRef):Receive ={
      case SystemSupervisorActor.ReInit() => {
         consumerActor ! ConsumerSyncActor.Connected(init.consumerConfigPath,modelActor)
         modelActor ! DataModelActor.Connected(init.modelPath,init.gesturesConfigPath,consumerActor)
         restActor ! RESTActor.Connected(self,modelActor,providerActor,consumerActor)
         providerActor ! ProviderSyncActor.Connected(modelActor,init.providerConfigPath)
      }
      case m => restActor.forward(m) //TODO Limit to Request
   }


   override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy(maxNrOfRetries = 3){
      case _ => {
         self ! SystemSupervisorActor.ReInit() //Restart new Actors
         Restart
      }
   }
}

object SystemSupervisorActor{
   case class Initialised(consumerConfigPath:String,modelPath:String,providerConfigPath:String,gesturesConfigPath:String){}
   case class ReInit(){}
   def props():Props= Props(new SystemSupervisorActor())
}
