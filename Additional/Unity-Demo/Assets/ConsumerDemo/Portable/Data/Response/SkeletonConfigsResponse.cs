using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Response
{
    public class SkeletonConfigsResponse:IResponse
    {
        public SkeletonConfigsResponse()
        {
        }

        [JsonProperty(PropertyName = "configs")]
        public List<SkeletonProviderConfig> Configs{
            get;
            set;
        }
    }
}
