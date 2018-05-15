using UnityEngine;
using System;
using BAALLClient.Portable.Data;
using Newtonsoft.Json;
using System.Collections.Generic;
using BAALLClient.Unity;
using BAALLClient.Portable.Data.Model;
using BAALLClient.Portable.Data.Events;

public class SkeletonManager : MonoBehaviour {

    public GameObject RootObject;
	public GameObject SkeletonPrefab;
    public Dictionary<String,GameObject> Kinects = new Dictionary<string, GameObject>();

	private Dictionary<string,GameObject> skeletons;
    private SocketManager socketManager;



	// Use this for initialization
	void Start () {
        SkeletonPrefab = Resources.Load("Human") as GameObject;
        Debug.Log(SkeletonPrefab != null);
        socketManager = new SocketManager();
        var ugly = UnityThreadHelper.Dispatcher;
		skeletons = new Dictionary<string, GameObject> ();
        socketManager.MessageRecieved += (message) => {
            UnityThreadHelper.Dispatcher.Dispatch(() =>
            {
                processMessage(message);
            });
        };
        socketManager.Connect(ConstantValues.SKELETONSOCKET_URL);
	}

	void OnDestroy()
	{
		Debug.Log("DESTROYED");
		UnityThreadHelper.Dispatcher.Dispose();
		socketManager.Dispose();
		socketManager = null;
	}

	private void processMessage(String json){
        var skeletonEvent = JsonConvert.DeserializeObject <BAALLClient.Portable.Data.Event>(json);
        if(skeletonEvent.SkeletonUpdatedEvent != null){
            var skeleton = skeletonEvent.SkeletonUpdatedEvent.skeleton;
            if (!skeletons.ContainsKey(skeleton.SkeletonID))
            {
                BAALLClient.Portable.Data.Model.Joint headJoint = skeleton.GetJoint(Skeleton.HEAD);
                Debug.Log(JsonConvert.SerializeObject(skeletonEvent));
                Debug.Log(headJoint);
                var skeletonObject = Instantiate(SkeletonPrefab, new Vector3(headJoint.x, headJoint.y, headJoint.z), Quaternion.Euler(new Vector3(0, 0, 0)));
                skeletonObject.transform.SetParent(RootObject.transform,false);
				skeletonObject.transform.localPosition = new Vector3(0, 0, 0);
                skeletons.Add(skeleton.SkeletonID, skeletonObject);
				foreach (var part in skeletonObject.GetComponent<RenderSkeleton>().partMap)
                {
					part.Value.transform.SetParent(skeletonObject.transform);
                }
            }
            GameObject sObject = null;
            skeletons.TryGetValue(skeleton.SkeletonID, out sObject);
            var renderScript = sObject.GetComponent<RenderSkeleton>();
            renderScript.Skeleton = skeleton;
        }
        if(skeletonEvent.SkeletonLostEvent != null){
            Debug.Log("SKELETON LOST");
            GameObject skeleton = null;
            skeletons.TryGetValue(skeletonEvent.SkeletonLostEvent.Identifier, out skeleton);
            if(skeleton != null){
                Destroy(skeleton);
                skeletons.Remove(skeletonEvent.SkeletonLostEvent.Identifier);
            }
        }
	}


}
