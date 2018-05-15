using System;
using Ventuz.OSC;
using Newtonsoft.Json.Serialization;
using Newtonsoft.Json;
using System.Net.Sockets;
using System.Text;
using Newtonsoft.Json.Linq;

namespace OSCeleton
{
	public class JSONWriter
	{
		private UdpClient client;
		private String host;
		private int port;

		public JSONWriter(String oscHost, int oscPort)
		{
			host = oscHost;
			port = oscPort;
			client = new UdpClient();
			client.Connect(host, port);
		}

		public void Send(String bundleName,OscBundle bundle)
		{
			var json = JSONFactory.buildJSONBundle(bundleName,bundle);
			var bytes = Encoding.UTF8.GetBytes(json);
			#if (DEBUG)
			if (!client.Client.Connected) //Try to reconnect -> Only for debug purposes
				client.Connect(host, port);
			#endif
			client.Send(bytes, bytes.Length);
		}


		public void Send(OscElement element)
		{
			var json = JSONFactory.buildJSONElement(element);
			var bytes = Encoding.UTF8.GetBytes(json);
			#if (DEBUG)
			if (!client.Client.Connected) //Try to reconnect -> Only for debug purposes
				client.Connect(host, port);
			#endif
			client.Send(bytes,bytes.Length);
		}
	}
}
