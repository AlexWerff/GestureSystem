using System;
using BAALLClient.Portable.Data.Model;

namespace BAALLClient.Portable.Data.Response
{
    public class ObjectResponse : IResponse
    {
		public ObjectResponse()
        {
        }

        public String Identifier
        {
            get;
            set;
        }

        public ModelObject Content{
			get;
			set;
		}
    }
}
