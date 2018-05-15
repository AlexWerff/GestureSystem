using BAALLClient.Portable.Data.Model;
using Newtonsoft.Json;
using System.Collections.Generic;

public class StateObject : ModelObject
{
	[JsonProperty(PropertyName = "metaData")]
    public MetaData MetaData { get; set; }
	[JsonProperty(PropertyName = "prefab")]
    public Prefab Prefab{
        get;
        set;
    } 
	[JsonProperty(PropertyName = "state")]
    public State State { get; set; }
	[JsonProperty(PropertyName = "models")]
    public Dictionary<string, ModelObject> Models { get; set; }
	[JsonProperty(PropertyName = "modelProperties")]
    public ModelProperties ModelProperties { get; set; }
}
