package com.alexwerff.modelbackend.utils

import java.io.{BufferedWriter, File, FileWriter}
import java.net.InetAddress

import com.alexwerff.modelbackend.actor.model.GestureAnalysis
import com.alexwerff.modelbackend.data.{DataModel,Config}
import io.circe.Error
import io.circe.parser.decode

import scala.concurrent.Future
import scala.io.Source
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by alexwerff on 25.11.17.
  */
object IOUtils {
   def load[A](path:String)(decoder:String => Either[Error, A]):Future[A]= {
      Future{
         closeable(Source.fromFile(path)){
            source => {
               decoder(source.getLines().mkString).toOption match {
                  case Some(value) => value
                  case None => throw new Exception("Unable to decode json")
               }
            }
         }
      }
   }

   def save[A](path:String,value:A)(encoder:A =>String):Future[Boolean] = Future{
      closeable(new BufferedWriter(new FileWriter(new File(path)))){
         source =>{
            source.write(encoder(value))
            true
         }
      }
   }

   def loadConfigs(path:String) = load(path){
      json:String => {
         val res = decode[List[Config]](json)
         res
      }
   }
   def saveConfigs(path:String,value:List[Config]) = save(path,value){value => value.asJson.noSpaces}

   def loadModel(path:String):Future[DataModel] = load(path){
      json:String => {
         val res =decode[DataModel](json.replaceAll("\\p{C}",""))
         res
      }
   }
   def saveModel(path:String,value: DataModel):Future[Boolean] = save(path,value){value => value.asJson.noSpaces}

   def loadGestures(path:String)= load(path){json:String => decode[List[GestureAnalysis]](json)}
   def saveGestures(path:String,value:List[GestureAnalysis]) = save(path,value){ value => value.asJson.noSpaces}


   /**
     * Closes Resource after using it
     * @param resource
     * @param f function which interacts with the ressource
     * @tparam A generic which has to implement the function closes
     * @tparam B generic result
     * @return B generic
     */
   def closeable[A<:{def close()},B](resource:A)(f:A=>B)={
      try{
         f(resource)
      }finally {
         resource.close
      }
   }

   def getLocalIP():String={
      val inetAddr = InetAddress.getLocalHost
      val addr = inetAddr.getAddress
      // Convert to dot representation
      var ipAddr = ""
      var i = 0
      while (i < addr.length) {
         if (i > 0) ipAddr += "."
         ipAddr += addr(i) & 0xFF

         {
            i += 1; i - 1
         }
      }
      ipAddr
   }

}
