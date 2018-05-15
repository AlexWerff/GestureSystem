using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Model
{
    public class ModelProperties
    {
        public ModelProperties()
        {
            Position = new List<float>();
            Orientation = new List<float>();
            Scale = new List<float>();
        }

        public static ModelProperties GetOrigin()
        {
            return new ModelProperties()
            {
                Position = new List<float>() {},
                Orientation = new List<float>() { },
                Scale = new List<float>() { },
            };
        }

		[JsonProperty(PropertyName = "position")]
        public List<float> Position{
            get;
            set;
        }

		[JsonProperty(PropertyName = "orientation")]
        public List<float> Orientation
        {
            get;
            set;
        }

		[JsonProperty(PropertyName = "scale")]
        public List<float> Scale
        {
            get;
            set;
        }
    }


}
