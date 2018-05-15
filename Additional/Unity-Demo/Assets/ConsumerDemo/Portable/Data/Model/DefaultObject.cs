using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace BAALLClient.Portable.Data.Model
{
    public class DefaultObject:ModelObject
    {
        public DefaultObject()
        {
            MetaData = new MetaData();
            Models = new Dictionary<string, ModelObject>();
            ModelProperties = ModelProperties.GetOrigin();
        }

		[JsonProperty(PropertyName = "metaData")]
        public MetaData MetaData
        {
            get;
            set;
        }

		[JsonProperty(PropertyName = "modelProperties")]
        public ModelProperties ModelProperties
        {
            get;
            set;
        }

		[JsonProperty(PropertyName = "models")]
        public Dictionary<String, ModelObject> Models
        {
            get;
            set;
        }
    }
}
