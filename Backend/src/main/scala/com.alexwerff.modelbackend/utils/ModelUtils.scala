package com.alexwerff.modelbackend.utils

import com.alexwerff.modelbackend.data._

import scala.util.{Try}

object ModelUtils {
   def findModelByIdentifier(models:List[(String,ModelObject)],identifier: String):Option[ModelObject]={
      models.find(p=> p._1 == identifier) match{
         case Some((_,v:ModelObject)) => Some(v)
         case None => models.flatMap(f=>findModelByIdentifier(f._2.models.toList,identifier)).headOption
      }
   }

   def deleteModel(dataModel: DataModel,identifier:String):Try[DataModel]= {
      Try{
         dataModel.mapScenesRecursive{
            case (id:String,_) if id == identifier => None
            case p => Some(p)
         }
      }
   }

   def updateModel(dataModel: DataModel,parentIdentifier:String,modelObject: (String,ModelObject)):Try[DataModel]={
      Try{
         findModelByIdentifier(dataModel.scenes.toList,modelObject._1) match{
            case Some(v)=> dataModel.mapScenesRecursive{
               case (id:String,model) if id == modelObject._1 => Some((id,modelObject._2))
               case p => Some(p)
            }
            case None => dataModel.mapScenesRecursive{
               case (id:String,model) if id == parentIdentifier => Some((id,model.updateModels(model.models ++ Map(modelObject))))
               case p => Some(p)
            }
         }
      }
   }


}
