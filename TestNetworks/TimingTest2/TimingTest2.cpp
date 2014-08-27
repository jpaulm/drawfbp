// TimingTest2.cpp : Defines the entry point for the console application.

#include "thxdef.h"
#include <stdio.h>

#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>

/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list

Using fibres...

This test case took 11 secs with 2,000,000 IPs, and connection capacity of 50; 
assuming create/destroy trivial, send/receive pair takes 110 nanoseconds

Only uses one processor

Using Boost...

       25 secs        - this suggests that send/receive is quite a bit slower!

Uses all 4 processors

  However, there may be too few processes, so we will add 5 more, giving 11 processes, and see what happens...
  We still have 2,000,000 IPs, so 2,000,000 creates and destroys each, 10 sends and receives for each IP

  Took 18 secs - maybe 25 secs was bug!  That seems much better - twice as many processes and not a lot more time...!

  
*/
void CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

THRCOMP ThGenIps(_anchor anch);
THRCOMP ThDrop(_anchor anch);
THRCOMP ThPsThru(_anchor anch);

bool TRACE = false;

proc_ent P00 = {0, "Gen", "ThGenIps",      ThGenIps, 0, 0, TRACE, 0};
proc_ent P01 = {&P00, "Copy0", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};
proc_ent P02 = {&P01, "Copy1", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};
proc_ent P03 = {&P02, "Copy2", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};
proc_ent P04 = {&P03, "Copy3", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};
proc_ent P05 = {&P04, "Copy4", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};  // added
proc_ent P06 = {&P05, "Copy5", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};  // added
proc_ent P07 = {&P06, "Copy6", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};  // added
proc_ent P08 = {&P07, "Copy7", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};  // added
proc_ent P09 = {&P08, "Copy8", "ThPsThru", ThPsThru, 0, 0, TRACE, 0};  // added
proc_ent P10 = {&P09, "Drop", "ThDrop",    ThDrop,   0, 0,  TRACE, 0};
IIP I00 = {"2000000"};   //  was 2000000
cnxt_ent C00 = {0, "!", "", 0, "Gen", "COUNT", 0, &I00, 0};
cnxt_ent C01 = {&C00, "Gen", "OUT", 0, "Copy0", "IN", 0, 0, 50};
cnxt_ent C02 = {&C01, "Copy0", "OUT", 0, "Copy1", "IN", 0, 0, 50};
cnxt_ent C03 = {&C02, "Copy1", "OUT", 0, "Copy2", "IN", 0, 0, 50};
cnxt_ent C04 = {&C03, "Copy2", "OUT", 0, "Copy3", "IN", 0, 0, 50};
cnxt_ent C05 = {&C04, "Copy3", "OUT", 0, "Copy4", "IN", 0, 0, 50};
cnxt_ent C06 = {&C05, "Copy4", "OUT", 0, "Copy5", "IN", 0, 0, 50};
cnxt_ent C07 = {&C06, "Copy5", "OUT", 0, "Copy6", "IN", 0, 0, 50};
cnxt_ent C08 = {&C07, "Copy6", "OUT", 0, "Copy7", "IN", 0, 0, 50};
cnxt_ent C09 = {&C08, "Copy7", "OUT", 0, "Copy8", "IN", 0, 0, 50};
cnxt_ent C10 = {&C09, "Copy8", "OUT", 0, "Drop", "IN", 0, 0, 50};


label_ent LABELTAB = {0, " ", "", &C10, &P10, 'L'};


void main() {
	//_CrtSetDbgFlag(_CrtSetDbgFlag(_CRTDBG_REPORT_FLAG)|_CRTDBG_LEAK_CHECK_DF);
//_CrtSetBreakAlloc(838);  // 3132 989 714
CppFBP(&LABELTAB, 0, 0, 1);  // time required
}
