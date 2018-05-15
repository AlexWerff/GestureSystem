package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.data._
import io.circe.Json
import io.circe.parser.parse

import scala.collection.immutable.HashMap

/**
  * Created by alexwerff on 21.09.17.
  */
object JSONParser {

   def decodeOscJson(json:String):Option[Any] ={
      parse(json).toOption match{
         case Some(document)=> document.asArray match{
            case Some(Vector(name,identifier)) if name.asString.getOrElse("-").equals("/lost_skeleton") => {
               Some(SkeletonLostEvent(identifier.asString.getOrElse("-")))
            }
            case Some(Vector(name,identifier)) if name.asString.getOrElse("-").equals("/new_skeleton") => {
               Some(SkeletonNewEvent(identifier.asString.getOrElse("-")))
            }
            case Some(Vector(name,joints)) if name.asString.getOrElse("-").equals("/skeleton") => joints.asArray.flatMap(jointArray => decodeSkeleton(jointArray))
            case Some(Vector(name,a,b,c)) if name.asString.getOrElse("-").equals("/speech") => decodeSpeechInput(document)
            case None => None
         }
         case None => None
      }
   }

   def decodeSkeleton(jointArray:Vector[Json]):Option[Skeleton]={
      val skeletonID = jointArray.flatMap(
         joint =>joint.asArray.flatMap(
            jointEntry => Some(jointEntry.zipWithIndex.filter(f=>f._2 == 3).map(f => f._1.asString).reduceLeft((a,_)=> a))
         )
      ).reduceLeft((a,b)=> b)
      val joints = jointArray.flatMap {
         joint: Json => joint.asArray.flatMap(
            jointEntries => jointEntries.map(f=> f.asString.getOrElse("")) match{
               case Vector("/osceleton2/joint",n,_,_,x,y,z,p,_) => Some(n,Joint(x.toFloat,y.toFloat,z.toFloat,p.toFloat))
               case _ => None
            }
         )
      }.toMap
      (skeletonID,joints) match{
         case (None,_) => None
         case (_,j) if j.size == 0 => None
         case (Some(skeletonID),joints) => {
            val time = System.currentTimeMillis().toString
            val meta = MetaData(HashMap((MetaData.KEY_SKELETON_ID,skeletonID),(MetaData.KEY_TIMESTAMP,time)))
            Some(Skeleton(joints,Map(),Movement.default,meta))
         }
      }
   }



   def decodeSpeechInput(document:Json):Option[SpeechInput]={
      document.asArray.flatMap(
         document => document.map(f=>f.asString.getOrElse("")) match {
            case Vector(_,value,tag,p) => {
               Some(SpeechInput (value,tag, p.toFloat))
            }
            case v => {
               None
            }
         }
      )
   }


}
