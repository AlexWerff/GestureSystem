using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class MenuBehavior : MonoBehaviour {
    
	public Button SettingsButton;
    public Button StartButton;
	public Camera MainCamera;
	public GameObject CenterRotation;

	// Use this for initialization
	void Start()
    {
		StartButton.onClick.AddListener(StartClick);
		SettingsButton.onClick.AddListener(SettingsClick);
    }
    
    void StartClick()
    {
        UnityEngine.SceneManagement.SceneManager.LoadScene(2, UnityEngine.SceneManagement.LoadSceneMode.Single);
    }

    void SettingsClick()
    {
		UnityEngine.SceneManagement.SceneManager.LoadScene(1, UnityEngine.SceneManagement.LoadSceneMode.Single);
    }
	
	// Update is called once per frame
	void Update () {
		//MainCamera.transform.LookAt(CenterRotation.transform);
		//MainCamera.transform.Translate(Vector3.right * (Time.deltaTime*20));
	}
}
