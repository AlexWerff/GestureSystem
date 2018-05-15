package com.alexwerff.modelbackend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.alexwerff.modelbackend.utils.IOUtils

import scala.io.StdIn

/**
  * Created by alexwerff on 22.08.17.
  */
class WebServer extends App {
   def start(host:String,port:Int)={
      implicit val system = ActorSystem("modelbackend")
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher


      val routes = WebServerRoutes.getRoutes(host,port)
      val bindingFuture = Http().bindAndHandle(routes, host, port)
      println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
         .flatMap(_.unbind()) // trigger unbinding from the port
         .onComplete(_ => system.terminate()) // and shutdown when done
   }
}
