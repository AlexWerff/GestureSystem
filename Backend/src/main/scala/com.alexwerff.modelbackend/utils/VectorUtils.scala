package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.data.VectorImprovements.OperationVector

/**
  * Created by alexwerff on 08.11.17.
  */
object VectorUtils {

   def getDirection(a:Vector[Float],b:Vector[Float]):Vector[Float] = b - a

   def getSpeed(distance:Float,deltaTime:Float):Float = (distance,deltaTime) match{
      case (0,0) => 0
      case (_,0) => 0
      case (0,_) => 0
      case (m,d) => m / d
   }

   def getSpeed(a:Vector[Float],b:Vector[Float],deltaTime:Float):Vector[Float] = (a-b).map(getSpeed(_,deltaTime))

   def getDistanceSpeed(a:Vector[Float],b:Vector[Float],deltaTime:Float):Float = getSpeed(a.distanceTo(b),deltaTime)

}

