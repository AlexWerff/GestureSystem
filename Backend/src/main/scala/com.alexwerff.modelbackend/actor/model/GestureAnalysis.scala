package com.alexwerff.modelbackend.actor.model

import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.utils.{GestureUtils, ModelUtils, VectorUtils}
import com.alexwerff.modelbackend.data.VectorImprovements.OperationVector

/**
  * Created by alexwerff on 27.11.17.
  */
sealed trait GestureAnalysis {
   val active:Boolean
   val name:String

   def evalGesture(model:DataModel,skeleton: Skeleton):List[GestureEvent] =
      model.scenes.flatMap(s => evalRecursive(s._2.models,skeleton,model)).toList

   private def evalRecursive(models:Map[String,ModelObject],skeleton: Skeleton,dataModel: DataModel):List[GestureEvent] =
      models.filter((f)=> f._2.metaData.data.getOrElse(MetaData.KEY_INTERACTABLE,"") == "YES")
         .flatMap((f)=> eval(f,skeleton,dataModel) ++ evalRecursive(f._2.models,skeleton,dataModel)).toList


   protected def eval(model:(String,ModelObject),skeleton: Skeleton,dataModel: DataModel):List[GestureEvent]
}


case class PointingAtGesture(active:Boolean,name:String) extends GestureAnalysis{
   var last:List[GestureEvent] = List()
   override def eval(model:(String,ModelObject),skeleton:Skeleton,dataModel: DataModel):List[GestureEvent] = model match{
      case (_,model:SliderObject) => List()
      case _ => {
         val arms = List(("RIGHT",(skeleton.joints.get(Skeleton.RIGHT_ELBOW), skeleton.joints.get(Skeleton.RIGHT_HAND))),
            ("LEFT",(skeleton.joints.get(Skeleton.LEFT_ELBOW), skeleton.joints.get(Skeleton.LEFT_HAND))))
         arms.map((bodyPart)=> {
            val elbow = bodyPart._2._1
            val hand = bodyPart._2._2
            val id = bodyPart._1
            val line = Line(Vector(elbow.get.x, elbow.get.y, elbow.get.z), Vector(hand.get.x, hand.get.y, hand.get.z))
            val cuboid = Cuboid(model._2.modelProperties)
            if (cuboid.intersectsWithLine(line))
               PointingAtObjectEventStart(model._1, skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID, "-"),id)
            else
               PointingAtObjectEventEnd(model._1, skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID, "-"),id)
         }).flatMap((f: GestureEvent) => {
            val l = GestureUtils.checkStartEnd(f, last)
            last = last ++ l._2
            l._1
         })
      }
   }
}

case class SliderUseGesture(active:Boolean,name:String) extends GestureAnalysis{
   val JOINT_DELTA_M = 0.2f
   val last:List[GestureEvent] = List()
   override def eval(model:(String,ModelObject),skeleton:Skeleton,dataModel: DataModel):List[GestureEvent] = model match {
      case (sID,slider:SliderObject) => {
         val cuboid = Cuboid(model._2.modelProperties)
         (for{
            hand <- List(skeleton.joints.get(Skeleton.LEFT_HAND),skeleton.joints.get(Skeleton.RIGHT_HAND))
            joint <- hand
            if cuboid.containsPoint(Vector(joint.x,joint.y,joint.z))
         } yield{
            val scale = model._2.modelProperties.scale
            val joint3 = Vector(joint.x,joint.y,joint.z)

            val startV = cuboid.getBounds()(1)
            val distance = joint3.distanceTo(startV) //TODO Optimize distance

            val perFloat = distance / (scale.max - (scale.min/2))
            val perInt = ((perFloat-0.3f) * 100).toInt
            Some(SliderUseEvent(sID,slider.controlModels.toList,perInt,skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID,"-"),"RIGHT"))
         }).flatten
      }
      case _ => List()
   }
}




case class TouchGesture(active:Boolean,name:String) extends GestureAnalysis{
   val JOINT_DELTA_M = 0.1f
   var last:List[GestureEvent] = List()

   override def eval(model:(String,ModelObject),skeleton:Skeleton,dataModel: DataModel):List[GestureEvent] = model match {
      case (_,model:SliderObject) => List()
      case _ => {
         (for{
            hand <- List(skeleton.joints.get(Skeleton.RIGHT_HAND),skeleton.joints.get(Skeleton.LEFT_HAND))
            joint <- hand
         } yield{
            val cuboid = Cuboid(model._2.modelProperties)
            if (cuboid.containsPoint(Vector(joint.x,joint.y,joint.z)))
               TouchEventStart(model._1,skeleton.metaData.data.getOrElse(MetaData.KEY_NAME,"-"),"RIGHT")
            else
               TouchEventEnd(model._1,skeleton.metaData.data.getOrElse(MetaData.KEY_NAME,"-"),"RIGHT")
         }).flatMap((f)=>{
            val l = GestureUtils.checkStartEnd(f,last)
            last = last ++ l._2
            l._1
         })
      }
   }
}

case class SwipeGesture(active:Boolean,name:String) extends GestureAnalysis{

   var last:Map[String,(String,Skeleton)] = Map()
   var lastTime:Map[String,Long] = Map()

   override protected def eval(model: (String, ModelObject), skeleton: Skeleton, dataModel: DataModel): List[GestureEvent] = model match{
      case (sID,swipe:SwipeObject) => { //TODO CHANGE TO SWIPE OBJECT

         val cuboid = Cuboid(model._2.modelProperties)
         val current = System.currentTimeMillis()
         (for{
            hand <- List((Skeleton.RIGHT_HAND,skeleton.joints.get(Skeleton.RIGHT_HAND)),(Skeleton.LEFT_HAND,skeleton.joints.get(Skeleton.LEFT_HAND)))
            joint <- hand._2
            if cuboid.containsPoint(Vector(joint.x,joint.y,joint.z))
            lastS <- last.get(model._1) orElse {
               last = last ++ Map(model._1 -> (hand._1, skeleton))
               lastTime = lastTime ++ Map(model._1 -> current)
               None
            }
            vLast <- lastS._2.get(hand._1).map(f=> Vector(f.x,f.y,f.z))
            vNew <- skeleton.get(hand._1).map(f=> Vector(f.x,f.y,f.z))
         } yield{
            val deltaTimeS = (current - lastTime.getOrElse(model._1,current)) / 1000f
            val speed = VectorUtils.getDistanceSpeed(vLast,vNew,deltaTimeS)
            lastTime = lastTime ++ Map(model._1 -> System.currentTimeMillis())
            println(speed)
            if(speed > 15.0f){
               val dStart = cuboid.getBounds()(0).distanceTo(vNew)
               val dEnd = cuboid.getBounds()(1).distanceTo(vNew)
               val dir = if (dStart > dEnd) "RIGHT" else "LEFT"
               Some(SwipeEvent(model._1,skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID,"-"),"RIGHT",dir))

            }else None
         }).flatten
      }
      case _ => List()
   }
}


