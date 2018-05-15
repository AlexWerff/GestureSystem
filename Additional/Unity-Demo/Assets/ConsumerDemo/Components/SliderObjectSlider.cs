using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SliderObjectSlider : MonoBehaviour {
    
    public int Max;
    public int Current;

    private GameObject trackball;
    private GameObject backgroundObject;
    private GameObject progressObject;

	// Use this for initialization
	void Start () {
		trackball = gameObject.transform.GetChild(0).GetChild(0).gameObject;
        backgroundObject = gameObject.transform.GetChild(0).GetChild(2).gameObject;
        progressObject = gameObject.transform.GetChild(0).GetChild(1).gameObject;
    }
	
	// Update is called once per frame
	void Update () {
        renderProgress(Current);
	}

    private void renderProgress(int progress)
    {
        var backgroundSize = (Max - progress) * (1f / Max);
        var progressSize = 1f - backgroundSize;
        backgroundObject.transform.localScale = new Vector3(backgroundObject.transform.localScale.x, backgroundSize, backgroundObject.transform.localScale.z);
        progressObject.transform.localScale = new Vector3(progressObject.transform.localScale.x, progressSize, progressObject.transform.localScale.z);
  
        progressObject.transform.localPosition = new Vector3(progressObject.transform.localPosition.x, progressObject.transform.localPosition.y, progressSize - 1f);
        backgroundObject.transform.localPosition = new Vector3(backgroundObject.transform.localPosition.x, backgroundObject.transform.localPosition.y, (progressSize - 1f)+1f);
        var trackballPos = (progressSize * 1f)-0.5f;
        trackball.transform.localPosition = new Vector3(trackball.transform.localPosition.x, trackball.transform.localPosition.y, trackballPos);
    }

    void OnMouseDown()
    {
        Debug.Log("Mouse down");
    }

    void OnMouseDrag()
    {
        Debug.Log("Mouse drag");
    }
}
