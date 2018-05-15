package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.data.ProviderConfig
import com.alexwerff.modelbackend.data._
import com.alexwerff.modelbackend.data.VectorImprovements.OperationVector

/**
  * Created by alexwerff on 01.02.18.
  */
object SkeletonUtils {

   def transformToModel(skeleton: Skeleton, config:ProviderConfig, dataModel: DataModel):Skeleton={
      (for{
         scene <- dataModel.scenes.get(config.sceneIdentifier)
         model:ModelObject <- scene.models.get(config.modelIdentifier)
      }yield{
         val rotatedSkeleton = SkeletonUtils.rotateSkeleton(skeleton,model.modelProperties.orientation)
         Some(SkeletonUtils.transformSkeleton(rotatedSkeleton,model.modelProperties.position))
      }).flatten.getOrElse(skeleton)
   }


   def rotateSkeleton(skeleton:Skeleton,vector: Vector[Float]):Skeleton={

      def shouldSwap(id:String,vector: Vector[Float]) = (id.contains("left") | id.contains("right")) && vector(1) > 500.0f

      val map = skeleton.joints.map(j=>{
         val jointVector = Vector(j._2.x,j._2.y,j._2.z)
         val v = jointVector.rotate(vector * Vector(1.0f,-1.0f,1.0f))
         val newJoint = Joint(v.x,v.y,v.z,j._2.p)
         (j._1,newJoint)
      }).map{ //TODO Check if necessary
         case (id,joint) if shouldSwap(id,vector) =>
            (if (id.contains("left")) id.replace("left","right") else id.replace("right","left"),joint)
         case p => p
      }
      Skeleton(map,skeleton.models,rotateMovement(skeleton.movement,vector),skeleton.metaData)
   }

   def transformSkeleton(skeleton: Skeleton,origin:Vector[Float]):Skeleton={
      val map = skeleton.joints.map(j=>{
         val jointVector = Vector(j._2.x * -1,j._2.y * -1,j._2.z)
         val v = origin + jointVector
         val newJoint = Joint(v.x,v.y,v.z,j._2.p)
         (j._1,newJoint)
      })
      Skeleton(map,skeleton.models,skeleton.movement,skeleton.metaData)
   }

   def rotateMovement(movement:Movement,vector: Vector[Float]):Movement={
      val dir = movement.direction.rotate(vector)
      Movement(movement.speedMPS,dir)
   }

   def getSkeletonUID(config:ProviderConfig, skeleton:Skeleton):String = config.identifier + "-" + skeleton.metaData.data.getOrElse(MetaData.KEY_SKELETON_ID,"-")
   def getSkeletonUID(config:ProviderConfig, identifier:String):String = config.identifier + "-" + identifier
}
