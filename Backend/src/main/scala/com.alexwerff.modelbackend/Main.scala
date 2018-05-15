package com.alexwerff.modelbackend

import com.alexwerff.modelbackend.WebServer
import com.alexwerff.modelbackend.utils.IOUtils


/**
  * Created by alexwerff on 22.08.17.
  */
object Main {
   def main(args:Array[String]):Unit={
      val webServer:WebServer = new WebServer()
      webServer.start(IOUtils.getLocalIP(),8080)
   }
}
