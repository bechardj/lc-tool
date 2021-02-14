# Lyrasis Document Capture
Video Setup:
https://youtu.be/E372bsYnbxc

## Updating Versions
* Backup your output directory
* Replace your existing jar file with the latest release
* Start the jar as usual
* Do a quick test to verify functionality (do images save and crop corectly? do existing files open save/crop?)

## Installation
### Prerequisites
#### Java
This application requires Java 8 or newer to run. You can get an installer for the OpenJDK from [here](https://adoptopenjdk.net). 

#### Web Browser
Functionality is primarily tested under Google Chrome and Safari on macOS. You should make sure you are on a recent web browser for best results. The behavior of some key-bindings are not extensively tested on Windows.

### Setting up
Download the latest lyrasis-x.y.z.zip release from GitHub. This should be visible in the "Release" section on the right of this page. Extract it somewhere convenient.

Included in the zip file are 4 files:

* The program itself, ending in `.jar`
* An `application.properties` file
* An `ingest` directory
* An `output` directory

First, put the program `.jar` file in a convenient location, along with the `application.properties` file. Likewise, move the ingest and output directories somewhere that makes sense. Their purpose will be explained later, but it might make sense to store them separate from the `.jar` file depending on your file organization preferences.

Open `application.properties` file in a text editor. Here you will configure the parameters for the application. The `image.ingest.path` is the full absolute path the `ingest` directory from earlier. On UNIX-like systems, this might look like:

`image.ingest.path=/Users/joey/lyrasis/ingest`.
 
On Windows, you will likely have to use a path like:
 
`image.ingest.path=C://Users//Joey//lyrasis//ingest`

 
The output directory must be configured likewise.
 
The only other setting to configure is your preferred server port. By default, it is:
 
`server.port = 8091`
 
You can change this as needed, but the default is probably fine.
 
### Running the application
Open up your terminal and navigate to the directory where the `.jar` file is installed.

To run the application, you will need to execute a command similar to the following on UNIX (all one line):

**Note: if you keep the properties file in the same directory as the jar, you don't need to specify the spring.config.location path. This is recommended**

`java -jar lyrasis-0.0.1.jar --spring.config.location=/Users/joey/src/lyrasis/application.properties`

On Windows (all one line):

`java -jar lyrasis-0.0.1.jar --spring.config.location=C:\Users\Joey\Documents\application.properties`

Note that the argument `--spring.config.location` is set to the absolute path of the `application.properties` file.

After executing this, you should now have the application running! If all went well, you should see a line similar to the following:

`Started LyrasisApplication in 1.621 seconds`

This means the web app is ready to go at `localhost:8091` (the port may be different if you changed it). Leave this terminal window open while using the web app. If you accidentally close it, you should be able to restart it without issue, even while editing a document in your browser- but I wouldn't try this unless you must. 

If you see any errors, double check your paths in the `application.properties` file and the `spring.config.location` path. Make sure java has the correct permissions to open and modify those directories as well.

### Adding Images To 'Capture'

All you need to do is add image files with the desired naming convention to the ingest directory. Image files should have the extension "jpeg", "jpg", or "png". If you have a pdf, you should export it to separate images (use a tool like `pdfimages`), When you load `http://localhost:8091` in your browser, all image files in this folder will be copied over to a directory with the same file name in the output folder. An `imageJob.json` file will also be generated. This contains the raw data from the capture. JSON is easily interchanged between Java, JavaScript, Python, etc.- so it makes sense as a means to store it in this format.  When you open an image in the web app, image data will be loaded from this `imageJob.json` file.

An `archive` directory is also created. Every time you save the capture data in the web app, a backup of the previous `imageJob.json` is stored here. Think of it as an insurance policy in case something goes wrong. 

The `crops` directory is also created. Every time you save the capture data in the web app, each character that you labeled is extracted from the image, and saved in a labeled directory within the `crops` directory. Please note this directory is not backed up, and is cleared on every save to prevent duplicates.

You should avoid modifying files in the `output` directory unless you know what you are doing. You can always make a backup copy as well. Additionally, if you want to clean up the web interface listing, you can move the folders within the `output` directory elsewhere. You should not rename any files, otherwise the application may no longer be able to traverse the file tree.

### Capturing Images

For more detailed instructions on the capture process itself, visit `http://localhost:8091/help` once the application has started.

Please verify the application is working as expected, especially when you are first using it on your machine. Mostly I have only tested on my own, so you should validate things for yourself and report back if anything seems odd.

Zooming into the web page is **not** known to cause issues at the moment, but if you do this please check the image crops to double check nothing funny has happened with the combination of your machine + web browser.

Currently, only the Letter Capture process involves any processing on the backend. The other 2 modes are there for future potential functionality. Also note the ImageJob file contains a map called fields. This should let us add info to the JSON without breaking parsing in the future.

## Future Development
These are things I'd like to eventually add, in no particular order.
* Resizable/Responsive canvas- currently, the canvas takes up the size of the image. For simplicity sake, this is the easiest approach with the smallest risk of causing bugs with the capture process
* Per-Word and Per-Line capture
* Pagination/Document Management
* User preferences
* Render images from URL instead of API call
* JS optimization (currently mostly vanilla JS and jQuery, with room for improvement)
* Multiple users off of single instance of app

## Building From Source
This project needs to be built with `maven`. Best to get Maven from your package manager.

macOS:

`brew install maven`

Linux:

`sudo apt install maven`

Windows:

Haven't tried it

To build, navigate to the same directory as the `pom.xml` and run 

`mvn clean compile install`

This should build a `jar` file in the `target` directory.

## Modifying the Source

You can use any editor, but I recommend IntelliJ because it has a lot of Spring integration.
