package com.alexwerff.modelbackend.data
/**
  * Created by alexwerff on 31.08.17.
  */

sealed trait Event{}

case class ModelUpdatedEvent() extends Event{}
case class ObjectUpdatedEvent(identifier:String) extends Event{}
case class ObjectStateUpdated(identifier:String,state: State) extends Event{}
case class ObjectCreatedEvent(identifier:String) extends Event{}
case class ObjectDeletedEvent(identifier:String) extends Event{}

case class ProviderConfigEvent(config: ProviderConfig) extends Event{}
case class ProviderConfigDeletedEvent(identifier: String) extends Event{}

case class ConsumerConfigEvent(config: ConsumerConfig) extends Event{}
case class ConsumerConfigDeletedEvent(identifier: String) extends Event{}

case class SkeletonUpdatedEvent(identifier:String,skeleton: Skeleton) extends Event{}
case class SkeletonLostEvent(identifier:String) extends Event{}
case class SkeletonNewEvent(identifier:String) extends Event{}
case class SpeechEvent(identifier: String,speechModel: SpeechModel) extends Event{}

sealed trait GestureEvent extends Event{
   val modelID:String
   val skeletonID:String
   val bodyPartID:String
}
sealed trait GestureStartEvent extends GestureEvent{}
sealed trait GestureEndEvent extends GestureEvent{}

case class PointingAtObjectEventStart(modelID: String, skeletonID: String,bodyPartID:String) extends GestureStartEvent{}
case class PointingAtObjectEventEnd(modelID: String, skeletonID: String,bodyPartID:String) extends GestureEndEvent{}
case class SliderUseEvent(modelID: String,controlIds:List[String] ,percentage:Int,skeletonID: String,bodyPartID:String) extends GestureEvent{}
case class TouchEventStart(modelID:String ,skeletonID: String,bodyPartID:String) extends GestureStartEvent{}
case class TouchEventEnd(modelID: String , skeletonID: String,bodyPartID:String) extends GestureEndEvent{}
case class SwipeEvent(modelID: String ,skeletonID:String,bodyPartID:String,direction:String) extends GestureEvent{}

