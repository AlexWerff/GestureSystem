using System;
using BAALLClient.Portable.Data.Events;

namespace BAALLClient.Portable.Data
{
	public class Event
	{
		public Event ()
		{
		}

		public SkeletonUpdatedEvent SkeletonUpdatedEvent{
			get;
			set;
		}

        public SkeletonLostEvent SkeletonLostEvent
        {
            get;
            set;
        }

        public ObjectUpdatedEvent ObjectUpdatedEvent{
            get;
            set;
        }

		public SliderUseEvent SliderUseEvent
        {
            get;
            set;
        }
		public ObjectStateUpdated ObjectStateUpdated 
		{ 
			get;
			set; 
		}
	}
}

