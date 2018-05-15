using System;
using System.Collections.Generic;
using BAALLClient.Portable.Data.Model;
using Newtonsoft.Json;
using UnityEngine;

namespace BAALLClient.Unity.Utils
{

    public static class JSONParser
    {
        public static JsonSerializerSettings ModelSettings = new JsonSerializerSettings()
        {
            ReferenceLoopHandling = ReferenceLoopHandling.Ignore,
            NullValueHandling = NullValueHandling.Ignore,
            Converters = new JsonConverter[] {}
        };

        public static String ObjectToJSON<T>(T obj){
            var className = typeof(T).Name;
            var json = "{\"" + className + "\":";
            json += JsonConvert.SerializeObject(obj, ModelSettings);
            json += "}";
            return json;
        }


        public static T JSONToObject<T>(String json)
        {
            Type listType = extractTypeFromJSON(ref json);
            T obj = (T)JsonConvert.DeserializeObject(json, listType, ModelSettings);
            return obj;
        }

        private static Type extractTypeFromJSON(ref String json){
			json = json == null ? "" : json.Replace(" ","").Replace("\n","");
            var startIndex = json.IndexOf("{");           
            var endIndex = json.IndexOf(":");

            var type = json.Substring(startIndex+2,endIndex-3);
            json = json.Substring(endIndex+1,json.Length-2-endIndex);
            Debug.Log(json);

            return Type.GetType("BAALLClient.Portable.Data.Response."+type);
        }
    }
}


public class ModelConverter : JsonConverter
{
    public override bool CanConvert(Type objectType)
    {
        return objectType == typeof(ModelObject);
    }

    public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
    {
        var result = serializer.Deserialize<Dictionary<String,TempObject>>(reader);
        var parsed = new Dictionary<String, ModelObject>();
        foreach(var value in result)
        {
            if (value.Value.PrefabObject != null)
            {
                parsed.Add(value.Key,value.Value.PrefabObject);
            }
            if(value.Value.NoteObject != null)
            {
                parsed.Add(value.Key, value.Value.NoteObject);
            }
            if (value.Value.StateObject != null)
            {
                parsed.Add(value.Key, value.Value.StateObject);
            }
            if (value.Value.SliderObject != null)
            {
                parsed.Add(value.Key, value.Value.SliderObject);
            }
            if (value.Value.DefaultObject != null)
            {
                parsed.Add(value.Key, value.Value.DefaultObject);
            }
        }
        return parsed;
    }

    public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
    {
        var dict = new Dictionary<String, TempObject>();
        var oldDict = value as Dictionary<String,ModelObject>;
        foreach(var val in oldDict)
        {
            var temp = new TempObject();
            temp.PrefabObject = val.Value as PrefabObject;
            temp.DefaultObject = val.Value as DefaultObject;
            dict.Add(val.Key, temp);
        }
        serializer.Serialize(writer, dict);
    }

    class TempObject
    {
        public PrefabObject PrefabObject
        {
            get;
            set;
        }

        public DefaultObject DefaultObject
        {
            get;
            set;
        }

        public StateObject StateObject
        {
            get;
            set;
        }

        public NoteObject NoteObject
        {
            get;
            set;
        }

        public SliderObject SliderObject
        {
            get;
            set;
        }
    }
}


