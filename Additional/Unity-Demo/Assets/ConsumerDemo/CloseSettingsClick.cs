using BAALLClient.Portable.Data;
using BAALLClient.Portable.Data.Request;
using BAALLClient.Portable.Data.Response;
using BAALLClient.Unity;
using UnityEngine;
using UnityEngine.UI;

public class CloseSettingsClick : MonoBehaviour {

    public InputField InputAddress;
    public Canvas SettingsCanvas;
    public Canvas HUDCanvas;
    public GameObject ProviderItemPrefab;

    // Use this for initialization
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }

    void OnButtonClick()
    {
        SettingsCanvas.gameObject.SetActive(false);
        HUDCanvas.gameObject.SetActive(true);
    }

    private void save()
    {
    }
}
