using System;
using System.Collections;
using System.Collections.Generic;
using BAALLClient.Portable.Data.Model;
using BAALLClient.Unity.Utils;
using Newtonsoft.Json;
using UnityEngine;

public class RenderSkeleton : MonoBehaviour {
    
    public Skeleton Skeleton;
	public GameObject HeadObjectPrefab;
	public GameObject JointConnectionPrefab;
	public GameObject JointPrefab;
	public GameObject HandPrefab;
	public GameObject FootPrefab;
    
	private GameObject headObject;
    public Dictionary<String, GameObject> partMap = new Dictionary<string, GameObject>();
	private Dictionary<String, JointObject> jointMap = new Dictionary<string, JointObject>();

    private const float SPEED = 5f;

	// Use this for initialization
	void Start () {
	}
	
	// Update is called once per frame
	void Update () {
        if(Skeleton != null){
            Render();
        }
	}

    public void Render(){
		var movement = Time.deltaTime * SPEED;
		var headPos = processJoint(Skeleton.GetJoint(Skeleton.HEAD));
		if(headObject == null){
			headObject = Instantiate(HeadObjectPrefab) as GameObject;
            headObject.transform.SetParent(this.transform, false);
			headObject.transform.localPosition = headPos;
		}
  
		var headMove = Vector3.Lerp(headObject.transform.localPosition, headPos, movement);
		headObject.transform.localPosition = headMove;

        foreach(var jArray in SkeletonUtils.GetOrderedJointPairs(Skeleton)){
            try{
				JointObject jointObject = null;
                var key = String.Format("{0}-{1}-{2}", Skeleton.SkeletonID,jArray[0].Item1, jArray[1].Item1);
				jointMap.TryGetValue(key,out jointObject);
                var v1 = processJoint(jArray[0].Item2);
                var v2 = processJoint(jArray[1].Item2);
               
				if(jointObject == null){
					jointObject = new JointObject() { Start = v1, End = v2, GameObject = Instantiate(JointConnectionPrefab) };
					drawLine(jointObject.GameObject,v1,v2,movement);
					jointObject.GameObject.name = key;
					jointMap.Add(key,jointObject);
					jointObject.GameObject.transform.SetParent(this.transform,false);
                }
				else{
					var v1Lerp = Vector3.Lerp(jointObject.Start, v1, movement);
					var v2Lerp = Vector3.Lerp(jointObject.End, v2, movement);
					drawLine(jointObject.GameObject,v1Lerp,v2Lerp,movement);
					jointObject.Start = v1;
                    jointObject.End = v2;
					jointMap.Add(key, jointObject);

				}
            }
            catch(Exception ex){
                Debug.LogWarning(ex.ToString());
            }
        }
		foreach(var joint in Skeleton.Joints){
			try{
				GameObject jointPoint = null;
                partMap.TryGetValue(joint.Key, out jointPoint);
                var lerp = true;
                if (jointPoint == null)
                {
                    lerp = false;
                    if (joint.Key == Skeleton.L_HAND || joint.Key == Skeleton.R_HAND || joint.Key == Skeleton.L_FOOT || joint.Key == Skeleton.R_FOOT)
                    {
                        jointPoint = Instantiate(HandPrefab) as GameObject;
                    }
                    else if (joint.Key != Skeleton.L_WRIST && joint.Key != Skeleton.R_WRIST)
                    {
                        jointPoint = Instantiate(JointPrefab) as GameObject;
                    }
                    jointPoint.name = joint.Key;
                    partMap.Add(joint.Key, jointPoint);
                    jointPoint.transform.SetParent(this.transform, false);
                }
                if (lerp)
                {
                    var jointMove = Vector3.Lerp(jointPoint.transform.localPosition, processJoint(joint.Value), movement * 2.5f);
                    jointPoint.transform.localPosition = jointMove;
                }
                else
                {
                    jointPoint.transform.localPosition = processJoint(joint.Value);
                }
			}
			catch(Exception ex){
				
			}         
		}
    }


    private Vector3 processJoint(BAALLClient.Portable.Data.Model.Joint joint)
    {
        var jointVector = new Vector3(joint.x, joint.y, joint.z);
        return jointVector;
    }
    
    
	void drawLine(GameObject joint,Vector3 start,Vector3 end,float movement)
	{      
		var width = .5f;
		var offset = end - start;
		var scale = new Vector3(width, offset.magnitude / 2.0f, width);
		var position = start + (offset / 2.0f);
		var oldScale = joint.transform.localScale;
		joint.transform.position = position;
		joint.transform.up = offset;
		joint.transform.localScale = new Vector3(oldScale.x, scale.y, oldScale.z);
    }
    
}

public class JointObject{

	public JointObject(){
		
	}

	public GameObject GameObject{
		get;
		set;
	}

	public Vector3 Start{
		get;
		set;
	}

	public Vector3 End{
		get;
		set;
	}
}
