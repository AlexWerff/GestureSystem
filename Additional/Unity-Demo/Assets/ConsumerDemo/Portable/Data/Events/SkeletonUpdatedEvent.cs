using System;
using BAALLClient.Portable.Data.Model;

namespace BAALLClient.Portable.Data.Events
{
	public class SkeletonUpdatedEvent
	{
		public String identifier{
			get;
			set;
		}

		public Skeleton skeleton{
			get;
			set;
		}
	}
}

