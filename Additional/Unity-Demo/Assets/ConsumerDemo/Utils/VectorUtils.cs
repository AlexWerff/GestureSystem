using System;
using UnityEngine;

namespace BAALLClient.Unity.Utils
{
    public static class VectorUtils
    {
        public static void SetPositionByTwoPoints(Vector3 start,Vector3 end,Transform obj){
            Vector3 centerPos = new Vector3(start.x + end.x, start.y + end.y,start.z + end.z) / 2f;

            Vector3 scale = new Vector3(obj.localScale.x,
                                        obj.localScale.y,
                                        obj.localScale.z);
            var bondDistance = Vector2.Distance(start, end);
            obj.localScale = new Vector3(obj.localScale.x,obj.localScale.y,bondDistance / 2f);
            obj.position = start;
            obj.LookAt(end);
        }

    }
}
