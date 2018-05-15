using System;
namespace BAALLClient.Portable.Data.Request
{
	public class GetObjectRequest: IRequest{
		public GetObjectRequest()
        {
			Identifier = "";
        }

		public String Identifier{
			get;
			set;
		}

		public RequestType GetRequestType()
		{
			return RequestType.POST;
		}

		public string GetUrl()
		{
			return "getObject";
		}
	}
   
}