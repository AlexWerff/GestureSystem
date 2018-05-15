using System.Collections.Generic;

namespace BAALLClient.Portable.Data
{
    public class SliderUseEvent
    {
		public SliderUseEvent(){
			ModelID = "";
			ControlIds = new List<string>();
			Percentage = 0;
			SkeletonID = "";
			BodyPartID = "";
		}

		public string ModelID{
			get;
			set;
		}

		public List<string> ControlIds{
			get;
			set;
		}

		//percentage:Int,skeletonID: String,bodyPartID:String
		public int Percentage{
			get;
			set;
		}

		public string SkeletonID{
			get;
			set;
		}

		public string BodyPartID{
			get;
			set;
		}
    }
}