using System;

namespace BAALLClient.Unity{
    public static class ConstantValues
    {
		public static String BASE_URL = "192.168.178.21:8080";

		public static String SKELETONSOCKET_URL{
			get{
				return "ws://" + BASE_URL + "/sockets/skeletonSocket";
			}
		}
		public static String EVENTSOCKET_URL{
			get{
				return "ws://" + BASE_URL + "/sockets/eventSocket";
			}
		}
		public static String REST_URL{
			get{
				return "http://" + BASE_URL + "/api";
			}
		}
    }
}
