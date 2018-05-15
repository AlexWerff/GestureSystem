package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.data._

/**
  * Created by alexwerff on 05.12.17.
  */
object SyncUtils {
   val AMOUNT_SAME_JOINTS_TO_MERGE = 10
   val JOINT_DELTA_M = 0.03f
   val WEIGHT_HEAD = 10
   val WEIGHT_OTHER = 2

   def syncSpeech(speech:SpeechModel,config:ProviderConfig,speeches:Map[String,SpeechModel]):(String,SpeechModel) = (config.identifier,speech)


   def syncSkeleton(skeleton: (String,Skeleton),skeletons:Map[String,SkeletonGroup]):(String,SkeletonGroup)={
      lazy val uid = java.util.UUID.randomUUID.toString
      val res = skeletons.find(p => p._2.skeletons.exists(f => skeletonsAreSame(f, skeleton)))
      res match{
         case Some(v)=> (v._1,SkeletonGroup(v._2.skeletons ++ Map(skeleton._1 -> skeleton._2)))
         case None => (uid,SkeletonGroup(Map(skeleton._1 -> skeleton._2)))
      }
   }

   def skeletonsAreSame(skeleton: (String,Skeleton),otherSkeleton:(String,Skeleton)):Boolean =
      (skeleton._1 == otherSkeleton._1) || skeleton._2.joints.map((a)=> otherSkeleton._2.joints.map((b)=> jointsAreSame(a,b)).sum).sum > AMOUNT_SAME_JOINTS_TO_MERGE

   def jointsAreSame(jointA:(String,Joint),jointB:(String,Joint)):Int={
      val compare = (a:Joint,b:Joint) =>  (a.x + JOINT_DELTA_M) >= b.x && (a.x - JOINT_DELTA_M) <= b.x &&
                                          (a.y + JOINT_DELTA_M) >= b.y && (a.y - JOINT_DELTA_M) <= b.y &&
                                          (a.z + JOINT_DELTA_M) >= b.z && (a.z - JOINT_DELTA_M) <= b.z
      (jointA,jointB) match{
         case ((a,jA),(b,jB)) if a == b && compare(jA,jB) => a match{
            case Skeleton.HEAD => WEIGHT_HEAD
            case _ => WEIGHT_OTHER
         }
         case _ => 0
      }
   }

   def generateMovement(skeletonA: Skeleton,skeletonB: Skeleton,deltaTimeS:Float):Movement={
      skeletonA.joints.get(Skeleton.NECK).flatMap(f=>{
         skeletonB.joints.get(Skeleton.NECK).map(g=>{
            val v1 = Vector(f.x,f.y,f.z)
            val v2 = Vector(g.x,g.y,g.z)
            Movement(VectorUtils.getSpeed(v1,v2,deltaTimeS),VectorUtils.getDirection(v1,v2))
         })
      }).getOrElse(Movement.default)
   }

   def generateSkeleton(identifier:String,skeletonGroup: SkeletonGroup):Option[Skeleton]={
      val s = skeletonGroup.skeletons.values.headOption//.toList.sortBy(_.metaData.data.getOrElse(MetaData.KEY_TIMESTAMP,0)).headOption
      s.map(f=> Skeleton(skeletonGroup.skeletons.values.reduceLeft(mergeSkeletons).joints,f.models,f.movement,MetaData(f.metaData.data.map{
         case (MetaData.KEY_SKELETON_ID,d) => (MetaData.KEY_SKELETON_ID,identifier)
         case d => d
      })))
   }

   def mergeSkeletons(skeletonA: Skeleton,skeletonB: Skeleton):Skeleton={
      Skeleton(skeletonA.joints.map((a)=> {
         skeletonB.joints.get(a._1) match{
            case Some(joint) if joint.p > a._2.p => (a._1,joint)
            case _ => a
         }
      }),skeletonA.models,skeletonA.movement,skeletonA.metaData)
   }


}
