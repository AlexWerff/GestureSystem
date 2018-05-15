using System;

namespace BAALLClient.Portable.Data.Request
{
    public class GetSkeletonConfigsRequest:IRequest
    {
        public GetSkeletonConfigsRequest()
        {
        }

        public RequestType GetRequestType()
        {
            return RequestType.GET;
        }

        public string GetUrl()
        {
            return "getSkeletonConfigs";
        }
    }
}
