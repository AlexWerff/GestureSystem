
using BAALLClient.Unity;
using UnityEngine;
using UnityEngine.UI;

public class SettingsBehavior : MonoBehaviour {

	public Button SaveButton;
	public Button CancelButton;
	public InputField InputFieldIP;

	// Use this for initialization
	void Start () {
		SaveButton.onClick.AddListener(SaveClick);
		CancelButton.onClick.AddListener(CancelClick);
    }
    
    void SaveClick(){
		ConstantValues.BASE_URL = InputFieldIP.text.ToString();
		Debug.Log("UPDATED BASE URL");
		UnityEngine.SceneManagement.SceneManager.LoadScene(0,UnityEngine.SceneManagement.LoadSceneMode.Single);
	}

	void CancelClick(){
		UnityEngine.SceneManagement.SceneManager.LoadScene(0,UnityEngine.SceneManagement.LoadSceneMode.Single);
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
