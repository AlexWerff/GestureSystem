using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace BAALLClient.Portable.Data.Model
{
    public class Skeleton:ModelObject
	{
		public const String HEAD="head";
        public const String NECK = "neck";
        public const String TORSO = "torso";
        public const String WAIST = "waist";
        public const String L_COLLAR = "l_collar";
        public const String L_SHOULDER = "l_shoulder";
        public const String L_ELBOW = "l_elbow";
        public const String L_WRIST = "l_wrist";
        public const String L_HAND = "l_hand";
        public const String L_FINGERTIP = "l_fingertip";
        public const String R_COLLAR = "r_collar";
        public const String R_SHOULDER = "r_shoulder";
        public const String R_ELBOW = "r_elbow";
        public const String R_WRIST = "r_wrist";
        public const String R_HAND = "r_hand";
        public const String R_FINGERTIP = "r_fingertip";
        public const String L_HIP = "l_hip";
        public const String L_KNEE = "l_knee";
        public const String L_ANKLE = "l_ankle";
        public const String L_FOOT = "l_foot";
        public const String R_HIP = "r_hip";
        public const String R_KNEE = "r_knee";
        public const String R_ANKLE = "r_ankle";
        public const String R_FOOT = "r_foot";

        public static String[] BODY_PARTS = new String[] {
            "head", "neck", "torso", "waist",
            "l_collar", "l_shoulder", "l_elbow", "l_wrist", "l_hand", "l_fingertip",
            "r_collar", "r_shoulder", "r_elbow", "r_wrist", "r_hand", "r_fingertip",
            "l_hip", "l_knee", "l_ankle", "l_foot",
            "r_hip", "r_knee", "r_ankle", "r_foot" };

		public Skeleton ()
		{
            Joints = new Dictionary<string, Joint>();
            MetaData = new MetaData();
		}

        public String SkeletonID{
            get{
                var skeletonID = "";
                MetaData.Data.TryGetValue("skeletonID",out skeletonID);
                return skeletonID;
            }
        }

		[JsonProperty(PropertyName = "joints")]
        public Dictionary<string, Joint> Joints
        {
            get;
            set;
        }

		[JsonProperty(PropertyName = "modelProperties")]
        public ModelProperties ModelProperties { 
            get;
            set;
        }

		[JsonProperty(PropertyName = "models")]
        public Dictionary<string, ModelObject> Models
        {
            get;
            set;
        }

		[JsonProperty(PropertyName = "metaData")]
        public MetaData MetaData
        {
            get;
            set;
        }

        public Joint GetJoint(String name)
        {
            Joint joint = null;
            Joints.TryGetValue(name,out joint);
            return joint as Joint;
        }
	}
}

