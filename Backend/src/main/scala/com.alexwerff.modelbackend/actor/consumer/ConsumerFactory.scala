package com.alexwerff.modelbackend.actor.consumer

import akka.actor.{ActorContext, ActorRef}
import com.alexwerff.modelbackend.data._

/**
  * Created by alexwerff on 16.03.18.
  */
object ConsumerFactory {
   def configToActor:PartialFunction[(Config,ActorContext),ActorRef]= {
      case (config:AlexaConsumerConfig,context) => context.actorOf(AlexaConsumerActor.props())
      case (config:PhillipsHueConsumerConfig,context) => context.actorOf(PhillipsHueConsumerActor.props())
   }
}
