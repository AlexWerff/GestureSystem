package com.alexwerff.modelbackend.data
/**
  * Created by alexwerff on 10.11.17.
  */


sealed trait ModelObject{
   val modelProperties: ModelProperties
   val models: Map[String, ModelObject]
   val metaData:MetaData
   def updateModels(newModels: Map[String, ModelObject]):ModelObject

   def mapModelRecursive(operation:((String,ModelObject)) => Option[(String,ModelObject)]):ModelObject=
      updateModels(models.map(p => (p._1,p._2.mapModelRecursive(operation))).flatMap(operation(_)))

}
sealed trait Stateful{
   val state:State
   def updateState(newState:State):Stateful
}
sealed trait Note{
   val name:String
   val content:String
}

case class DataModel(scenes: Map[String,Scene],prefabs:List[Prefab]) {
   def mapScenesRecursive(operation:((String,ModelObject)) => Option[(String,ModelObject)]):DataModel={
      val newScenes = scenes.map(f=>(f._1,f._2.mapModelRecursive(operation))).flatMap(operation(_)).map(f=>{
         (f._1,f._2.asInstanceOf[Scene])
      })
      DataModel(newScenes,prefabs).copy(scenes=newScenes)
   }

}

case class Prefab(name:String) {}
case class InfoNote(name:String,content:String) extends Note{}
case class DescriptionNote(name:String,content:String) extends Note{}
case class State(on:Boolean,value:Int){}
case class MetaData(data:Map[String,String] = Map()){}
case class ModelProperties(position:Vector[Float],orientation:Vector[Float],scale:Vector[Float]){}
case class Scene(models:Map[String,ModelObject],modelProperties: ModelProperties,metaData: MetaData) extends ModelObject{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = Scene(newModels,modelProperties,metaData)
}
case class DefaultObject(modelProperties: ModelProperties,models:Map[String,ModelObject],metaData: MetaData) extends ModelObject{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = DefaultObject(modelProperties,newModels,metaData)
}
case class PrefabObject(prefab:Prefab,modelProperties: ModelProperties,models:Map[String,ModelObject],metaData: MetaData) extends ModelObject{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = PrefabObject(prefab,modelProperties,newModels,metaData)
}

case class StateObject(prefab: Prefab,state:State,modelProperties: ModelProperties,models:Map[String,ModelObject],metaData: MetaData) extends ModelObject with Stateful{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = StateObject(prefab,state,modelProperties,newModels,metaData)
   def updateState(newState: State): Stateful = StateObject(prefab,newState,modelProperties,models,metaData)
}
case class SliderObject(state: State,modelProperties: ModelProperties,models:Map[String,ModelObject],controlModels:Seq[String],metaData: MetaData) extends ModelObject with Stateful{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = SliderObject(state,modelProperties,models,controlModels,metaData)
   override def updateState(newState: State): Stateful = SliderObject(newState,modelProperties,models,controlModels,metaData)
}

case class SwipeObject(modelProperties: ModelProperties,models:Map[String,ModelObject],metaData: MetaData) extends ModelObject{
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = SwipeObject(modelProperties,newModels,metaData)
}
case class NoteObject(note:Note,modelProperties: ModelProperties,models:Map[String,ModelObject],metaData: MetaData) extends ModelObject {
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = NoteObject(note,modelProperties,newModels,metaData)
}
case class Movement(speedMPS:Vector[Float],direction:Vector[Float]){}
case class Joint(x:Float,y:Float,z:Float,p:Float){}
case class Skeleton(joints:Map[String,Joint],models:Map[String,ModelObject],movement:Movement,metaData: MetaData) extends ModelObject{
   override val modelProperties: ModelProperties = ModelProperties.origin
   def get(name:String):Option[Joint]= joints.get(name)
   override def updateModels(newModels: Map[String, ModelObject]): ModelObject = Skeleton(joints,newModels,movement,metaData)
}
case class SkeletonGroup(skeletons:Map[String,Skeleton]){}

object MetaData{
   val KEY_NAME:String ="name"
   val KEY_HUE_ID:String = "hueID"
   val KEY_SKELETON_ID:String = "skeletonID"
   val KEY_INTERACTABLE:String = "interactable"
   val KEY_TIMESTAMP:String = "timestamp"
}
object Skeleton{
   val HEAD:String = "head"
   val NECK:String = "neck"
   val TORSO:String = "torso"
   val HIP_CENTER:String = "hip_center"
   val LEFT_COLLAR:String = "l_collar"
   val LEFT_HAND:String = "l_hand"
   val RIGHT_HAND:String = "r_hand"
   val LEFT_ELBOW:String = "l_elbow"
   val RIGHT_ELBOW:String = "r_elbow"
}
object ModelProperties{
   def origin:ModelProperties={
      ModelProperties(Vector(0.0f,0.0f,0.0f),Vector(0.0f,0.0f,0.0f),Vector(1.0f,1.0f,1.0f))
   }
}
object Note{
   val TYPE_INFO:String = "INFO"
   val TYPE_DESCRIPTION:String = "DESCRIPTION"
   val TYPE_MEMORY:String = "MEMORY"
}

object Movement{
   def default:Movement = Movement(Vector(0,0,0),Vector(0,0,0))
}

