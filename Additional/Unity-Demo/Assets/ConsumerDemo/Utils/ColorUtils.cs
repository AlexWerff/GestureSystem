using System;
using System.Collections.Generic;
using UnityEngine;

namespace BAALLClient.Unity.Utils
{
    public static class ColorUtils
    {
        private static Dictionary<String, Color> colors = new Dictionary<String, Color>();
        private static List<Color> colorList = new List<Color>
        {
            Color.red,
            Color.blue,
            Color.yellow,
            Color.cyan,
            Color.green
        };
        private static int currentColorIndex = 0;

        public static Color ColorForID(String id){
            Color color = Color.red;
            colors.TryGetValue(id,out color);
            if(color.Equals(Color.red)){
                currentColorIndex++;
                color = colorList[currentColorIndex];
            }
            return color;
        }
    }
}
