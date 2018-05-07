DrawFBP
=======

Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies

#### The File Selection function of v2.13.23 had a problem, now fixed in v2.13.24.  This can be obtained by searching for DrawFBP on Maven. Go to https://search.maven.org/ - then search for DrawFBP (not case-sensitive).  Download the .jar file from here, and execute using Java Platform SE binary.  

Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise refinement" or "top-down development" by supporting subnets - blocks in the diagram that can specify lower level diagrams, which can in turn specify lower level ones, and so on.  DrawFBP allows the user to draw a diagram just using short, descriptive names for blocks (nodes) and to fill in either component or subnet names later.  Multiple levels can be held under separate tabs, allowing the user to jump back and forth between different levels of a design.

DrawFBP can generate networks for Java, C#, and NoFlo.  These are kept separate in the DrawFBP dialogs and typically use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP. 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

Six Youtube videos are currently available showing how to use DrawFBP, for drawing diagrams, and generating running JavaFBP networks, using the Eclipse IDE:

- https://www.youtube.com/watch?v=OrKenPOV4Js
- https://www.youtube.com/watch?v=9NXYNxDjFWY
- https://www.youtube.com/watch?v=-AmzfhV2hIU
- https://www.youtube.com/watch?v=F0lKQpIjfVE
- https://www.youtube.com/watch?v=5brTDk8cpNo
- https://youtu.be/IvTAexROKSA  (simple interactive systems using WebSockets, with demo of JavaFBP-WebSockets - JavaFBP and HTML5)

#### Getting Started

To get started using DrawFBP once you have it installed, just click anywhere on the drawing screen, and a block will appear, with a popup prompting you to add a (short) description.  The type of block defaults to "Process", but a number of other block types are available, controlled by by the buttons along the bottom of the screen.

To connect two blocks, click anywhere on the border of the "from" block; then click anywhere on the border of the "to" block - it doesn't matter whether the left mouse button is held down or not. 

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


Running DrawFBP
----

DrawFBP can be executed directly by executing its .jar file.  You can download it from Maven (search for DrawFBP), or from the latest release in the <a href="https://github.com/jpaulm/drawfbp/releases">DrawFBP Releases</a> directory in GitHub; it can then be executed directly by double-clicking on it. 

If you wish to run DrawFBP from the command line, position to the folder containing the DrawFBP jar file, and enter 

    java -jar drawfbp-x.y.z.jar com.jpmorrsn.graphics.DrawFBP (for v2.12.x and lower)
    java -jar drawfbp-x.y.z.jar com.jpaulmorrison.graphics.DrawFBP (for v2.13.0 and higher)
    
Note: if you are displaying a network built using an earlier version of DrawFBP, with some or all of the component classes filled in, you will have to reaccess the component classes, as the naming conventions have changed.

Sample DrawFBP network
---

Here is a very simple diagram built using DrawFBP, showing process names, names of component source code and class names (used when checking port connections).

![MergeandSort](https://github.com/jpaulm/drawfbp/blob/master/docs/MergeandSort.png "Simple Network Diagram")

JavaHelp facility
---

DrawFBP has a help facility which uses the powerful JavaHelp facility.  The first time you click on Help/Launch Help, you will be prompted to locate the `javahelp.jar` file.  This is the standard JavaHelp jar file, and can be obtained from Maven Central, by doing a search for artifact `javahelp`, or from http://www.jpaulmorrison.com/graphicsstuff/DrawFBP-Help.jar , which has the same contents.

From then on DrawFBP will remember the location of your DrawFBP-Help.jar file in your `DrawFBPProperties.xml` file.

XML Schema for `.drw` files
---

An XML Schema has been added to the `lib` folder - https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd - specifying the format of the XML files used to hold DrawFBP diagrams. These files have an extension of `.drw`.  This schema can also be used to check whether the file format of any other diagramming tool matches the `.drw` format.

Old `.drw` files can still be displayed using the latest release of DrawFBP (v2.13.0 and following), but will be stored in the new format when they are rebuilt.

External ports for subnets still have to be added.

Running JavaFBP networks generated by DrawFBP
---

If you wish to run any networks that you create with DrawFBP, you will need to add the JavaFBP jar file, obtainable from GitHub -  https://github.com/jpaulm/javafbp/releases/download/4.1.0/javafbp-4.1.0.jar, to the Java Build Path of any projects you create. 

If you want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the README file for the `javafbp-websockets` project on GitHub.

