using System;
using BAALLClient.Portable.Data.Request;
using BAALLClient.Portable.Data.Response;
using BAALLClient.Unity.Utils;
using CI.HttpClient;
using Newtonsoft.Json;
using UnityEngine;

namespace BAALLClient.Unity
{
    public class APIManager
    {
        private static APIManager instance;

        private HttpClient client;

        private APIManager()
        {
            client = new HttpClient();
        }
        
        public void SendRequest(IRequest request,Action<IResponse> OnResponse = null)
        {
            var fullUrl = String.Format("{0}/{1}", Url, request.GetUrl());
            Debug.Log(fullUrl);
            if (request.GetRequestType() == RequestType.GET)
            {
                var json = JsonConvert.SerializeObject(request);
                client.GetString(new Uri(fullUrl),(result) => {
                    Debug.Log(result.Data);
                    IResponse resp = JSONParser.JSONToObject<IResponse>(result.Data);
                    if(OnResponse != null){
                        OnResponse.Invoke(resp);
                    }
                });
            }
            if (request.GetRequestType() == RequestType.POST)
            {
                var json = JsonConvert.SerializeObject(request);
                client.Post(new Uri(fullUrl),new StringContent(json),(result) => {
                    Debug.Log(result.Data);
					IResponse resp = JSONParser.JSONToObject<IResponse>(result.Data);
                    if (OnResponse != null)
                    {
                        OnResponse.Invoke(resp);
                    }
                });
            }
        }

        public static APIManager Instance
        {
            get
            {
                if (instance == null)
                {
                    instance = new APIManager();
                }
                return instance;
            }
        }


        public void Connect(String url){
            Url = url;
        }


        public String Url{
            get;
            private set;
        }
    }
}
