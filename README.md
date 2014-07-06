DrawFBP
=======

Tool for Creating and Exploring Flow-Based Programming Diagram Hierarchies


Description
-----------

DrawFBP is a picture-drawing tool that allows users to create multi-level diagrams implementing the technology and methodology known as Flow-Based Programming (FBP).  Diagrams are saved in DrawFBP XML format, and can actually be used to generate JavaFBP networks, which can then be compiled and run on an IDE such as Eclipse.

DrawFBP supports "stepwise decomposition" by supporting subnets - blocks in the diagram can specify lower level diagrams,which can in turn specify lower level ones, and so on.   This allows the user to "zoom in" to a lower level, and then pop back up to the original diagram.

DrawFBP supports groups of related languages: Java and languages using the same JVM, C#, and NoFlo.  These are kept separate in the DrawFBP dialogs and typically will use different libraries.

DrawFBP also generates a network definition in .fbp notation.  This was originally defined by Wayne Stevens, and has been somewhat modified for NoFlo.  It will also be usable as input to the C++ implementation, called CppFBP (under development). 

For information about FBP in general, see the FBP web site - http://www.jpaulmorrison.com/fbp . For information on JavaFBP in particular, see http://www.jpaulmorrison.com/fbp/index.shtml#JavaFBP .

To install DrawFBP, extract the included jar files for DrawFBP and jhall.jar from DrawFBPInstaller-2.8.zip, and install them on the same directory.  If you click on Help/Launch Help and DrawFBP cannot process it, go into File/Locate JavaHelp Jar File, and select the jhall file you have just downloaded.  Help should now work.

You will need to add the JavaFBP jar file, obtainable from J. Paul Morrison's web site www.jpaulmorrison.com/fbp, or from SourceForge, to the Java Build Path of any projects you create. 
