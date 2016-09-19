DrawFBP
=======

Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies

#### The latest version of DrawFBP is now up on Maven Central - go to http://search.maven.org/ , and search for DrawFBP.  Download the .jar file from here, and execute using Java Platform SE binary.

Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise decomposition" by supporting subnets - blocks in the diagram can specify lower level diagrams,which can in turn specify lower level ones, and so on.   This allows the user to "zoom in" to a lower level, and then pop back up to the original diagram.

DrawFBP can generate networks for Java, C#, and NoFlo.  These are kept separate in the DrawFBP dialogs and typically use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP (under development). 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

#### Getting Started

To get started using DrawFBP once you have it installed (see below), just click anywhere on the drawing screen, and a block will appear, with a popup prompting you to add a (short) description.  The type of block is controlled by the buttons along the bottom of the screen.

To connect two blocks, click anywhere on the border of the "from" block; then click anywhere on the border of the "to" block - it doesn't matter whether the left mouse button is held down or not. 

Other features are described in the Help facility, which you should install (see below also).


Features
----

- Variety of symbols, including Initial IP, Report, File, Legend (text with no boundary), External ports (for subnets), Human
- Top-down design supported - although bottom-up is also supported (blocks can be placed on the diagram and connected, and class names filled in later)
- Display subnets in separate tabs
- Convert portion of diagram to subnet ("excise")
- Specify connection capacity
- "Automatic" ports
- Checking for valid port names
- Indicate "drop oldest" for given connection
- Generate complete networks in Java, C#, JSON, or .fbp notation
- Pan, zoom in/out
- Keyboard-only usage (except positioning of blocks)
- Choose fonts (fixed size and variable size, indicating support for Chinese), font sizes
- Structured Help facility
- Export diagram as image
- Print diagram
- Drag blocks, sections of diagram (using "Enclosure"), heads or tails of arrows; create or drag bends in arrows
- "Grid" positioning on/off
- Extra arrowhead (one per arrow)


Prerequisites for Building  (DrawFBP can be _run_ simply from the .jar file)
---

The project requires Gradle for building (tested with version 2.0). You can download the corresponding package from the following URL: 
http://www.gradle.org

Windows and Linux users should follow the installation instructions on the Maven website (URL provided above).

OSX users (using Brew, http://brew.sh) can install Maven by executing the following command:

    brew install gradle

Eclipse IDE Integration
---

You can generate Eclipse project using the following command:

    gradle eclipse

If you already created an Eclipse project you can run:

    gradle cleanEclipse Eclipse

You need to install a Gradle plugin for Eclipse as explained here:
https://github.com/spring-projects/eclipse-integration-gradle/
Then import a generated project in Eclipse, right (ctrl for OSX) click on the project in Eclipse -> Configure -> Convert to Gradle Project. After the conversion you can Right (ctrl for OSX) click on the project -> Gradle -> Task Quick Launcher and type `build`.


In Eclipse, you may have to do a `Refresh` on the project.


Building from command line
---

Current release can be found in most recent release in `Releases`, which also contains the most recent jar file.

For building the project yourself simply run the following command:

    gradle build

As a result a `DrawFBP-x.y.z.jar` file will be created in the `build/libs` directory.  If you have done this earlier, you have to delete the `build` directory first.


Running DrawFBP
----

DrawFBP can be executed directly by executing the jar file.  You can download it from the latest release in the DrawFBP project Releases directory in GitHub, and can then be executed directly by double-clicking on it.  You may need to have the Java binary associated with the .jar extension - otherwise double-clicking on the jar file may just display it as a zip file.  You can also build the jar file yourself by running `gradle build`, or run `gradle installDist` and you will find start scripts in `build\install\drawfbp\bin`.

Alternatively it can be run from the command line by positioning to your DrawFBP folder, and entering 

    java -cp build\libs\drawfbp-x.y.z.jar com.jpmorrsn.graphics.DrawFBP (for v2.12.x and lower)
    java -cp build\libs\drawfbp-x.y.z.jar com.jpaulmorrison.graphics.DrawFBP (for v2.13.0 and higher)
    
Note: if you are displaying a network built using an earlier version of DrawFBP, with some or all of the component classes filled in, you will have to reaccess the component classes, as the naming conventions have changed.

Sample DrawFBP network
---

Here is a very simple diagram built using DrawFBP, showing process names, names of component source code and class names (used when checking port connections).

![MergeandSort](https://github.com/jpaulm/drawfbp/blob/master/docs/MergeandSort.png "Simple Network Diagram")

JavaHelp facility
---

DrawFBP has a help facility which uses the powerful JavaHelp facility.  The first time you click on Help/Launch Help, you will be prompted to locate the DrawFBP-Help.jar file.  This is the standard JavaHelp jar file, and can be found in the `lib` directory on the GitHub `drawfbp` project, or can be downloaded from http://www.jpaulmorrison.com/graphicsstuff/DrawFBP-Help.jar .

From then on DrawFBP will remember the location in your `DrawFBPProperties.xml` file.

XML Schema for `.drw` files
---

An XML Schema has been added to the `lib` folder - https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd - specifying the format of the XML files used to hold DrawFBP diagrams. These files have an extension of `.drw`.  This schema can also be used to check whether the file format of any other diagramming tool matches the `.drw` format.

Old `.drw` files can still be displayed using the latest release of DrawFBP (v2.13.0 and following), but will be stored in the new format when they are rebuilt.

External ports for subnets still have to be added.

Running JavaFBP networks generated by DrawFBP
---

If you wish to run any networks that you create with DrawFBP, you will need to add the JavaFBP jar file, obtainable from GitHub -  https://github.com/jpaulm/javafbp/releases/download/4.1.0/javafbp-4.1.0.jar, to the Java Build Path of any projects you create. 

If you want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the README file for the `javafbp-websockets` project on GitHub.

