using System;
using System.Globalization;
using Ventuz.OSC;

namespace OSCeleton
{
	public static class JSONFactory
	{

		public static String buildJSONElement(OscElement element)
		{
			String json = "[";
			var counter = 0;
            json += "\"" + element.Address.ToString() + "\",";
            foreach (object obj in element.Args)
			{
                json += "\"" + String.Format(CultureInfo.InvariantCulture, "{0}", obj) + "\"";
			
				json += counter == (element.Args.Length - 1) ? "]" : ",";
                counter++;
			}


			return json;
		}

		public static String buildJSONBundle(String bundleName, OscBundle bundle)
		{
			String json = "[\"" + bundleName+ "\",[";
            var counter = 0;
			foreach (OscElement element in bundle.Elements)
			{
                json += buildJSONElement(element);
                json += counter == (bundle.Elements.Count - 1) ? "" : ",";
                counter++;
			}
			json += "]]";
			return json;
		}

	}

}
