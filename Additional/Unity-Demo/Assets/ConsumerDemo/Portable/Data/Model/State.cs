using Newtonsoft.Json;

public class State
{
	[JsonProperty(PropertyName = "on")]
    public bool On { get; set; }
	[JsonProperty(PropertyName = "value")]
    public float Value { get; set; }
}