package com.alexwerff.modelbackend.data

/**
  * Created by alexwerff on 17.11.17.
  */
sealed trait SpeechModel {

}

case class SpeechInput(speechInput:String,tag:String,probability:Float) extends SpeechModel{}
case class SpeechOutput(speechOutput:String) extends SpeechModel{}
