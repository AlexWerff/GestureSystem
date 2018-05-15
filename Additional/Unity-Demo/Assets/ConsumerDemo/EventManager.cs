using System.Collections;
using System.Collections.Generic;
using BAALLClient.Unity;
using BAALLClient.Portable.Data.Request;
using BAALLClient.Portable.Data.Response;
using Newtonsoft.Json;
using UnityEngine;
using BAALLCLient.Utils;

public class EventManager : MonoBehaviour {
    private SocketManager socketManager;
    public SceneManager SceneManager;


	// Use this for initialization
	void Start () {
        socketManager = new SocketManager();
        var ugly = UnityThreadHelper.Dispatcher;
        socketManager.MessageRecieved += (message) => {
            UnityThreadHelper.Dispatcher.Dispatch(() =>
            {
                processMessage(message);
            });
        };
        socketManager.Connect(ConstantValues.EVENTSOCKET_URL);
	}

	void OnDestroy()
    {
        Debug.Log("DESTROYED");
		UnityThreadHelper.Dispatcher.Dispose();
        socketManager.Dispose();
        socketManager = null;
    }

    private void processMessage(string json){
        var evt = JsonConvert.DeserializeObject<BAALLClient.Portable.Data.Event>(json);
        if(evt.ObjectStateUpdated != null){;
			var go = SceneManager.GameObjectForID(evt.ObjectStateUpdated.Identifier);
            if (go != null)
            {
				ModelUtils.UpdateLight(go, evt.ObjectStateUpdated.State);
            }  
        }

		if (evt.SliderUseEvent != null)
        {
			if(SceneManager != null){
				var obj = SceneManager.DataModel.FindObject(evt.SliderUseEvent.ModelID);
				if(obj != null){
					var go = SceneManager.GameObjectForID(evt.SliderUseEvent.ModelID);
					if (go != null){
						var slider = go.GetComponent<SliderObjectSlider>();
						slider.Current = evt.SliderUseEvent.Percentage;
						/*foreach(var cID in evt.SliderUseEvent.ControlIds){
							var cObj = SceneManager.DataModel.FindObject(cID) as StateObject;
							var cGo = SceneManager.GameObjectForID(cID);
							if (cObj != null){
								ModelUtils.UpdateLight(cGo, new State() { On = cObj.State.On, Value = evt.SliderUseEvent.Percentage });
							}
                       
						} */
					}
				}
			}
        }
    }
	
	// Update is called once per frame
	void Update () {
		
	}
}
