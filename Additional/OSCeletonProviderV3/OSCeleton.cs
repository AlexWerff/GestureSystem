using Microsoft.Kinect;
using Microsoft.Kinect.Face;
using Microsoft.Samples.Kinect.FaceBasics;
using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.Threading;
using Ventuz.OSC;

namespace OSCeleton
{
    class OSCeleton
    {

        // Settings
        private bool allUsers = true;
        private bool faceTracking = false;
        private bool writeOSC = true;
        private bool useUnixEpochTime = true;
        private String oscHost;
        private int oscPort;
        private const int skeletonCount = 6;

        // Outputs
        private bool capturing = true;
        private BlockingCollection<TrackingInformation> trackingInformationQueue = new BlockingCollection<TrackingInformation>();
        Thread sendTracking;
		private JSONWriter osc;
        private Stopwatch stopwatch;

        public void Initialise(int oscPort,String oscHost)
        {
            this.oscHost = oscHost;
            this.oscPort = oscPort;
            // Parse commandline arguments
            string[] args = Environment.GetCommandLineArgs();
            for (int index = 1; index < args.Length; index += 2)
            {
                args[index] = args[index].ToLower();
                if ("allUsers".ToLower().Equals(args[index])) allUsers = StringToBool(args[index + 1]);
                if ("faceTracking".ToLower().Equals(args[index])) faceTracking = StringToBool(args[index + 1]);
                if ("writeOSC".ToLower().Equals(args[index])) writeOSC = StringToBool(args[index + 1]);
                if ("useUnixEpochTime".ToLower().Equals(args[index])) useUnixEpochTime = StringToBool(args[index + 1]);
                if ("oscHost".ToLower().Equals(args[index])) oscHost = args[index + 1];
                if ("oscPort".ToLower().Equals(args[index]))
                {
                    if (!int.TryParse(args[index + 1], out oscPort))
                    {
                        System.Windows.MessageBox.Show("Failed to parse the oscPort argument: " + args[index + 1]);
                    }
                }
            }

            // Initialisation
            stopwatch = new Stopwatch();
            stopwatch.Reset();
            stopwatch.Start();
            if (writeOSC)
            {
                osc = new JSONWriter(oscHost, oscPort);
            }
            if (sendTracking == null)
            {
                sendTracking = new Thread(SendTrackingInformation);
                sendTracking.Start();
            }
        }

        public void Stop()
        {
            capturing = false;
            if (sendTracking != null)
            {
                sendTracking.Abort();
                sendTracking = null;
            }
        }

        public void SpeechRecognized(String value, String tag,float confidence)
        {
            if (osc != null)
                osc.Send(new OscElement("/speech", value, tag,confidence));
        }

        public void LostUser(ulong id)
        {
            if (osc != null)
                osc.Send(new OscElement("/lost_skeleton", (int)id));
        }

        public void NewUser(ulong id)
        {
            if (osc != null)
                osc.Send(new OscElement("/new_skeleton", (int)id));
        }

        public void EnqueueBody(int sensorId, int user, Body b)
        {
            if (!capturing) { return; }
            if (b == null) { return; }
            trackingInformationQueue.Add(new BodyTrackingInformation(sensorId,user, b,false,0.2));
        }

        float detectionResultToConfidence(DetectionResult r)
        {
            switch (r)
            {
                case DetectionResult.Unknown:
                    return 0.5f;
                case DetectionResult.Maybe:
                    return 0.5f;
                case DetectionResult.No:
                    return 0f;
                case DetectionResult.Yes:
                    return 1f;
                default:
                    return 0.5f;
            }
        }

        public void EnqueueFaceTracking(int sensorId, int user, Microsoft.Kinect.Face.FaceFrameResult faceResult)
        {
            if (!capturing) { return; }
            if (faceResult == null) { return; }

            
            // extract face rotation in degrees as Euler angles
            if (faceResult.FaceRotationQuaternion != null)
            {
                int pitch, yaw, roll;
                MainWindow.ExtractFaceRotationInDegrees(faceResult.FaceRotationQuaternion, out pitch, out yaw, out roll);
                trackingInformationQueue.Add(new FaceRotationTrackingInformation(sensorId,user, pitch, yaw, roll,0.2));
            }

            // extract each face property information and store it in faceText
            if (faceResult.FaceProperties != null)
            {
                trackingInformationQueue.Add(new FacePropertyTrackingInformation(sensorId,user,
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.Happy]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.Engaged]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.WearingGlasses]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.LeftEyeClosed]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.RightEyeClosed]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.MouthOpen]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.MouthMoved]),
                    detectionResultToConfidence(faceResult.FaceProperties[FaceProperty.LookingAway]),0.2));
            }
        }

        void SendTrackingInformation()
        {
            while (true)
            {
                TrackingInformation i = trackingInformationQueue.Take();
                if (i != null && capturing)
                    i.Send(1, osc, null);
            }
        }

        private double getTime()
        {
            if (useUnixEpochTime)
                return getUnixEpochTime();
            return stopwatch.ElapsedMilliseconds;
        }

        private double getUnixEpochTime()
        {
            var unixTime = DateTime.Now.ToUniversalTime() - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            return unixTime.TotalMilliseconds;
        }

        private long getUnixEpochTimeLong()
        {
            var unixTime = DateTime.Now.ToUniversalTime() - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            return System.Convert.ToInt64(unixTime.TotalMilliseconds);
        }

        private string GetErrorText(Exception ex)
        {
            string err = ex.Message;
            if (ex.InnerException != null)
            {
                err += " - More details: " + ex.InnerException.Message;
            }
            return err;
        }

        bool StringToBool(String msg)
        {
            msg = msg.ToLower();
            return msg.Equals("1") || msg.ToLower().Equals("true");
        }        
    }
}
