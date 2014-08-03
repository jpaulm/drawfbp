DrawFBP
=======

Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies


Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise decomposition" by supporting subnets - blocks in the diagram can specify lower level diagrams,which can in turn specify lower level ones, and so on.   This allows the user to "zoom in" to a lower level, and then pop back up to the original diagram.

DrawFBP supports groups of related languages: Java and languages using the same JVM, C#, and NoFlo.  These are kept separate in the DrawFBP dialogs and typically will use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP (under development). 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . 

To install DrawFBP, do a mvn install on your DrawFBP folder repository loaded from https://github.com/jpaulm/drawfbp .

You will need to add the JavaFBP jar file, obtainable from GitHub - https://github.com/jpaulm/javafbp - or J. Paul Morrison's web site www.jpaulmorrison.com/fbp, to the Java Build Path of any projects you create. 

DrawFBP Help
----

The first time you click on Help/Launch Help, you will get a message asking if you want to locate the DrawFBP-Help jar file.  If you click on OK, you will see a file chooser panel. Go to your project folder, then src/main/resources, and you should see DrawFBP-Help.jar.  Select that, and you should see the Table of Contents of the Help facility and the Overview screen.  From then on, DrawFBP will remember the location of the DrawFBP-Help jar file. This file can be (re)located at any time later by clicking on File/Locate DrawFBP Help File. 


Running DrawFBP
----

To run DrawFBP, go to your DrawFBP project directory, and enter

    target\drawfbp-x.x.x-SNAPSHOT.jar
    
where x.x.x is currently 2.8.14  

