using System;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Request
{
    public class PostSkeletonConfigRequest:IRequest
    {
        public PostSkeletonConfigRequest()
        {
        }

        public string GetUrl()
        {
            return "postSkeletonConfig";
        }

        RequestType IRequest.GetRequestType()
        {
            return RequestType.POST;
        }

        [JsonProperty(PropertyName = "config")]
        public SkeletonProviderConfig Config{
            get;
            set;
        }
    }
}
