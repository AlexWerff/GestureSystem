using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Model
{
    public class DataModel
    {
        public DataModel()
        {
            Scenes = new Dictionary<string, Scene>();
            Prefabs = new List<Prefab>();
        }

		[JsonProperty(PropertyName = "scenes")]
        public Dictionary<String,Scene> Scenes{
            get;
            set;
        }

		[JsonProperty(PropertyName = "prefabs")]
        public List<Prefab> Prefabs{
            get;
            set;
        }

		public ModelObject FindObject(String identifier){
			foreach(var k in Scenes){
				if(k.Key == identifier){
					return k.Value;
				}
				else{
					return find(k.Value.Models, identifier);
				}
			}
			return null;
		}

		private ModelObject find(Dictionary<string, ModelObject> nestedDict,string identifier)
        {
			ModelObject result = null;
			if(nestedDict.ContainsKey(identifier)){
				nestedDict.TryGetValue(identifier, out result);
			}
			else{
				foreach (var key in nestedDict)
                {
                    var ret = find(key.Value.Models, identifier);
					if(ret != null){
						result = ret;
						break;
					}
                }
			}
			return result;
        }
    }
}
