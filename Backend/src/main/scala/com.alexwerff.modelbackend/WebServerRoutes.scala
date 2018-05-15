package com.alexwerff.modelbackend

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{TextMessage, Message => WSMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.Timeout
import com.alexwerff.modelbackend.actor._
import com.alexwerff.modelbackend.actor.consumer.ConsumerSyncActor
import com.alexwerff.modelbackend.actor.model.{DataModelActor, GestureAnalysis}
import com.alexwerff.modelbackend.actor.provider.ProviderSyncActor
import com.alexwerff.modelbackend.actor.socket.{EventSocketActor, SkeletonEventSocketActor}
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.JSONUtils
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.util.Success

/**
  * Created by alexwerff on 30.08.17.
  */
object WebServerRoutes{
   implicit val timeout = Timeout(3, TimeUnit.SECONDS)

   def getRoutes(host:String,port:Int): Route = {
      implicit val system = ActorSystem("modelbackend")
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher


      val systemSupervisorActor = system.actorOf(SystemSupervisorActor.props())
      val broadcastActor = system.actorOf(BroadcastActor.props(), "broadcastActor")

      val modelPath = "model/model.json"
      val providerConfigPath = "model/provider.json"
      val consumerConfigPath = "model/consumer.json"
      val gesturesConfigPath = "model/gestures.json"

      systemSupervisorActor ! SystemSupervisorActor.Initialised(consumerConfigPath,modelPath,providerConfigPath,gesturesConfigPath)

      def skeletonOutputHandler(): Flow[WSMessage, WSMessage, NotUsed] = {
         val skeletonSocketActor = system.actorOf(SkeletonEventSocketActor.props())

         val incomingMessages: Sink[WSMessage, NotUsed] =
            Flow[WSMessage].map((message: WSMessage) => {
               ErrorRequest("No incoming Requests allowed")
            }).to(Sink.actorRef[Request](skeletonSocketActor, PoisonPill))

         val outgoingMessages: Source[WSMessage, NotUsed] =
            Source.actorRef[Event](0, OverflowStrategy.dropHead)
               .map((message) => {
                  TextMessage(JSONUtils.minimize(message.asJson.noSpaces))
               })
               .mapMaterializedValue { outActor =>
                  skeletonSocketActor ! SkeletonEventSocketActor.Connected(outActor)
                  NotUsed
               }
         Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
      }

      def eventOutputHandler(): Flow[WSMessage, WSMessage, NotUsed] = {
         val eventSocketActor = system.actorOf(EventSocketActor.props())

         val incomingMessages: Sink[WSMessage, NotUsed] =
            Flow[WSMessage].map((message: WSMessage) => {
               ErrorRequest("No incoming Requests allowed")
            }).to(Sink.actorRef[Request](eventSocketActor, PoisonPill))

         val outgoingMessages: Source[WSMessage, NotUsed] =
            Source.actorRef[Event](50, OverflowStrategy.dropHead)
               .map((message) => {
                  TextMessage(JSONUtils.minimize(message.asJson.noSpaces))
               })
               .mapMaterializedValue { outActor =>
                  eventSocketActor ! EventSocketActor.Connected(outActor)
                  NotUsed
               }
         Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
      }

      def assets = getFromResourceDirectory("web") ~ pathSingleSlash(getFromResource("web/admin/index.html"))
      val mainRoute =
         assets~
         path("api" / "getInfo") {
            get {
               val baseUrl = "ws://"+host+":"+port+"/sockets"
               complete(StatusCodes.OK, parseJSON(InfoResponse(baseUrl+"/eventSocket",baseUrl+"/skeletonSocket")))
            }
         } ~
         path("api" / "postGesture"){
               post {
                  entity(
                     as[String].map(
                        (content=>{
                           decode[GestureAnalysis](content).toOption match {
                              case Some(gesture:GestureAnalysis) => PostGestureRequest(gesture)
                              case None => ErrorRequest("Malformed content.")
                           }
                        })
                     )
                  )
                  {
                     (content)=>
                        onSuccess(systemSupervisorActor ? content) {
                           case response: GestureResponse => complete(StatusCodes.OK, parseJSON(response))
                           case errorResponse: ErrorResponse => complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                           case _ => complete(StatusCodes.InternalServerError)
                        }
                  }
               }
            }~
         path("api" / "getGestures") {
               get {
                  onSuccess(systemSupervisorActor ? GetGesturesRequest()) {
                     case response: GesturesResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            } ~
         path("api" / "postProviderConfig"){
            post {
               entity(
                  as[String].map(
                     (content=>{
                        decode[ProviderConfig](content).toOption match {
                           case Some(config:ProviderConfig) => PostProviderConfigRequest(config)
                           case None => ErrorRequest("Malformed content.")
                        }
                     })
                  )
               )
               {
                  (content)=>
                  onSuccess(systemSupervisorActor ? content) {
                     case response: ProviderConfigResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            }
         }~
         path("api" / "getProviderConfigs") {
               get {
                  onSuccess(systemSupervisorActor ? GetProviderConfigsRequest()) {
                     case response: ProviderConfigsResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            } ~
         path("api" / "deleteProviderConfig") { //TODO Implement
               post {
                  parameters('identifier.as[String]) { (identifier) =>
                     onSuccess(systemSupervisorActor ? DeleteProviderConfigRequest(identifier)) {
                        case response: DeleteProviderConfigResponse =>
                           complete(StatusCodes.OK, parseJSON(response))
                        case errorResponse: ErrorResponse =>
                           complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                        case _ =>
                           complete(StatusCodes.InternalServerError)
                     }
                  }
               }
            } ~
         path("api" / "postConsumerConfig"){
            post {
               entity(
                  as[String].map(
                     (content=>{
                        decode[ConsumerConfig](content).toOption match {
                           case Some(config:ConsumerConfig) => PostConsumerConfigRequest(config)
                           case None => ErrorRequest("Malformed content.")
                        }
                     })
                  )
               )
               {
                  (content)=>
                     onSuccess(systemSupervisorActor ? content) {
                        case response: ConsumerConfigResponse =>
                           complete(StatusCodes.OK, parseJSON(response))
                        case errorResponse: ErrorResponse =>
                           complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                        case _ =>
                           complete(StatusCodes.InternalServerError)
                     }
               }
            }
         }~
         path("api" / "getConsumerConfigs") {
            get {
               onSuccess(systemSupervisorActor ? GetConsumerConfigsRequest()) {
                  case response: ConsumerConfigsResponse => complete(StatusCodes.OK, parseJSON(response))
                  case errorResponse: ErrorResponse => complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                  case _ => complete(StatusCodes.InternalServerError)
               }
            }
         } ~
         path("api" / "deleteConsumerConfig") {
               post {
                  parameters('identifier.as[String]) { (identifier) =>
                     onSuccess(systemSupervisorActor ? DeleteConsumerConfigRequest(identifier)) {
                        case response: DeleteConsumerConfigResponse =>
                           complete(StatusCodes.OK, parseJSON(response))
                        case errorResponse: ErrorResponse =>
                           complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                        case _ =>
                           complete(StatusCodes.InternalServerError)
                     }
                  }
               }
            } ~
         path("api" / "postModel"){
               post {
                  entity(
                     as[String].map(
                        (content=>{
                           val result = decode[DataModel](content)
                           decode[DataModel](content).toOption match {
                              case Some(model:DataModel) => PostModelRequest(model)
                              case None => ErrorRequest("Malformed content.")
                           }
                        })
                     )
                  )
                  {
                     (content)=>
                        onSuccess(systemSupervisorActor ? content) {
                           case response: ModelResponse => //TODO Broadcast
                              complete(StatusCodes.OK, parseJSON(response))
                           case errorResponse: ErrorResponse =>
                              complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                           case _ =>
                              complete(StatusCodes.InternalServerError)
                        }
                  }
               }
            }~
         path("api" / "getModel") {
            get {
               onComplete(systemSupervisorActor ? GetModelRequest()) {
                  case Success(response: ModelResponse) =>
                     complete(StatusCodes.OK, parseJSON(response))
                  case Success(errorResponse: ErrorResponse) =>
                     complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                  case _ =>
                     complete(StatusCodes.InternalServerError)
               }
            }

         } ~
         path("api" / "getObject") {
            post {
               entity(as[String].map((content=>{
                  val result = decode[GetObjectRequest](content)
                  decode[GetObjectRequest](content).toOption match {
                     case Some(request) => request
                     case None => ErrorRequest("Malformed content.")
                  }
               }))){ (content)=>
                  onSuccess(systemSupervisorActor ? content) {
                     case response: ObjectResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            }
         }  ~
         path("api" / "postObject") {
            post {
               entity(as[String].map((content=>{
                  val result = decode[PostObjectRequest](content)
                  decode[PostObjectRequest](content).toOption match {
                     case Some(request) => request
                     case None => ErrorRequest("Malformed content.")
                  }
               }))){ (content)=>
                  onSuccess(systemSupervisorActor ? content) {
                     case response: UpdateObjectResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.NotAcceptable, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            }
         }  ~
         path("api" / "deleteObject") {
            post {
               parameters('identifier.as[String]) { (identifier) =>
                  onSuccess(systemSupervisorActor ? DeleteObjectRequest(identifier)) {
                     case response: DeleteObjectResponse =>
                        complete(StatusCodes.OK, parseJSON(response))
                     case errorResponse: ErrorResponse =>
                        complete(StatusCodes.BadRequest, parseJSON(errorResponse))
                     case _ =>
                        complete(StatusCodes.InternalServerError)
                  }
               }
            }
         }  ~
         path("api" / "getSkeletons") {
            get{
               onSuccess(systemSupervisorActor ? GetSkeletonsRequest()){
                  case response: SkeletonsResponse => complete(StatusCodes.OK,parseJSON(response))
                  case response: ErrorResponse => complete(StatusCodes.BadRequest,parseJSON(response))
                  case _ => complete(StatusCodes.InternalServerError)
               }
            }
         } ~
         path("api" / "getSpeeches") {
            get{
               onSuccess(systemSupervisorActor ? GetSpeechesRequest()){
                  case response: SpeechesResponse => complete(StatusCodes.OK,parseJSON(response))
                  case response: ErrorResponse => complete(StatusCodes.BadRequest,parseJSON(response))
                  case _ => complete(StatusCodes.InternalServerError)
               }
            }
         } ~
         path("sockets" / "skeletonSocket") {
               handleWebSocketMessages(skeletonOutputHandler)
            } ~
         path("sockets" / "eventSocket") {
            handleWebSocketMessages(eventOutputHandler)
         }

      mainRoute
   }

   //Important to get Response-Type in front of content
   def parseJSON(response:Response):String = response.asJson.noSpaces

}
