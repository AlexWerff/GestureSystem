using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class GameSceneBehavior : MonoBehaviour {

	public Button MenuButton;
	// Use this for initialization
	void Start () {
		MenuButton.onClick.AddListener(MenuClick);
	}

	void MenuClick(){
		UnityEngine.SceneManagement.SceneManager.LoadScene(0, UnityEngine.SceneManagement.LoadSceneMode.Single);
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
