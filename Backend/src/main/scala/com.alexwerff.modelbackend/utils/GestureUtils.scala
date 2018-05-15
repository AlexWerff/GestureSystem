package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.actor.model.GestureAnalysis
import com.alexwerff.modelbackend.data.{GestureEndEvent, GestureEvent, GestureStartEvent}
import com.alexwerff.modelbackend.data.{DataModel, Skeleton}

/**
  * Created by alexwerff on 21.11.17.
  */
object GestureUtils {
   def checkForGestures(model:DataModel,skeleton: Skeleton,gestures:List[GestureAnalysis]):List[GestureEvent] =
      gestures.filter(p=>p.active).flatMap((gesture)=> gesture.evalGesture(model,skeleton))


   def checkStartEnd(gestureEvent: GestureEvent,last:List[GestureEvent]):(Option[GestureEvent],List[GestureEvent])={
      val lastF = last.filter((p) => p.modelID == gestureEvent.modelID
         && p.skeletonID == gestureEvent.skeletonID
         && p.bodyPartID == gestureEvent.bodyPartID).lastOption.toList
      (gestureEvent,lastF)match{
         case (f:GestureStartEvent,List()) => (Some(f),lastF ++ List(f))
         case (f:GestureEndEvent,List()) => (Some(f),lastF ++ List(f))
         case (f:GestureStartEvent,List(r:GestureStartEvent)) => (None,lastF ++ List(r))
         case (f:GestureStartEvent,List(r:GestureEndEvent)) => (Some(f),lastF ++ List(f))
         case (f:GestureEndEvent,List(r:GestureEndEvent)) => (None,lastF ++ List(r))
         case (f:GestureEndEvent,List(r:GestureStartEvent)) => (Some(f),lastF ++ List(f))
         case (s,l) => (Some(s),l)
      }
   }

}
