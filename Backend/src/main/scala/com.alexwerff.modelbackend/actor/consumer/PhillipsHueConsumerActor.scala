package com.alexwerff.modelbackend.actor.consumer
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.pattern._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import com.alexwerff.modelbackend.data._

/**
  * Created by alexwerff on 21.11.17.
  */
class PhillipsHueConsumerActor extends Actor with ActorLogging{

   private val SPEECH_DELAY_MS = 5000
   implicit val materializer = ActorMaterializer()
   import context.system
   implicit val executionContext = system.dispatcher
   implicit val timeout = Timeout(3, TimeUnit.SECONDS)
   var last:Long = 0

   override def receive: Receive = {
      case ConsumerSyncActor.ConsumerConnected(config:PhillipsHueConsumerConfig,_,modelActor) =>
         context.become(connected(config,None,None,modelActor))
      case _=> println("Not Connected")
   }


   def connected(config:PhillipsHueConsumerConfig,gestureEvent:Option[GestureEvent],lastSpeech: Option[SpeechEvent],modelActor:ActorRef):Receive ={
      case GetConsumerConfigRequest(identifier)=> sender() ! config
      case skeletonEvent: SkeletonUpdatedEvent =>
      case PostConsumerConfigRequest(newConfig:PhillipsHueConsumerConfig)=>{
         context.become(connected(newConfig,gestureEvent,lastSpeech,modelActor))
         sender() ! ConsumerConfigResponse(newConfig)
      }
      case g:GestureStartEvent => context.become(connected(config,Some(g),lastSpeech,modelActor))
      case g:GestureEndEvent => context.become(connected(config,Some(g),lastSpeech,modelActor))
      case g:SliderUseEvent =>{
         val current  = System.currentTimeMillis()
         if(current - last > 100){
            last = current
            g.controlIds.foreach(modelID=>{
               modelActor.ask(GetObjectRequest(modelID)).foreach {
                  case ObjectResponse(id, obj: StateObject) => {
                     setLightState(config, obj.metaData, g.percentage)
                     modelActor ! PostObjectRequest(id, "-", obj.updateState(State(obj.state.on, g.percentage)).asInstanceOf[StateObject])
                  }
                  case _ =>
               }
            })
         }
      }
      case speechEvent:SpeechEvent => {
         context.become(connected(config,gestureEvent,Some(speechEvent),modelActor))
         gestureEvent.foreach{
            case p:PointingAtObjectEventStart => {
               modelActor.ask(GetObjectRequest(p.modelID)).foreach {
                  case ObjectResponse(id,obj:StateObject)=> {
                     speechEvent match{
                        case SpeechEvent(_,SpeechInput("Computer anschalten",_,_)) => {
                           setLightState(config,obj.metaData,true)
                           modelActor ! PostObjectRequest(id,"-",obj.updateState(State(true,obj.state.value)).asInstanceOf[StateObject])
                        }
                        case SpeechEvent(_,SpeechInput("Computer ausschalten",_,_)) => {
                           setLightState(config,obj.metaData,false)
                           modelActor ! PostObjectRequest(id,"-",obj.updateState(State(false,obj.state.value)).asInstanceOf[StateObject])
                        }
                        case SpeechEvent(_,SpeechInput("Computer schalten",_,_)) => {
                           setLightState(config,obj.metaData,!obj.state.on)
                           modelActor ! PostObjectRequest(id,"-",obj.updateState(State(!obj.state.on,obj.state.value)).asInstanceOf[StateObject])
                        }
                        case _ =>
                     }
                  }
               }
            }
            case _=>
         }
      }
      case _=>
   }

   def getApiUrl(config:PhillipsHueConsumerConfig):String= "http://"+config.address+"/api/"+config.username

   def setLightState(config:PhillipsHueConsumerConfig,meta:MetaData,value:Int)={
      println(s"PhillipsHue:Setting state for light to $value %.")
      val url = getApiUrl(config)+"/lights/"+meta.data.getOrElse(MetaData.KEY_HUE_ID,"-")+"/state"
      val v = (value/100f) * 250.0f
      val json = "{\"bri\":"+v.toInt+"}"
      Http().singleRequest(HttpRequest(uri = url,method = HttpMethods.PUT,entity = json))foreach {
         case HttpResponse(StatusCodes.OK, headers, entity, _) => entity.dataBytes.runFold(ByteString())(_ ++ _)
         case HttpResponse(code, _, entity, _) => entity.toStrict(100, materializer)
         case unknown =>
      }
   }

   def setLightState(config:PhillipsHueConsumerConfig,meta:MetaData,on:Boolean)={
      println(s"PhillipsHue:Setting state for light to $on.")
      val url = getApiUrl(config)+"/lights/"+meta.data.getOrElse(MetaData.KEY_HUE_ID,"-")+"/state"
      val json = "{\"on\":"+on.toString+"}"
      val entity =  HttpEntity(ContentTypes.`application/json`, json)
      Http().singleRequest(HttpRequest(uri = url,method = HttpMethods.PUT,entity = json))foreach {
         case HttpResponse(StatusCodes.OK, headers, entity, _) => entity.dataBytes.runFold(ByteString())(_ ++ _)
         case HttpResponse(code, _, entity, _) => entity.toStrict(100, materializer)
         case unknown =>
      }
   }
}

object PhillipsHueConsumerActor{
   def props():Props = Props(new PhillipsHueConsumerActor())
}
