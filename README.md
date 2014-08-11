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

Running and/or Installing DrawFBP
----

If you just want to run DrawFBP, you can download the latest version (currently 2.8.15) from J. Paul Morrison's web site http://www.jpaulmorrison.com/graphicsstuff/drawfbp-2.8.15-SNAPSHOT.jar . 

Alternatively, you can install the code, from https://github.com/jpaulm/drawfbp .  Then do an `mvn package` on your DrawFBP folder repository.  In this case, you can run DrawFBP by going to your DrawFBP project directory, and enter

    java -jar target\drawfbp-2.8.15-SNAPSHOT.jar
    
If you wish to run any networks that you create with DrawFBP, you will need to add the JavaFBP jar file, obtainable from GitHub - https://github.com/jpaulm/javafbp/releases/download/v1.0/fbp-1.0-SNAPSHOT.jar or J. Paul Morrison's web site www.jpaulmorrison.com/fbp/fbp-1.0-SNAPSHOT.jar, to the Java Build Path of any projects you create. If You want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the Readme file for the javafbp project on GitHub.

DrawFBP Help
----

The first time you click on Help/Launch Help, you will get a message asking if you want to locate the DrawFBP-Help jar file.  If you click on OK, you will see a file chooser panel. Go to your project folder, then target\classes, and you should see DrawFBP-Help.jar.  Select that, and you should see the Table of Contents of the Help facility and the Overview screen.  From then on, DrawFBP will remember the location of the DrawFBP-Help jar file. This file can be (re)located at any time later by clicking on File/Locate DrawFBP Help File. 


