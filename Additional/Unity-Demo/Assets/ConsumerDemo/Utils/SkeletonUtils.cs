using System;
using System.Collections.Generic;
using BAALLClient.Portable.Data;
using BAALLClient.Portable.Data.Model;

namespace BAALLClient.Unity.Utils
{
    public static class SkeletonUtils
    {

        public static UnityEngine.Vector3 VectorFromJoint(Joint joint){
            return new UnityEngine.Vector3(joint.x, joint.y, joint.z);
        }

        public static Tuple<String,Joint>[][] GetOrderedJointPairs(Skeleton skeleton){
            return new Tuple<String, Joint>[][]{
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.HEAD,skeleton.GetJoint(Skeleton.HEAD)),
                    Tuple.Create(Skeleton.NECK,skeleton.GetJoint(Skeleton.NECK))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.NECK,skeleton.GetJoint(Skeleton.NECK)),
                    Tuple.Create(Skeleton.TORSO,skeleton.GetJoint(Skeleton.TORSO))
                },
				new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.NECK,skeleton.GetJoint(Skeleton.NECK)),
					Tuple.Create(Skeleton.L_SHOULDER,skeleton.GetJoint(Skeleton.L_SHOULDER))
                },
				new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.NECK,skeleton.GetJoint(Skeleton.NECK)),
					Tuple.Create(Skeleton.R_SHOULDER,skeleton.GetJoint(Skeleton.R_SHOULDER))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.TORSO,skeleton.GetJoint(Skeleton.TORSO)),
                    Tuple.Create(Skeleton.L_COLLAR,skeleton.GetJoint(Skeleton.L_COLLAR))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_COLLAR,skeleton.GetJoint(Skeleton.L_COLLAR)),
                    Tuple.Create(Skeleton.L_SHOULDER,skeleton.GetJoint(Skeleton.L_SHOULDER))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_SHOULDER,skeleton.GetJoint(Skeleton.L_SHOULDER)),
                    Tuple.Create(Skeleton.L_ELBOW,skeleton.GetJoint(Skeleton.L_ELBOW))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_ELBOW,skeleton.GetJoint(Skeleton.L_ELBOW)),
                    Tuple.Create(Skeleton.L_HAND,skeleton.GetJoint(Skeleton.L_HAND))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_HAND,skeleton.GetJoint(Skeleton.L_HAND)),
                    Tuple.Create(Skeleton.L_FINGERTIP,skeleton.GetJoint(Skeleton.L_FINGERTIP))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.TORSO,skeleton.GetJoint(Skeleton.TORSO)),
                    Tuple.Create(Skeleton.R_COLLAR,skeleton.GetJoint(Skeleton.R_COLLAR))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_COLLAR,skeleton.GetJoint(Skeleton.R_COLLAR)),
                    Tuple.Create(Skeleton.R_SHOULDER,skeleton.GetJoint(Skeleton.R_SHOULDER))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_SHOULDER,skeleton.GetJoint(Skeleton.R_SHOULDER)),
                    Tuple.Create(Skeleton.R_ELBOW,skeleton.GetJoint(Skeleton.R_ELBOW))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_ELBOW,skeleton.GetJoint(Skeleton.R_ELBOW)),
                    Tuple.Create(Skeleton.R_HAND,skeleton.GetJoint(Skeleton.R_HAND))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_HAND,skeleton.GetJoint(Skeleton.R_HAND)),
                    Tuple.Create(Skeleton.R_FINGERTIP,skeleton.GetJoint(Skeleton.R_FINGERTIP))
                }, 
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.TORSO,skeleton.GetJoint(Skeleton.TORSO)),
                    Tuple.Create(Skeleton.WAIST,skeleton.GetJoint(Skeleton.WAIST))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.WAIST,skeleton.GetJoint(Skeleton.WAIST)),
                    Tuple.Create(Skeleton.L_HIP,skeleton.GetJoint(Skeleton.L_HIP))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_HIP,skeleton.GetJoint(Skeleton.L_HIP)),
                    Tuple.Create(Skeleton.L_KNEE,skeleton.GetJoint(Skeleton.L_KNEE))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_KNEE,skeleton.GetJoint(Skeleton.L_KNEE)),
                    Tuple.Create(Skeleton.L_ANKLE,skeleton.GetJoint(Skeleton.L_ANKLE))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_ANKLE,skeleton.GetJoint(Skeleton.L_ANKLE)),
                    Tuple.Create(Skeleton.L_FOOT,skeleton.GetJoint(Skeleton.L_FOOT))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.WAIST,skeleton.GetJoint(Skeleton.WAIST)),
                    Tuple.Create(Skeleton.R_HIP,skeleton.GetJoint(Skeleton.R_HIP))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_HIP,skeleton.GetJoint(Skeleton.R_HIP)),
                    Tuple.Create(Skeleton.R_KNEE,skeleton.GetJoint(Skeleton.R_KNEE))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.L_ELBOW,skeleton.GetJoint(Skeleton.R_KNEE)),
                    Tuple.Create(Skeleton.L_ELBOW,skeleton.GetJoint(Skeleton.R_ANKLE))
                },
                new Tuple<String,Joint>[]{
                    Tuple.Create(Skeleton.R_ANKLE,skeleton.GetJoint(Skeleton.R_ANKLE)),
                    Tuple.Create(Skeleton.R_FOOT,skeleton.GetJoint(Skeleton.R_FOOT))
                }
            };
        }


    }
}
