DrawFBP
=======

#### Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies

Latest release is v2.16.2 - it is now available on Maven Central. The next release, v2.16.5, is in pre-Release status. Releases v2.16.3 and v2.16.4 have been deleted.

[![Maven Central](https://img.shields.io/maven-central/v/com.jpaulmorrison/drawfbp.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.jpaulmorrison%22%20AND%20a:%22drawfbp%22)

All Releases from v2.15.10 on require the `math.geom2d` jar file - see below.

The standard (javax.help) JavaHelp jar file (also available on Maven) has now been included among the DrawFBP deliverables.

Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise refinement" or "top-down development" by supporting subnets - blocks in the diagram that can specify lower level diagrams, which can in turn specify lower level ones, and so on.  DrawFBP allows the user to draw a diagram just using short, descriptive names for blocks (nodes) and to fill in either component or subnet names later.  Multiple levels can be held under separate tabs, allowing the user to jump back and forth between different levels of a design.

Alternatively, complex diagrams can be turned into multi-level diagrams by using the "excise" function of the Enclosure block: a group of blocks can be converted into a separate subnet and replaced by a "subnet" block, containing appropriate "external port" blocks, in one operation (see Youtube DrawFBP 5 - https://www.youtube.com/watch?v=5brTDk8cpNo around 9:06).  

DrawFBP can generate networks for Java, C#, and NoFlo (JSON).  These are kept separate in the DrawFBP dialogs and typically use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP. 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

Six Youtube videos are currently available showing how to use DrawFBP, for drawing diagrams, and generating running JavaFBP networks, using the Eclipse IDE:

- https://www.youtube.com/watch?v=OrKenPOV4Js
- https://www.youtube.com/watch?v=9NXYNxDjFWY
- https://www.youtube.com/watch?v=-AmzfhV2hIU
- https://www.youtube.com/watch?v=F0lKQpIjfVE
- https://www.youtube.com/watch?v=5brTDk8cpNo
- https://youtu.be/IvTAexROKSA  (simple interactive systems using WebSockets, with demo of JavaFBP-WebSockets - JavaFBP and HTML5)

#### Installing DrawFBP

- Obtain the jar file `drawfbp-x.y.z.jar`, save it.
- (As of v2.15.10) obtain jar file `math.geom2d`, save it.
- Run as described below under "Running DrawFBP".

#### Getting Started

To get started using DrawFBP once you have it installed, just click anywhere on the drawing screen, and a block will appear, with a popup prompting you to add a (short) description.  The type of block defaults to "Process", but a number of other block types are available, controlled by by the buttons along the bottom of the screen.

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
- Go to folder from diagram (v2.14.1)
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
- Compile C# program
- Run C# program

Running DrawFBP
----

DrawFBP can be executed directly by executing its jar file, but, as of v2.15.10, it needs the 2D geometry jar file.  You can download DrawFBP from Maven (search for DrawFBP), but you will also need the 2D geometry jar file to run.  To run DrawFBP's Help facility, you will need the standard javax.help JavaHelp jar file.  All of these jar files will be held in the project's `lib` directory (thanks, Toivo!).

DrawFBP has been compiled to run on Java 1.8.

The `math.geom2d` jar file can be obtained from Maven Central, by doing a search for artifact `math.geom2d`, or from the project's `lib` folder.  After downloading:

- for Eclipse you can then update your project Properties/Build Path entry to specify its location.

The `math.geom2d` file and the latest full (non-pre-release) release of DrawFBP are all in the `lib` folder, so you can just run DrawFBP as shown below.  The standard JavaHelp file is also in this folder, but you have to locate it yourself (DrawFBP has a function to do this) - DrawFBP will then remember its location.

- if you wish to run DrawFBP from the command line using the latest full release of DrawFBP, position to the folder containing the DrawFBP jar file, then type

        java -cp "lib/*" com.jpaulmorrison.graphics.DrawFBP
        
- if you want access to the Java annotations of your components, add the jar file(s) containing them (at least JavaFBP and possibly others) to the project Properties/Build Path (for Eclipse), or e.g. type on the command line  

        java -cp "lib/*;..\javafbp\build\libs\javafbp-4.1.0.jar" com.jpaulmorrison.graphics.DrawFBP

- to run under Linux, replace the semi-colon(s) with colons(s).   

- Note: this technique will pick up the latest full Release.  If you want to use a different release (or a pre-Release version), use something like

        java -cp "javaGeom-0.11.1.jar;build/libs/drawfbp-x.y.z.jar" com.jpaulmorrison.graphics.DrawFBP
    
Note: if you are displaying a network built using a pre-v2.13.0 version of DrawFBP, with some or all of the component classes (from JavaFBP) filled in, you will have to reaccess the component classes, as the naming conventions have changed slightly.

**DrawFBP properties** are held in a file called <code>DrawFBPProperties.xml</code> in the user's home directory.  If this does not exist, it will be created the first time the user runs DrawFBP - it is automatically updated as the user uses various DrawFBP facilities.

Sample DrawFBP network
---

Here is a very simple diagram built using DrawFBP, showing process names, names of component source code and class names (used when checking port connections).

![MergeandSort](https://github.com/jpaulm/drawfbp/blob/master/docs/MergeandSort.png "Simple Network Diagram")

JavaHelp facility
---

DrawFBP has a help facility which uses the powerful JavaHelp facility.  The first time you click on Help/Launch Help, you will be prompted to locate the `javahelp.jar` file.  This is the standard JavaHelp jar file, and can be obtained from Maven Central, by doing a search for artifact `javahelp`, or from http://www.jpaulmorrison.com/graphicsstuff/DrawFBP-Help.jar , which has the same contents.

From then on, DrawFBP will remember the location of your DrawFBP-Help.jar file in your `DrawFBPProperties.xml` file.

XML Schema for `.drw` files
---

An XML Schema has been added to the `lib` folder - https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd - specifying the format of the XML files used to hold DrawFBP diagrams. These files have an extension of `.drw`.  This schema can also be used to check whether the file format of any other diagramming tool matches the `.drw` format.

Old `.drw` files can still be displayed using the latest release of DrawFBP (v2.13.0 and following), but will be stored in the new format when they are rebuilt.

External ports for subnets still have to be added.

Running JavaFBP networks generated by DrawFBP
---

JavaFBP networks created by DrawFBP can be run stand-alone: you will need to add the JavaFBP jar file, obtainable from GitHub -  https://github.com/jpaulm/javafbp/releases/download/4.1.0/javafbp-4.1.0.jar, to the Java Build Path of any projects you create. 

If you want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the README file for the `javafbp-websockets` project on GitHub.

Running generated networks under DrawFBP
---

You can now run Java or C# programs under DrawFBP: in this case you will probably need additional jar files or dll files for Java or C#, respectively.  This can be done using the File/Add additional jar/dll files function.  

