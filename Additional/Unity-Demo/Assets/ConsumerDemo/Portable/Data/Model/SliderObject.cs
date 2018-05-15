using BAALLClient.Portable.Data.Model;
using Newtonsoft.Json;
using System.Collections.Generic;

public class SliderObject : ModelObject
{
    public SliderObject(){
        MetaData = new MetaData();
        Models = new Dictionary<string, ModelObject>();
    }
	[JsonProperty(PropertyName = "note")]
    public Note Note { get; set; }
	[JsonProperty(PropertyName = "models")]
    public Dictionary<string, ModelObject> Models { get; set; }
	[JsonProperty(PropertyName = "modelProperties")]
    public ModelProperties ModelProperties { get; set; }
	[JsonProperty(PropertyName = "metaData")]
    public MetaData MetaData { get; set; }
}
