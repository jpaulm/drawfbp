
// TryLua.cpp : Defines the entry point for the console application.
//
/*

44 secs in its present form - running on 4 processors

1,000,000 IPs running through basically 3 connections (2 in parallel, plus 2 in sequence) - so roughly 3,000,000 recevs, 3,000,000 sends,
giving 14 microsecs per send/receive pair - not bad given that all app logic is in Lua!  Which could probably be speeded up by compiling the Lua text... 

Using a non-looper (recvr.lua) in place of drop.lua multiplies the run time enormously, which is not surprising given the time required to fire up 
the Lua environment on every non-looper activation - so I wouldn't recommend doing that... but it works!

*/

#include "stdafx.h"

#pragma comment(lib, "CppFBPCore")

#if 0
#pragma comment(lib, "CppFBPComponents")		// static lib
#else
#pragma comment(lib, "CppFBPComponentsDll")		// dll
#endif

#pragma comment(lib, "lua53")

#include "thxdef.h"
#include <stdio.h>

#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>

/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list

*/
void CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

THRCOMP ThLua(_anchor anch);


proc_ent P00 = {0, "Gen", "ThLua", ThLua, 0, 0, !TRACE, 0};
proc_ent P01 = {&P00, "Gen2", "ThLua", ThLua, 0, 0, !TRACE, 0};
proc_ent P02 = {&P01, "Concat", "ThLua",  ThLua, 0, 0, !TRACE, 0};
proc_ent P03 = {&P02, "Repl", "ThLua",  ThLua, 0, 0, !TRACE, 0};
proc_ent P04 = {&P03, "Drop", "ThLua",  ThLua, 0, 0, !TRACE, 0};

cnxt_ent C00 = {0, "Gen", "OUT", 0, "Concat", "IN", 0, 0, 2};
cnxt_ent C01 = {&C00, "Gen2", "OUT", 0, "Concat", "IN", 1, 0, 2};
cnxt_ent C025 = {&C01, "Concat", "OUT", 0, "Repl", "IN", 0, 0, 2};
cnxt_ent C02 = {&C025, "Repl", "OUT", 0, "Drop", "IN", 0, 0, 2};

IIP I00 = {"500000"};
IIP I01 = {"..\\..\\LuaScripts\\gen.lua"};                                      // creates and sends 'COUNT' IPs  
cnxt_ent C03 = {&C02, "!", "", 0, "Gen", "COUNT", 0, &I00, 0};
cnxt_ent C04 = {&C03, "!", "", 0, "Gen", "PROG", 0, &I01, 0};

 
IIP I02 = {"500000"};
IIP I03 = {"..\\..\\LuaScripts\\gen.lua"};                                      // creates and sends 'COUNT' IPs  
cnxt_ent C05 = {&C04, "!", "", 0, "Gen2", "COUNT", 0, &I02, 0};
cnxt_ent C06 = {&C05, "!", "", 0, "Gen2", "PROG", 0, &I03, 0};
 

IIP I04 = {"..\\..\\LuaScripts\\concat.lua"};                                       // receives from IN[0] then IN[1], etc. sending to OUT
cnxt_ent C07 = {&C06, "!", "", 0, "Concat", "PROG", 0, &I04, 0};

IIP I05 = {"..\\..\\LuaScripts\\repl.lua"};                                       // creates copies and sends to OUT[0], OUT[1], etc. dropping original
cnxt_ent C08 = {&C07, "!", "", 0, "Repl", "PROG", 0, &I05, 0};

IIP I06 = {"..\\..\\LuaScripts\\drop.lua"};                                        // looper
//IIP I06 = {"C:/Users/Paul/Documents/Business/C++Stuff/CppFBP/TestNetworks/TryLua/recvr.lua"};                                     
cnxt_ent C09 = {&C08, "!", "", 0, "Drop", "PROG", 0, &I06, 0};

label_ent LABELTAB = {0, " ", "", &C09, &P04, 'L'};


void main() {
	//_CrtSetDbgFlag(_CrtSetDbgFlag(_CRTDBG_REPORT_FLAG)|_CRTDBG_LEAK_CHECK_DF);
//_CrtSetBreakAlloc(838);  // 3132 989 714
CppFBP(&LABELTAB, 0, 0, 1);  // time required
}
