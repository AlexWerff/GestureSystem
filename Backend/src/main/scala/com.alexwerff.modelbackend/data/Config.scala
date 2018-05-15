package com.alexwerff.modelbackend.data

/**
  * Created by alexwerff on 21.11.17.
  */
sealed trait Config{
   val identifier:String
}
sealed trait ConsumerConfig extends Config{
   val name:String
   val address:String
   val port:Int
   def updateIdentifier(newIdentifier:String):ConsumerConfig
}
case class AlexaConsumerConfig(identifier:String,name:String,address:String,port:Int) extends ConsumerConfig{
   override def updateIdentifier(newIdentifier: String): ConsumerConfig =
      AlexaConsumerConfig(newIdentifier,name,address,port)
}
case class PhillipsHueConsumerConfig(identifier:String,name:String,address:String,port:Int,username:String) extends ConsumerConfig{
   override def updateIdentifier(newIdentifier: String): ConsumerConfig =
      PhillipsHueConsumerConfig(newIdentifier,name,address,port,username)
}


case class ProviderConfig(identifier:String,
                          modelIdentifier:String,
                          remoteAddress: String,
                          port:Int,
                          sceneIdentifier:String,
                          providerTypes:Seq[String]) extends Config{
   def updateIdentifier(newIdentifier:String):ProviderConfig =
      ProviderConfig(newIdentifier,modelIdentifier,remoteAddress,port,sceneIdentifier,providerTypes)
}