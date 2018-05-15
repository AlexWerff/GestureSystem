using System;
using System.Threading;
using UnityEngine;

namespace BAALLClient.Unity
{
	public class SocketManager:IDisposable
    {
        private const int RECONNECT_DELAY = 3000;

		private WebSocketSharp.WebSocket websocket;
        private Thread connectThread;

        public event Action<String> MessageRecieved;

        public  SocketManager()
        {
        }

        private void tryReconnect(int delay){
            Debug.Log(String.Format("Trying to reconnect with {0}ms delay",delay));
            connectThread = new Thread(() =>
            {
                Thread.Sleep(delay);
                Connect(Url);
            });
            connectThread.Start();
        }

       



        private void onOpen(object sender, EventArgs evt){
            Debug.Log(String.Format("Successfully connected to: {0}",Url));
        }

		private void onError(object sender,WebSocketSharp.ErrorEventArgs evt){
			//Debug.LogError(evt.Message);
            //tryReconnect(RECONNECT_DELAY);
        }

		private void onMessage(object sender,WebSocketSharp.MessageEventArgs evt){
            if(MessageRecieved != null){
                MessageRecieved.Invoke(evt.Data);
            }
        }

        private void onClose(object sender,EventArgs evt){
            Debug.LogWarning(String.Format("Disconnected from: {0}", Url));
        }


        public void Connect(String url){
            Url = url;
            try{
				websocket = new WebSocketSharp.WebSocket(url);
                websocket.OnOpen += onOpen;
                websocket.OnError += onError;
                websocket.OnClose += onClose;
                websocket.OnMessage += onMessage;
                websocket.Connect();
            }
            catch(Exception ex){
                
            }

        }

        public void Disconnect(){
            if(this.websocket != null){
                Url = "";
                websocket.Close();
                websocket = null;
            }
        }

		public void Dispose()
		{
			if(websocket != null){
				websocket.Close();
                websocket = null;
			}         
			if(connectThread != null){
				connectThread.Abort();
                connectThread = null;
			}
		}

		public String Url{
            private set;
            get;
        }
    }
}
