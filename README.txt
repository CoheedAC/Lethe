# Lethe

Anonymous region based photosharing application created by Armin Ak, Dylan Lynch, Max Kohne, Tim Martinez, & Vince Nicoara
Hi Gregory!

\\Instructions to Build and Execute Project Lethe
Open the “Lethe” project in Android Studio. Once loaded, click the Run menu then “Run App”. 
This will build, compile, and execute the app. Make sure to have an emulator running (we suggest Genymotion) or 
an android phone plugged in.

\\How to use Project Lethe
Use as you would any other social media application. Tap the camera button in the 
action bar to post an image to the server. Tap the me tab to see what images you have posted to the server. 
Tap the more tab to look at social media outlets and future legality information. The more tab has not been fully 
implemented so do not use this portion of the app.

\\Storage of Data Files
Photos are stored on the SD card privately (meaning not accessible from other apps) if the SD card is present. 
Otherwise, everything is stored on the internal storage. Images pulled from the server are stored in a cache folder.

\\\\Special Information (Known bugs, anomalies, etc)
\\Feed Tab
See Camera/Pictures...
Pinch to zoom does not work in full screen mode (but works in peel and me full screen mode).
Full screen view swipe right should like the photo. Swiping left hides the photo.

\\Peek Tab
Requires Google Play Services, which be can be installed on an emulator but is complicated, so you have to test on your own device.
Also, this requires internet AND location services to be enabled. Location services mode should be set to High Accuracy to work.
See Camera/Pictures...
Swiping should go to the next/previous photo in full screen view.

\\Me Tab
See Camera/Pictures...
Swiping should go to the next/previous photo in full screen view.

\\More Tab
The Privacy Policy and Terms of Service buttons are not linking to text yet because we have not contacted 
our lawyer about drafting these documents yet.
The Contact Us button requires the emulator/phone to have the email client set up before use.
Feel free to add me on facebook and like my instagram.

//Camera/Pictures
Pictures taken in landscape mode will appear in the wrong orientation in the app. This is a bug and has not been fixed.

