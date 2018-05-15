using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Model
{
    public class MetaData
    {
        public MetaData()
        {
            Data = new Dictionary<string, string>();
        }

		[JsonProperty(PropertyName = "data")]
        public Dictionary<String,String> Data { get; set; }

        public String GetValue(String key){
            var result = "";
            Data.TryGetValue(key,out result);
            return result;
        }
    }
}
