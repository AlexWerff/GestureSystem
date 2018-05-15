using System;
using System.Collections;
using System.Collections.Generic;
using BAALLClient.Portable.Data.Model;
using BAALLClient.Portable.Data.Request;
using BAALLClient.Portable.Data.Response;
using BAALLClient.Unity;
using BAALLCLient.Utils;
using UnityEngine;

public class SceneManager : MonoBehaviour {

    private const float REFRESH_TIMEOUT_SECONDS = 20.0f; 
    private GameObject rootObject;
    public GameObject SkeletonManagerPrefab;
    public GameObject EventManagerPrefab;
    public Camera MainCamera;
    
    private float currentTime;

    private Dictionary<String, GameObject> scenes;
	private Dictionary<String, GameObject> gameModelObjects;

	// Use this for initialization
	void Start () {
        rootObject = new GameObject("Scenes");
        rootObject.transform.SetParent(this.gameObject.transform);
        scenes = new Dictionary<string, GameObject>();
		gameModelObjects = new Dictionary<string, GameObject>();
		Debug.Log("LOADING FROM" + ConstantValues.BASE_URL);
        APIManager.Instance.Connect(ConstantValues.REST_URL);
        fetchModel();
	}

    private void fetchModel(){
        Debug.Log("Fetching new model");
        APIManager.Instance.SendRequest(new GetModelRequest(), (response) => {
			var modelResponse = response as ModelResponse;
            if (modelResponse != null)
            {
                DataModel = modelResponse.Model;
				Debug.Log(DataModel.Scenes.Keys.Count);
				foreach(var key in DataModel.Scenes.Keys){
                    try
                    {
                        renderModel(key);
                    }
                    catch(Exception ex)
                    {
                        Debug.LogError(ex);
                    }
                }
            }
        });
    }
	
	// Update is called once per frame
	void Update () {
        if(currentTime > REFRESH_TIMEOUT_SECONDS){
            //fetchModel();
            currentTime = 0;
        }
        currentTime += Time.deltaTime;
	}

    private void renderModel(String sceneKey){
        if (scenes.ContainsKey(sceneKey))
        {
            GameObject go = null;
            scenes.TryGetValue(sceneKey, out go);
            Destroy(go);
            scenes.Remove(sceneKey);
        }
        Scene scene = null;
		DataModel.Scenes.TryGetValue(sceneKey,out scene);
        var rendered = new GameObject(scene.MetaData.GetValue("name"));
        rendered.transform.SetParent(rootObject.transform);
        applyModelProperties(scene.ModelProperties,rendered);
        renderModelObject(scene.Models, rendered);

        GameObject smObject = Instantiate(SkeletonManagerPrefab,rendered.transform);
        smObject.name = "SkeletonManager";
        var skeletonManager = smObject.GetComponent<SkeletonManager>();
        skeletonManager.RootObject = rendered;
        GameObject emObject = Instantiate(EventManagerPrefab, rendered.transform);
        var eventManager = emObject.GetComponent<EventManager>();
        eventManager.SceneManager = this;
		emObject.name = "EventManager";
    }

    private List<GameObject> renderModelObject(Dictionary<String,ModelObject> models,GameObject parentGo){
        var result = new List<GameObject>();
        foreach (var modelObject in models)
        {
            try
            {
                var go = createModelObject(modelObject.Key, modelObject.Value, parentGo);
                renderModelObject(modelObject.Value.Models, go);
                result.Add(go);
             
				if(modelObject.Value is StateObject){
					ModelUtils.UpdateLight(go, ((StateObject)modelObject.Value).State);
				}

				gameModelObjects.Add(modelObject.Key, go);
            }
            catch (Exception ex)
            {
                Debug.LogError(ex);
            }
        }
        return result;
    }

    private GameObject createModelObject(String key, ModelObject modelObject,GameObject parent){
        if(modelObject is PrefabObject){
            GameObject go = Instantiate(Resources.Load(((PrefabObject)modelObject).Prefab.Name)) as GameObject;
            go.name = modelObject.MetaData.GetValue("name");
            go.transform.SetParent(parent.transform);
            applyModelProperties(modelObject.ModelProperties, go);
            return go;
        }
        if(modelObject is NoteObject)
        {
            GameObject go = Instantiate(Resources.Load("Note")) as GameObject;
            go.name = modelObject.MetaData.GetValue("name");
            go.transform.SetParent(parent.transform);
            applyModelProperties(modelObject.ModelProperties, go);
            return go;
        }
        if (modelObject is SliderObject)
        {
            GameObject go = Instantiate(Resources.Load("Slider")) as GameObject;
            go.name = modelObject.MetaData.GetValue("name");
            go.transform.SetParent(parent.transform);
            applyModelProperties(modelObject.ModelProperties, go);
            return go;
        }
        if (modelObject is StateObject)
        {
			GameObject go = Instantiate(Resources.Load(((StateObject)modelObject).Prefab.Name)) as GameObject;
			go.name = modelObject.MetaData.GetValue("name");
            go.transform.SetParent(parent.transform);
            applyModelProperties(modelObject.ModelProperties, go);
            return go;
        }
        else
        {
            GameObject go = new GameObject(modelObject.MetaData.GetValue("name"));
            go.transform.SetParent(parent.transform);
            applyModelProperties(modelObject.ModelProperties, go);
            return go;
        }
    }

	public GameObject GameObjectForID(String identifier){
		GameObject result = null;
		gameModelObjects.TryGetValue(identifier, out result);
		return result;
	}

    private void applyModelProperties(ModelProperties properties,GameObject gameObject){
		Debug.Log(JsonUtility.ToJson(properties.Orientation));
        gameObject.transform.localPosition = new Vector3(properties.Position[0], properties.Position[1], properties.Position[2]);
        gameObject.transform.eulerAngles = new Vector3(properties.Orientation[0], properties.Orientation[1], properties.Orientation[2]);
        gameObject.transform.localScale = new Vector3(properties.Scale[0], properties.Scale[1], properties.Scale[2]);
    }

	public DataModel DataModel{
		get;
		private set;
	}
}
