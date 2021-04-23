DrawFBP
=======

#### Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies

General web site on Flow-Based Programming: https://jpaulm.github.io/fbp/ .

Latest release is `v2.21.7`: the jar file (which includes the `math.geom2d` and `JavaHelp` jar files) - `drawfbp-2.21.7.jar` - can be obtained from the Releases folder (click on `tags`, then `Releases`), from `build/libs`, or Maven (shortly).  

If you click on the Maven shield below to obtain DrawFBP, select `Download` and `jar`. 


**Note:**  `fbp.json` in `docs` does not download correctly:  please do a copy and paste from https://github.com/jpaulm/drawfbp/blob/master/docs/fbp.json (current version thanks to Henri Bergius of NoFlo)

[![Maven Central](https://img.shields.io/maven-central/v/com.jpaulmorrison/drawfbp.svg?label=DrawFBP)](https://search.maven.org/search?q=g:%22com.jpaulmorrison%22%20AND%20a:%22drawfbp%22)


Sample DrawFBP network
---

Here is a simple diagram built using DrawFBP, courtesy of Bob Corrick, showing process names, IIPs, and actual class names (used for generating runnable code, and also when checking port connections).

![FilterByFirstValue](https://github.com/jpaulm/drawfbp/blob/master/docs/FilterByFirstValue.png "Simple Network Diagram")

Running DrawFBP
---

To run from the command line, do a `cd` to wherever you have stored your DrawFBP jar file, and enter:

<code>java -jar drawfbp-x.y.z.jar</code>   where `x.y.z` is the version number. 

If you have downloaded the whole DrawFBP project, you can position to your DrawFBP folder, and then enter this command to run DrawFBP:

<code>java -jar build\libs\drawfbp-x.y.z.jar</code>   where `x.y.z` is the version number. 

<!-- In either case, this command may be followed with the location of a `.drw` file, as of `v2.20.10`, e.g.

    cd ....GitHub\drawfbp
    java -jar build\libs\drawfbp-x.y.z.jar C:\Users\Paul\Documents\GitHub\drawfbp\Testing.drw

You can also associate your copy of the DrawFBP jar file permanently with all occurrences of the `.drw` extension (using Windows facilities).

-->

Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise refinement" or "top-down development" by supporting subnets - blocks in the diagram that can specify lower level diagrams, which can in turn specify lower level ones, and so on.  DrawFBP allows the user to draw a diagram just using short, descriptive names for blocks (nodes) and to fill in either component or subnet names later.  Multiple levels can be held under separate tabs, allowing the user to jump back and forth between different levels of a design.

Alternatively, complex diagrams can be turned into multi-level diagrams by using the "excise" function of the Enclosure block: a group of blocks can be converted into a separate subnet and replaced by a "subnet" block, containing appropriate "external port" blocks, in one operation (see Youtube DrawFBP 5 - https://www.youtube.com/watch?v=5brTDk8cpNo around 9:06).  

DrawFBP can generate networks for Java, C#, and NoFlo (JSON).  These are kept separate in the DrawFBP dialogs and typically use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP. 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

Six Youtube videos are currently available showing how to use DrawFBP, for drawing diagrams, and generating running JavaFBP networks, using the Eclipse IDE - see below...


#### Getting Started with your Diagram

To get started using DrawFBP once you have downloaded it, start it going, then click anywhere on the drawing screen, and a block will appear, with a popup prompting you to add a (short) description.  The type of block defaults to "Process", but a number of other block types are available, controlled by the buttons along the bottom of the screen.

To indicate a connection between two blocks, click anywhere on the border of the "from" block; then hold down the button and drag the mouse to an edge of the "to" block.  You should see a small blue circle appear where you can click the end of the arrow. 

Other features are described in the Help facility.


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
- New Functions (as of `v2.16.1`): 
        - Compile Java program
        - Run Java program
- New Functions (as of `v2.16.5`):
        - Compile C# program
        - Run C# program
- As of `v2.18.1`, File Chooser now displays date and time on non-jar items, and entries can be sorted by name (ascending) or by date/time (descending)      
- Compare two diagrams
- When saving generated network, code compares package name against directory structure, and adjusts package name if they don't match
- As of `v2.20.10`, you can associate `drawfbp.bat` with the file suffix `.drw`, using Windows facilities


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
              
Running DrawFBP (2)
----
DrawFBP can be executed directly by executing its jar file, but, as of v2.15.10, it needs the 2D geometry jar file.  The combination is called a "fat jar" file - before v2.18.1, the jar name contained `all-`; from v2.18.1 on, the `all-` has been dropped.  

DrawFBP requires Java 1.7 or later.

If you want access to the Java annotations of your components, add the jar file(s) containing them (at least JavaFBP and possibly others) to the project Properties/Build Path (for Eclipse), or e.g. type on the command line  

        java -cp "build/libs/drawfbp-x.y.z.jar;..\javafbp\build\libs\javafbp-4.1.2.jar" com.jpaulmorrison.graphics.DrawFBP
        
where `x.y.z` is the DrawFBP version number.

- to run under Linux, replace the semi-colon(s) with colon(s).   
    
Note: if you are displaying a network built using a pre-v2.13.0 version of DrawFBP, with some or all of the component classes (from JavaFBP) filled in, you will have to reaccess the component classes, as the naming conventions have changed slightly.

**DrawFBP properties** are held in a file called <code>DrawFBPProperties.xml</code> in the user's home directory.  If this does not exist, it will be created the first time the user runs DrawFBP - it is automatically updated as the user uses various DrawFBP facilities.

Compare facility
---

DrawFBP now (as of v2.19.0) lets you compare two diagrams, indicating which blocks and lines have been added or deleted.  In the case of deleted blocks and lines, the Compare facility paints the old block (called a "ghost") and/or line in pale gray on the new diagram.   

Added blocks and lines are marked with an "A" symbol, deleted blocks and lines with a "D" symbol, and deleted blocks also with the word "ghost".

XML Schema for `.drw` files
---

An XML Schema has been added to the `lib` folder - https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd - specifying the format of the XML files used to hold DrawFBP diagrams. These files have an extension of `.drw`.  This schema can also be used to check whether the file format of any other diagramming tool matches the `.drw` format.

Old `.drw` files can still be displayed using the latest release of DrawFBP (v2.13.0 and following), but will be stored in the new format when they are rebuilt.

External ports for subnets still have to be added.

Running networks generated by DrawFBP
---

**When you set up a Java project, for use with DrawFBP, it is recommended that you specify the output classes to be in the `bin` directory, rather than `target/classes`.**

JavaFBP or C#FBP networks created by DrawFBP can be run stand-alone: for Java, you will need to add the JavaFBP jar file, obtainable from GitHub -  do a Maven search for javafbp - click on `Download`, and then `jar` - to the Java Build Path of any projects you create. 

If you want to run an app using JavaFBP-WebSockets, you will need the jar file for that as well, as described in the README file for the `javafbp-websockets` project on GitHub - it is also on Maven.

Just as any necessary Java jar files can be obtained from the JavaFBP project on GitHub, to run C# applications using FBP you will need `.dll` files for `FBPLib` and `FBPVerbs`, obtained from the C#FBP libraries - https://github.com/jpaulm/csharpfbp .

`jar` files and `dll` files can be added to your project by using the `File/Add additional jar/dll files` function.  

