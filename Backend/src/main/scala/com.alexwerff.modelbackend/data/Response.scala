package com.alexwerff.modelbackend.data

import com.alexwerff.modelbackend.actor.model.GestureAnalysis


/**
  * Created by alexwerff on 30.08.17.
  */
sealed trait Response {}

case class ErrorResponse(message:String) extends Response{}
case class SuccessResponse(message:String) extends Response{}
case class InfoResponse(eventSocketAddress:String,skeletonSocketAddress:String) extends Response{}



//3D Model
case class ModelResponse(model:DataModel) extends Response{}
case class ObjectResponse(identifier:String, content:ModelObject) extends Response{}
case class UpdateObjectResponse(identifier:String) extends Response{}
case class DeleteObjectResponse(identifier:String) extends Response{}


//Config
case class ProviderConfigResponse(config: ProviderConfig) extends Response{}
case class ProviderConfigsResponse(configs:List[ProviderConfig]) extends Response{}
case class DeleteProviderConfigResponse(identifier:String) extends Response{}

case class ConsumerConfigResponse(config: ConsumerConfig) extends Response{}
case class ConsumerConfigsResponse(configs:List[ConsumerConfig]) extends Response{}
case class DeleteConsumerConfigResponse(identifier:String) extends Response{}

case class GestureResponse(gesture:GestureAnalysis) extends Response{}
case class GesturesResponse(gestures:List[GestureAnalysis]) extends Response{}

case class SkeletonsResponse(skeletons:List[Skeleton]) extends Response{}
case class SkeletonResponse(skeleton:Skeleton) extends Response{}

case class SpeechesResponse(speeches:Map[String,SpeechModel]) extends Response{}