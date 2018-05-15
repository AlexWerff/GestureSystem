# GestureSystem

A reaktive (Scala and Akka) System for managing, analysing and distributing Gestures.

# Content

  - Backend (Akka /Scala /SBT Project) 
  - Additional
    -  OSCeletonProvider V3 (Sending JSON instead of Bytestream)
    -  Unity Sample (bad coding but should work as an example)

# Comment
This was my bachelor-thesis. It is a basic piece of software which does gesture analysis based of a JSON model of a room. What it does is still very raw but it works (proven in my own room with a phillips hue).
I hope there is someone out there who likes this project as much as I do and will continue it. I probably don't have much time for improving it, therefore I want to share it with you guys. Its open source so feel free to copy, modify or do whatever you want to do with it! 


# How to use
First of all you have to create a JSON model of your room. I have written a small Plugin for Unity to export a scene. Its not working very well (it was not designed to be published or used more than once) but might help you generating a JSON file of your Unity model.
If you have your model as a JSON file you can copy it into the model folder or simply replace my "model.json".
The next step is to create a Provider. 
Simply add a new Provider or modify my Provider (follow the TODOs).
If this worked you can now create a Consumer. Simply add a new Consumer or modify my PhillipsHueConsumer (if you have a phillips hue, because for now only this is supported). 
If this also is done you can try to start the System. Since this is an SBT Project you should install SBT or start it with IntelliJ (if you want to debug it).

```sh
$ cd Backend
$ sbt run
```
That should give you the following output
```sh
Server online at http://{{YOUR-IP}}:8080/
Press RETURN to stop...
```
This tells you the Server is up and running. 
To finally test or check out the Dashboard open this URL in your browser:

> {{YOUR-IP}}:8080/admin/index.html

This should open a dashboard where you will see your model, provider, consumer etc.
If this is shown the system is working.
Now you can use your XBox Kinect and the OSCeletonProviderV3 to feed the System with some Skeletons.
Simply build and launch it and enter the the IP and Port of your Backend into the Fields at the bottom.

To check if any Skeletons are coming in you can check with a websocket.
Simply connect to:
> {{YOUR-IP}}:8080/sockets/skeletonSocket

If everything works there should be Skeletons coming in with a high frequence.
This shows that Skeletons are recognized.
Now you can build and launch the Unity App. It has a Settings View where you can enter your IP and Port but you can also modify the ConstantValues.cs File and set it there.

To check if any Gestures are recognized simply connect to:

> {{YOUR-IP}}:8080/sockets/eventSocket

This should show all Events coming up in the System.
So thats if guys so this was a short setup guide to get your gesture system ready.

# More Comments
So please keep in mind that this is not a final product and still far away from beeing it. The code my be bad here and there(or maybe in your opinion everywhere). The Backend Code is written in Scala and designed to be as functional as possible (Actors are not really functional or even pure).
If you don't have any experience with functional programming or akka please visit the akka webpage and try to learn Scala (its an amazing language and my favourite by far).
And please excuse any grammar misstakes because I am not a native english writer.


# Note of thanks
Since this way my bachelor thesis I would like to say thanks to all contributors of this thesis (people being involved know that I am talking to them). Thanks for giving me a room to work, the idea and the equipment.
Thanks for giving advices and help everytime I needed them.


