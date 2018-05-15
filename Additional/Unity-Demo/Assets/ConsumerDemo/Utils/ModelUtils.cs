using System;
using System.Collections.Generic;
using BAALLClient.Portable.Data.Model;
using UnityEditor;
using UnityEngine;

namespace BAALLCLient.Utils
{
    public static class ModelUtils
    {
        /*public static Scene ExportModel(GameObject root, String sceneName)
        {
            Scene result = new Scene();
            result.Terrain = new BAALLClient.Portable.Data.Model.Terrain();
            fillModels(root,result.Models);
            return result;
        }

        private static void fillModels(GameObject current,Dictionary<String,ModelObject> currentModels){
            for (int i = 0; i < current.transform.childCount;i++){
                var child = current.transform.GetChild(i);
                var name = child.gameObject.name;
                var prefab = new PrefabObject();
                prefab.ModelProperties = propertiesFromTransform(child.transform);
                var pref= PrefabUtility.GetPrefabObject(child);
                Debug.Log(pref);
                prefab.Prefab = new Prefab()
                {
                    Name = pref != null ? pref.name: ""
                };
                currentModels.Add(name,prefab);
                fillModels(child.gameObject,prefab.Models);
            }
        }*/

        public static ModelProperties PropertiesFromTransform(Transform transform){
            return new ModelProperties()
            {
                Position = new List<float>(){
					transform.position.x,transform.position.y,transform.localPosition.z
                },
                Orientation = new List<float>()
                {
                    transform.eulerAngles.x,transform.eulerAngles.y,transform.eulerAngles.z
                },
                Scale = new List<float>()
                {
                    transform.localScale.x,transform.localScale.y,transform.localScale.z
                }
            };
        }

		public static bool UpdateLight(GameObject gameObject,State state){
			try{
				var spotlight = gameObject.transform.GetChild(0).GetChild(1);
                var light = spotlight.GetComponent<Light>();
                light.spotAngle = state.Value;
                light.enabled = state.On;
				return true;
			}catch(Exception ex){
				return false;
			}

		}

/*
        public static DataModel ModelFromJSON(String json){
            return new DataModel();
        }*/
    }
}
