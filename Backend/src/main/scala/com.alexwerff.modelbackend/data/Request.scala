package com.alexwerff.modelbackend.data

import com.alexwerff.modelbackend.actor.model.GestureAnalysis


/**
  * Created by alexwerff on 30.08.17.
  */
sealed trait Request {}
case class ErrorRequest(request:String) extends Request
case class GetInfoRequest() extends Request

//3D Model Requests
sealed trait ModelRequest extends Request{}
case class GetModelRequest() extends  ModelRequest{}
case class PostModelRequest(model:DataModel) extends  ModelRequest{}
case class GetObjectRequest(identifier:String) extends ModelRequest{}
case class PostObjectRequest(identifier: String,parentIdentifier:String, model:ModelObject) extends ModelRequest{}
case class DeleteObjectRequest(identifier:String) extends ModelRequest{}

//Config Requests
case class PostProviderConfigRequest(config: ProviderConfig) extends ModelRequest{}
case class GetProviderConfigRequest(identifier:String) extends ModelRequest{}
case class GetProviderConfigsRequest() extends ModelRequest{}
case class DeleteProviderConfigRequest(identifier:String) extends ModelRequest{}

case class PostConsumerConfigRequest(config: ConsumerConfig) extends ModelRequest{}
case class GetConsumerConfigRequest(identifier:String) extends ModelRequest{}
case class GetConsumerConfigsRequest() extends ModelRequest{}
case class DeleteConsumerConfigRequest(identifier:String) extends ModelRequest{}

case class PostGestureRequest(gesture:GestureAnalysis) extends ModelRequest{}
case class GetGesturesRequest() extends ModelRequest{}

//Sceleton Requests
case class PostSkeletonRequest(config: ProviderConfig, skeleton: Skeleton) extends ModelRequest{}
case class PostSkeletonLostRequest(config: ProviderConfig,identifier: String) extends ModelRequest{}
case class PostSkeletonNewRequest(config: ProviderConfig,identifier: String) extends ModelRequest{}
case class GetSkeletonRequest(identifier:String) extends ModelRequest{}
case class GetSkeletonsRequest() extends ModelRequest{}

//Speech Requests
case class PostSpeechRequest(config: ProviderConfig,speechModel: SpeechModel) extends ModelRequest{}
case class GetSpeechesRequest() extends ModelRequest{}



