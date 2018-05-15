using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace BAALLClient.Portable.Data.Model
{
    public class Scene:ModelObject
    {
        public Scene()
        {
            Models = new Dictionary<string, ModelObject>();
            ModelProperties = ModelProperties.GetOrigin();
            MetaData = new MetaData();
        }

		[JsonProperty(PropertyName = "metaData")]
        public MetaData MetaData { get; set; }

		[JsonProperty(PropertyName = "models")]
        [JsonConverter(typeof(ModelConverter))]
        public Dictionary<String, ModelObject> Models{
            get;
            set;
        }

		[JsonProperty(PropertyName = "modelProperties")]
        public ModelProperties ModelProperties
        {
            get;
            set;
        }
    }
}
