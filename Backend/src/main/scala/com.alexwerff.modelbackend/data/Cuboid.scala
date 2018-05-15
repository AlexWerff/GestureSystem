package com.alexwerff.modelbackend.data

/**
  * Created by alexwerff on 30.03.18.
  */

import com.alexwerff.modelbackend.data.VectorImprovements.OperationVector


case class Quaternion(x: Float, y: Float, z: Float, w: Float){}

object Quaternion {
   val identity = Quaternion(0, 0, 0, 1)
   def forAxisAngle(axis: Vector[Float], angle: Float): Quaternion = {
      val halfAngle = angle / 2
      val s = Math.sin(halfAngle).toFloat
      Quaternion(axis.x * s, axis.y * s, axis.z * s, scala.math.cos(halfAngle).toFloat)
   }
}

object Orientation {
   val x = Vector[Float](1, 0, 0)
   val y = Vector[Float](0, 1, 0)
   val z = Vector[Float](0, 0, 1)
}

case class Cuboid(position:Vector[Float],scale:Vector[Float],orientation: Vector[Float]){
   def getBounds():List[Vector[Float]]={
      val p1 = Vector[Float](position.x + (scale.x/2),position.y - (scale.y/2),position.z - (scale.z/2))
      val p2 = Vector[Float](position.x - (scale.x/2),position.y - (scale.y/2),position.z - (scale.z/2))
      val p3 = Vector[Float](position.x - (scale.x/2),position.y - (scale.y/2),position.z + (scale.z/2))
      val p4 = Vector[Float](position.x + (scale.x/2),position.y - (scale.y/2),position.z + (scale.z/2))
      val p5 = Vector[Float](position.x + (scale.x/2),position.y + (scale.y/2),position.z - (scale.z/2))
      val p6 = Vector[Float](position.x - (scale.x/2),position.y + (scale.y/2),position.z - (scale.z/2))
      val p7 = Vector[Float](position.x - (scale.x/2),position.y + (scale.y/2),position.z + (scale.z/2))
      val p8 = Vector[Float](position.x + (scale.x/2),position.y + (scale.y/2),position.z + (scale.z/2))
      List(p1,p2,p3,p4,p5,p6,p7,p8).map(_.rotateByOrigin(position,orientation))
   }

   def getPlanes():List[Plane]={
      val bounds = getBounds()
      val front = Plane(bounds(4),bounds(7),bounds(0))
      val back = Plane(bounds(5),bounds(6),bounds(1))
      val left = Plane(bounds(4),bounds(5),bounds(0))
      val right = Plane(bounds(7),bounds(6),bounds(3))
      val bottom = Plane(bounds(0),bounds(1),bounds(3))
      List(front,back,left,right)
   }

   def rotateSelf(orientationDeg: Vector[Float]):Cuboid = Cuboid(position,scale,orientationDeg + orientation)

   def rotateAroundOrigin(origin:Vector[Float],orientationDeg: Vector[Float]):Cuboid=
      Cuboid(position.rotateByOrigin(origin,orientationDeg),scale,orientation)

   def scale (newScale:Vector[Float]):Cuboid= Cuboid(position,scale * newScale,orientation)

   def scale (f:Float):Cuboid= Cuboid(position,scale * f,orientation)

   def containsPoint(point:Vector[Float]):Boolean={
      val bounds = getBounds()
      val u = (bounds(0) - bounds(1))//(bounds(0) - bounds(3)).cross((bounds(0) - bounds(4)))
      val v = (bounds(0) - bounds(3))//(bounds(0) - bounds(1)).cross((bounds(0) - bounds(4)))
      val w = (bounds(0) - bounds(4))//(bounds(0) - bounds(1)).cross((bounds(0) - bounds(3)))

      val xU = u.dot(point)
      val xV = v.dot(point)
      val xW = w.dot(point)

      //println(point) //TODO


      xU < u.dot(bounds(0)) && xU > u.dot(bounds(1)) &&
         xV < v.dot(bounds(0)) && xV > v.dot(bounds(3)) &&
         xW < w.dot(bounds(0)) && xW > w.dot(bounds(4))
   }

   def lineIntersection(line:Line):List[(Plane,Vector[Float])] ={
      getPlanes().flatMap(plane => {
         val dP1P2 =  plane.p2 -plane.p1
         val dP1P3 = plane.p3 - plane.p1
         val n = dP1P2.cross(dP1P3)
         val dL = line.getDirection()

         if(Math.abs(n dot dL) < 1e-6f) None
         else{
            val t = ((n * -1.0f) dot (line.start - plane.p1)) / (n dot dL)
            val iP = line.start + (dL * t)
            val u = (iP - plane.p1) dot dP1P2
            val v = (iP - plane.p1) dot dP1P3
            if( (u >= 0.0f && u <= (dP1P2 dot dP1P2)) && (v >= 0.0f && v <= (dP1P3 dot dP1P3))) Some(plane,iP) else None
         }
      })
   }

   def intersectsWithLine(line:Line):Boolean = lineIntersection(line).nonEmpty
}

object Cuboid{
   def apply(modelProperties: ModelProperties): Cuboid =
      Cuboid(modelProperties.position,modelProperties.scale,modelProperties.orientation)
}


case class Line(start:Vector[Float],end:Vector[Float]){
   def getDirection():Vector[Float] = end - start
}

case class Plane(p1:Vector[Float],p2:Vector[Float],p3:Vector[Float]){

}



object VectorImprovements{
   implicit class OperationVector(vector: Vector[Float]){
      val x:Float = vector(0)
      val y:Float = vector(1)
      val z:Float = vector(2)

      def applyToAll(v: Vector[Float],f:(Float,Float) => Float):Vector[Float] = vector.zip(v).map(p=> f(p._1,p._2))

      def + (v: Vector[Float]):Vector[Float] = applyToAll(v,_+_)
      def - (v: Vector[Float]):Vector[Float] = applyToAll(v,_-_)
      def / (v: Vector[Float]):Vector[Float] = applyToAll(v,_/_)
      def / (v:Float):Vector[Float] = applyToAll(Vector[Float](v,v,v),_/_)
      def * (v: Vector[Float]):Vector[Float] = applyToAll(v,_*_)
      def * (v: Float):Vector[Float] = applyToAll(Vector[Float](v,v,v),_*_)
      def dot (b: Vector[Float]):Float = x * b.x + y * b.y + z * b.z
      def cross(b: Vector[Float]):Vector[Float] =  Vector[Float](y * b.z - z * b.y, z - b.x - x * b.z, x - b.y - y * b.x)
      def distanceTo(b: Vector[Float]):Float = Math.sqrt(Math.pow(x - b.x,2) + Math.pow(y - b.y, 2) + Math.pow(z - b.z, 2)).toFloat


      def rotate(rotationDeg:Vector[Float]):Vector[Float] ={
         val qY = Quaternion.forAxisAngle(Orientation.y, scala.math.toRadians(rotationDeg.y).toFloat) //*-1 because Y is inverted
         val qX = Quaternion.forAxisAngle(Orientation.x, scala.math.toRadians(rotationDeg.x).toFloat)
         val qZ = Quaternion.forAxisAngle(Orientation.z, scala.math.toRadians(rotationDeg.z).toFloat)
         rotate(qY).rotate(qX)
      }
      def rotate(q: Quaternion): Vector[Float] = {
         val ix =  q.w * x + q.y * z - q.z * y
         val iy =  q.w * y + q.z * x - q.x * z
         val iz =  q.w * z + q.x * y - q.y * x
         val iw = - q.x * x - q.y * y - q.z * z

         Vector[Float](
            ix * q.w + iw * - q.x + iy * - q.z - iz * - q.y,
            iy * q.w + iw * - q.y + iz * - q.x - ix * - q.z,
            iz * q.w + iw * - q.z + ix * - q.y - iy * - q.x
         )
      }
      def rotateByOrigin(origin:Vector[Float],orientation:Vector[Float]): Vector[Float] = ((vector - origin) rotate orientation) + origin
   }
}