DrawFBP
=======

#### Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies


Sample DrawFBP network
---

Here is a simple diagram built using DrawFBP, courtesy of Bob Corrick, showing process names, IIPs, and actual class names (used for generating runnable code, and also when checking port connections).

![FilterByFirstValue](https://github.com/jpaulm/drawfbp/blob/master/docs/FilterByFirstValue.png "Simple Network Diagram")

Release History
---

Latest release is v2.18.4: the jar file (which includes the `math.geom2d` jar file) - `drawfbp-2.18.4.jar` - can be obtained from Maven, the Releases folder, or from `build/libs`.  All these copies are identical.

[![Maven Central](https://img.shields.io/maven-central/v/com.jpaulmorrison/drawfbp.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.jpaulmorrison%22%20AND%20a:%22drawfbp%22)

Releases from v2.18.2 onwards have a much speeded up sort function (by name or by date, optionally).  The current version - `drawfbp-2.18.4.jar` - is now available for evaluation.

All Releases from v2.15.10 on require the `math.geom2d` jar file from Maven.

If you click on the Maven shield above, you will need to have Maven installed to run the jar file (it will be less than 500Kb).  If you download the jar file from Releases (1.3+ Mb), this jar file is already included.

Now, all you have to do to run one of the recent releases is to position to your DrawFBP folder, and enter:

<code>java -jar build/libs/drawfbp-x.y.z.jar</code>   where `x.y.z` is the version number.  

The standard (javax.help) JavaHelp jar file (also available on Maven) has now been included among the DrawFBP deliverables in the `lib` folder.

Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise refinement" or "top-down development" by supporting subnets - blocks in the diagram that can specify lower level diagrams, which can in turn specify lower level ones, and so on.  DrawFBP allows the user to draw a diagram just using short, descriptive names for blocks (nodes) and to fill in either component or subnet names later.  Multiple levels can be held under separate tabs, allowing the user to jump back and forth between different levels of a design.

Alternatively, complex diagrams can be turned into multi-level diagrams by using the "excise" function of the Enclosure block: a group of blocks can be converted into a separate subnet and replaced by a "subnet" block, containing appropriate "external port" blocks, in one operation (see Youtube DrawFBP 5 - https://www.youtube.com/watch?v=5brTDk8cpNo around 9:06).  

DrawFBP can generate networks for Java, C#, and NoFlo (JSON).  These are kept separate in the DrawFBP dialogs and typically use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP. 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

Six Youtube videos are currently available showing how to use DrawFBP, for drawing diagrams, and generating running JavaFBP networks, using the Eclipse IDE - see below...

#### Installing DrawFBP

- Create a directory for your DrawFBP clone; download using the GitHub "clone" function. 
- Run as described below under "Running DrawFBP".

#### Getting Started with your Diagram

To get started using DrawFBP once you have it installed, start it going, then just click anywhere on the drawing screen, and a block will appear, with a popup prompting you to add a (short) description.  The type of block defaults to "Process", but a number of other block types are available, controlled by by the buttons along the bottom of the screen.

To indicate a connection between two blocks, click anywhere on the border of the "from" block; then click anywhere on the border of the "to" block - it doesn't matter whether the left mouse button is held down or not. 

Other features are described in the Help facility, which you should install (see below).


Features
----

- Variety of block types, including "Initial IP", Report, File, Legend (text with no boundary), External ports (for subnets), Human (!)
- Top-down design supported - although bottom-up is also supported (blocks can be placed on the diagram and connected, and class names filled in later)
- Display subnets in separate tabs
- Convert portion of diagram to subnet ("excise")
- Specify connection capacity
- "Automatic" ports
- Checking for valid port names
- Indicate "drop oldest" attribute for given connection
- Generate complete networks in Java, C#, JSON, or .fbp notation
- Pan, zoom in/out
- Drag portion only of diagram (using Enclosure block)
- Go to folder from diagram (as of v2.14.1)
- Keyboard-only usage (except positioning of blocks)
- Choose fonts (fixed size and variable size, indicating support for Russian, Hindi (Devanagari), and Chinese)
- Change font size 
- Structured Help facility
- Export diagram as image
- Print diagram
- Drag blocks, sections of diagram (using "Enclosure"), heads or tails of arrows; create or drag bends in arrows
- "Grid" positioning on/off
- Extra arrowhead (one per arrow)
- New Functions (as of v2.16.1): 
        - Compile Java program
        - Run Java program
- New Functions (as of v2.16.5):
        - Compile C# program
        - Run C# program
- As of v2.18.1, File Chooser now displays date and time on non-jar items, and entries can be sorted by name (ascending) or by date/time (descending)         


Videos on DrawFBP features
----

In addition, there are six Youtube videos about DrawFBP, illustrating a number of basic ("classical") FBP concepts (what we are now calling "FBP-inspired" or "FBP-like" systems do not necessarily contain all of these, although DrawFBP should be able to support most of these systems):
- [DrawFBP video #1](https://www.youtube.com/watch?v=OrKenPOV4Js)
- [DrawFBP video #2](https://www.youtube.com/watch?v=9NXYNxDjFWY)
- [DrawFBP video #3](https://www.youtube.com/watch?v=-AmzfhV2hIU)
- [DrawFBP video #4](https://www.youtube.com/watch?v=F0lKQpIjfVE)
- [DrawFBP video #5](https://www.youtube.com/watch?v=5brTDk8cpNo) - concept of "subnets", both at design and implementation time
- [DrawFBP video #6](https://youtu.be/IvTAexROKSA) - simple interactive systems using WebSockets, with demo of JavaFBP-WebSockets (JavaFBP and HTML5)

**Note:** File Chooser now displays date and time on non-jar items, and entries can be sorted by name (ascending) or by date/time (descending).

In addition, the Excise function has changed a little, so the interaction will not be exactly as shown in the video (video #5)
              
Running DrawFBP
----
DrawFBP can be executed directly by executing its jar file, but, as of v2.15.10, it needs the 2D geometry jar file.  The combination is called a "fat jar" file - before v2.18.1, the jar name contained `all-`; from v2.18.1 on, the `all-` has been dropped.  You can download DrawFBP from Maven (search for DrawFBP).  

To run DrawFBP's Help facility, you will need the standard `javax.help` JavaHelp jar file (provided in the `lib` directory).  The first time you launch Help, you will be prompted to specify a folder to hold the `javax.help` jar file.  DrawFBP will remember this location from now on.

DrawFBP has been compiled to run on Java 1.8.

If you want access to the Java annotations of your components, add the jar file(s) containing them (at least JavaFBP and possibly others) to the project Properties/Build Path (for Eclipse), or e.g. type on the command line  

        java -cp "build/libs/drawfbp-x.y.z.jar;..\javafbp\build\libs\javafbp-4.1.0.jar" com.jpaulmorrison.graphics.DrawFBP
        
where `x.y.z` is the DrawFBP version number.

- to run under Linux, replace the semi-colon(s) with colons(s).   
    
Note: if you are displaying a network built using a pre-v2.13.0 version of DrawFBP, with some or all of the component classes (from JavaFBP) filled in, you will have to reaccess the component classes, as the naming conventions have changed slightly.

**DrawFBP properties** are held in a file called <code>DrawFBPProperties.xml</code> in the user's home directory.  If this does not exist, it will be created the first time the user runs DrawFBP - it is automatically updated as the user uses various DrawFBP facilities.


JavaHelp facility
---

DrawFBP has a help facility which uses the powerful JavaHelp facility.  The first time you click on Help/Launch Help, you will be prompted to locate the `javahelp.jar` file.  This is the standard JavaHelp jar file, and can be obtained from Maven Central, by doing a search for artifact `javahelp`, or from http://www.jpaulmorrison.com/graphicsstuff/DrawFBP-Help.jar , which has the same contents.

From then on, DrawFBP will remember the location of your DrawFBP-Help.jar file in your `DrawFBPProperties.xml` file.

XML Schema for `.drw` files
---

An XML Schema has been added to the `lib` folder - https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd - specifying the format of the XML files used to hold DrawFBP diagrams. These files have an extension of `.drw`.  This schema can also be used to check whether the file format of any other diagramming tool matches the `.drw` format.

Old `.drw` files can still be displayed using the latest release of DrawFBP (v2.13.0 and following), but will be stored in the new format when they are rebuilt.

External ports for subnets still have to be added.

Running networks generated by DrawFBP
---

JavaFBP or C#FBP networks created by DrawFBP can be run stand-alone: for Java, you will need to add the JavaFBP jar file, obtainable from GitHub -  https://github.com/jpaulm/javafbp/releases/download/4.1.0/javafbp-4.1.0.jar, to the Java Build Path of any projects you create. 

Just as any necessary Java jar files can be obtained from the JavaFBP project on GitHub, to run C# applications using FBP you will need `.dll` files for `FBPLib` and `FBPVerbs`, obtained from the C#FBP libraries.

If you want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the README file for the `javafbp-websockets` project on GitHub.

This can be done using the File/Add additional jar/dll files function.  

