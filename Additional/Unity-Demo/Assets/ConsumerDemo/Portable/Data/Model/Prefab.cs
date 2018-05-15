using System;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Model
{
    public class Prefab
    {
        public Prefab()
        {
            Name = "";
        }

		[JsonProperty(PropertyName = "name")]
        public String Name{
            get;
            set;
        }
    }
}
