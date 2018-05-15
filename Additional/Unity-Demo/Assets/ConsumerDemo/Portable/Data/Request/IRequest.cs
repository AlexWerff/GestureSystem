using System;
namespace BAALLClient.Portable.Data.Request
{
    public interface IRequest
    {
        String GetUrl();
        RequestType GetRequestType();
    }
}
