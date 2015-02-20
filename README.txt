# Lethe

Anonymous region based photosharing application created by Armin Ak, Dylan Lynch, Max Kohne, Tim Martinez, & Vince Nicoara
Hi Gregory!

\\Instructions to Build and Execute Project Lethe
Open the “Lethe” project in Android Studio. Once loaded, click the Run menu then “Run App”. 
This will build, compile, and execute the app. Make sure to have an emulator running (we suggest Genymotion) or 
an android phone plugged in.

\\\\Special Information (Known bugs, anomalies, etc)
\\More Tab
The Privacy Policy and Terms of Service buttons are not linking to text yet because we have not contacted 
our lawyer about drafting these documents yet.
The Contact Us button requires the emulator/phone to have the email client set up before use.
Feel free to add me on facebook and like my instagram.

\\Feed Tab
You have to manually get images from the server when you first open the app using the refresh button.
The title of the page is called “Home”
Full sized images are downloaded every single time you open the full screen view even if you 
have already downloaded it before.
Only shows the images in Isla Vista (the coordinates are hard coded)
Scrolling may be a bit laggy
When clicking a thumbnail and swiping right, the hide photo feature has not been implemented 
but it does decrement the view count on the server
The trash icon clears the cache (this is for testing purposes only)

\\Me Tab
Once again, scrolling is an issue
Loading the grid is on the main thread which can really slow down the app
The copy icon makes 50 copies of the first image in the grid (this is for testing purposes only)
The me tab sometimes shows blank images or even images stored in other directories. This is a bug that has not 
been fixed yet.
The trash icon deletes ALL posted images (it is for testing purposes only)
The user can delete individual photos when click on the image in the grid to enter full screen 
view and clicking the trash icon there

\\Peek Tab
We can only test the functionality on actual devices since the emulator lacks Google Maps functionality.
All of the Tabs
Do not store previous state so switching from tab to tab will load a new tab (with all of the respective objects)

\\Storage of Data Files
Photos are stored on the SD card privately (meaning not accessible from other apps) if the SD card is present. 
Otherwise, everything is stored on the internal storage. Images pulled from the server are stored in a cache folder.

\\How to use Project Lethe
Use as you would any other social media application. The app does not pull from the server when you open it. 
Click the refresh button in the feed tab (labeled “Home”) to get all of the images. Tap the camera button in the 
action bar to post an image to the server. Tap the me tab to see what images you have posted to the server. 
Tap the more tab to look at social media outlets and future legality information. The peek tab has not been fully 
implemented so do not use this portion of the app.
