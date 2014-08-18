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


Prerequisites
---

The project requires Gradle for building (tested with version 2.0). You can download the corresponding package from the following URL: 
http://www.gradle.org

Windows and Linux users should follow the installation instructions on the Maven website (URL provided above).

OSX users (using Brew, http://brew.sh) can install Maven by executing the following command:

    brew install gradle

Eclipse IDE Integration
---

You can generate Eclipse project using the following mvn command:

    gradle eclipse

If you already created an Eclipse project you can run:

    gradle cleanEclipse Eclipse

You need to install a Gradle plugin for Eclipse as explained here:
https://github.com/spring-projects/eclipse-integration-gradle/
Then import a generated project in Eclipse, right (ctrl for OSX) click on the project in Eclipse -> Configure -> Convert to Gradle Project. After the conversion you can Right (ctrl for OSX) click on the project -> Gradle -> Task Quick Launcher and type `build`.

Building from command line
---

For building the project simply run the following command:

    gradle build

As a result a `DrawFBP-2.8.15.jar` file will be created in the `build/libs` directory. 


Running and/or Installing DrawFBP
----

You can run DrawFBP by going to your DrawFBP project directory, and entering

    gradle build
    java -cp "build\libs\drawfbp-2.8.15.jar;lib\DrawFBP-Help.jar" com.jpmorrsn.graphics.DrawFBP
    
(note the quotes)    

(Note also, `gradle run` will run the drawing tool, but will not provide the Help facility)

Alternatively, run `gradle installApp` and you will find start scripts in `build\install\drawfbp\bin'.

Running JavaFBP networks
---

If you wish to run any networks that you create with DrawFBP, you will need to add the JavaFBP jar file, obtainable from GitHub - https://github.com/jpaulm/javafbp/releases/download/v1.1/javafbp-2.9.jar, or from J. Paul Morrison's web site www.jpaulmorrison.com/fbp/JavaFBP-2.9.jar, to the Java Build Path of any projects you create. If you want to run an app using JavaFBP WebSockets, you will need the jar file for that as well, as described in the Readme file for the javafbp project on GitHub.

