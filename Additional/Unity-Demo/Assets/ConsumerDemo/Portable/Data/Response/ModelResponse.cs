using System;
using BAALLClient.Portable.Data.Model;

namespace BAALLClient.Portable.Data.Response
{
    public class ModelResponse:IResponse
    {
        public ModelResponse()
        {
        }

        public DataModel Model{
            get;
            set;
        }
    }
}
