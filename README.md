# The Splunk Android Software Development Kit

#### Version 0.5

The Splunk Android Software Development Kit (SDK) contains library code and 
examples designed to enable developers to build Android applications using 
Splunk.

#### Notes about the creation process of the Android SDK

Android programs are commonly written and Java , compiled to bytecode, converted 
 to "dex" format and executed in [Android's Dalvik VM](http://en.wikipedia.org/wiki/Dalvik_%28software%29)

As a starting point for creating the Splunk Android SDK , I took a copy of the 
Splunk Java SDK(v 1.0.0).
Note, this is not a Git fork or  branch of the Splunk Java SDK , but just a 
straight file copy. When future releases of the Splunk Java SDK are made , the 
changes can be manually merged into the common components in the Splunk Android 
SDK.
The next issue I had to tackle was that the Dalvik VM is not Java SE 6 
compatible and the Splunk Java SDK uses XML Streaming classes that are only 
available in Java SE 6.
Fortunately the required classes are available and [Apache 2.0 open sourced here]
(http://stax.codehaus.org/Home)
Some of these classes are in the javax.* package , so I also had to rename this 
package to com.javax.* so that the Android "dx" converter tool wouldn't 
complain about converter classes with "core library" package prefixes.
An alternative would be to strip out the use of all the XML streaming classes , 
and recode this to a core Android XML API, an option I am still open to.

I have also provided some utility classes in the com.splunk.android.* package.
These classes have helper methods to make it easier to search Splunk and log 
events directly into Splunk from your Android application.
Furthermore , the SplunkLogger class has several methods for extracting various 
useful metrics from the Android runtime , Wifi / Telephony / GeoLocation / 
System Activity / Battery. These are just a few examples of the rich set of 
machine data available from the Android platform.


#### Notes about the release build

As the target platform here is a mobile device, we should be cogniscent of 
potential storage and bandwith constraints.So I want the release build of the 
Android SDK to be :

* 1 single jar file
* as small a footprint as possible

At the time of writing this, the current release size is 470 KB , but I feel we 
can get this much smaller by stripping out unrequired functionality.For instance 
, I have already stripped out the XML and CSV ResultsReaders , and just provided 
the JSON ResultsReader.The ANT build script builds the the gson-2.1.jar file 
into the Android SDK release jar file.So developers only have to deal with 1 
single jar file for development and production distribution.

#### Android version

This has been developed against Android version 4.2 (JellyBean) ,
minSDKVersion 9 , targetSDKVersion 17.

The Android 4.2 jar (android.jar) has been included in the github repo (as a 
compilation dependency for the ANT build script)

## Getting started with the Splunk Android SDK

The Splunk Android SDK contains library code and examples that show how to 
programmatically search Splunk and send events to Splunk from an Android 
application.Futhermore the Splunk Java SDK has more detailed examples that are 
going to be syntactically compliant for Android. 
You can see more examples here : 
[Splunk Developer Portal](http://dev.splunk.com/view/java-sdk/SP-CAAAECN). 

### Requirements

Here's what you need to get going with the Splunk Android SDK.

#### Splunk

If you haven't already installed Splunk, download it 
[here](http://www.splunk.com/download). For more about installing and running 
Splunk and system requirements, see 
[Installing & Running Splunk](http://dev.splunk.com/view/SP-CAAADRV). 

#### Splunk Android SDK

Get the Splunk Android SDK from [GitHub](https://github.com/) and clone the 
resources to your computer. For example, use the following command: 

    git clone https://github.com/damiendallimore/splunk-sdk-android.git

To build the SDK from source , you will need ANT. The ANT build script is in 
the project's "build" directory.This will output a release tarball to the 
"releases" directory.
    
You can also [download the realease file](https://github.com/damiendallimore/splunk-sdk-android/tree/master/releases/splunk-android-sdk-0.5.tar.gz) to include the Splunk Android SDK
in your applications. Untar the release and copy the jar file into your 
environment.

#### Google Android SDK

I recommend using [Google's Android SDK](http://developer.android.com/sdk/index.html) 
for your development. This will provision you with the appropriate envrironment 
for creating, compiling, testing and running your Android application.

#### Where to put the splunk-android-sdk-0.5.jar  file ?

When you use the project wizard in Eclipse to create your Android project , 
a "libs" directory will be created for you.You can take the Splunk Android jar 
file and just drop it in this directory.The jar file will automatically be 
picked up and added to the list of Android Dependencies.


####Examples

In the Splunk Android SDK "example" directory , I have included a few files from 
a demo Android application I created that utilizes the SDK.It's not the full 
project contents , but it is the main files that you can use a reference to get 
up and running.

* AndroidManifest.xml : examples of the required declarations (permissions, 
service definition etc..)
* LoggingService.java : an Android Service for logging events to Splunk
* MainActivity.java : contains example of firing off a realtime search and then 
listening for results.This particular search is triggered by clicking a button.
The search results are for twitter events that get rendered in the Android UI.
The rendering is the tweeter's profile pic, handle and the tweet they sent.


### Contact

This project was initiated by Damien Dallimore
<table>

<tr>
<td><em>Email</em></td>
<td>ddallimore@splunk.com</td>
</tr>

<tr>
<td><em>Twitter</em>
<td>@damiendallimore</td>
</tr>


</table>

## License

The Splunk Java Software Development Kit is licensed under the Apache
License 2.0. Details can be found in the LICENSE file.


