using Newtonsoft.Json;
using System;
using System.Collections.Generic;


namespace BAALLClient.Portable.Data.Model
{

    
    public interface ModelObject
    {

		[JsonProperty(PropertyName = "models")]
        [JsonConverter(typeof(ModelConverter))]
        Dictionary<String,ModelObject> Models{
            get;
            set;
        }

		[JsonProperty(PropertyName = "modelProperties")]
        ModelProperties ModelProperties{
            get;
            set;
        }

		[JsonProperty(PropertyName = "metaData")]
        MetaData MetaData
        {
            get;
            set;
        }
    }
}
