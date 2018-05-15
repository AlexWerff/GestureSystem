using BAALLClient.Portable.Data.Model;
using System.Collections.Generic;

public class NoteObject : ModelObject
{
    public MetaData MetaData{get;set;}
    public Note Note { get; set; }
    public Dictionary<string, ModelObject> Models { get; set; }
    public ModelProperties ModelProperties { get; set; }
}