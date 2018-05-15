using System;
using System.Collections.Generic;

namespace BAALLClient.Portable.Data
{
    public class SkeletonProviderConfig
    {
        public SkeletonProviderConfig()
        {
        }

        //name:String,remote: InetSocketAddress,position:List[Float],orientation:List[Float]

        public String Name{
            get;
            set;
        }

        public String RemoteAddress{
            get;
            set;
        }

        public int Port{
            get;
            set;
        }

        public List<float> Position{
            get;
            set;
        }

        public List<float> Orientation{
            get;
            set;
        }

        public override string ToString()
        {
            return string.Format("[SkeletonProviderConfig: Name={0}, RemoteAddress={1}, Port={2}, Position={3}, Orientation={4}]", Name, RemoteAddress, Port, Position, Orientation);
        }
    }
}
