using System;
namespace BAALLClient.Portable.Data.Request
{
    public class GetModelRequest:IRequest
    {
        public GetModelRequest()
        {
        }

        public RequestType GetRequestType()
        {
            return RequestType.GET;
        }

        public string GetUrl()
        {
            return "getModel";
        }
    }
}
