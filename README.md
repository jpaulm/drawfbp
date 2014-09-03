CppFBP
===

C++ Implementation of Flow-Based Programming (FBP)

General
---

In computer programming, flow-based programming (FBP) is a programming paradigm that defines applications as networks of "black box" processes, which exchange data across predefined connections by message passing, where the connections are specified externally to the processes. These black box processes can be reconnected endlessly to form different applications without having to be changed internally. FBP is thus naturally component-oriented.

FBP is a particular form of dataflow programming based on bounded buffers, information packets with defined lifetimes, named ports, and separate definition of connections.

One interesting aspect of this implementation is that it supports the scripting language `Lua`, so large parts of your networks can be written in a scripting language if desired.

Web sites for FBP: 
* http://www.jpaulmorrison.com/fbp/
* https://github.com/flowbased/flowbased.org/wiki

Sample Component, Component API and Network Definitions

* http://www.jpaulmorrison.com/fbp/CppFBP.shtml


Prerequisites
---

Download and install Visual C++ 2010 Express (or higher version)

Download and install `Boost` - http://www.boost.org/

Download and install `Lua` - http://www.lua.org/

Update following macros with correct version numbers: `BOOST_INCLUDE`, `BOOST_LIB`, `LUA_INCLUDE`, `LUA_LIB`, as follows:
- Go to `View`/`Property Manager`
- Under `Property Manager`, select `SolutionSettings`
- Expand, showing `Debug`
- Expand, showing `UserMacros`
- Select `UserMacros`
- Go to `UserMacros` under `Common Properties`
- Click on that, revealing the 4 macros; change version numbers if necessary
- Leave `set this macro as an environment variable in the build environment` set to selected
 
or, more simply, in Windows Explorer, just go to `SolutionSettings`, open it, open `UserMacros.props`, and make and save your changes.


Build FBP Project
---

Create empty `cppfbp` directory in your local GitHub directory

Do `git clone https://github.com/jpaulm/cppfbp`

Now go into Visual C++, and `Open/Project/Solution` `CppFBP.sln` (in the just cloned directory)

There will be a "solution" line, followed by a number of "projects" - two of which are `CppFBPCore` and `CppFBPComponents`.
 
Right click on `CppFBPCore` and do a `Build`

Right click on `CppFBPComponents` and do a `Build`

Right click on the "solution" line, and do `Build Solution`

If you get errors, you may have to build individual projects (sometimes this will require more than one try); if you only get warnings, you can proceed


Testing "TimingTest1" (console application)
---

Right click on `TimingTest1` in Solution Explorer; Debug/Start new instance

You should see something like

    Elapsed time in seconds: 35.000
    Press any key to continue . . .



