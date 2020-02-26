#pragma comment(lib, "CppFBPCore")


#pragma comment(lib, "CppFBPComponents")


#include "thxdef.h"
#include <stdio.h>

/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list

Trying to force deadlock

*/
void CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

THRCOMP ThGenIps(_anchor anch);
THRCOMP ThReplStr(_anchor anch);
THRCOMP ThConcatStr(_anchor anch);
THRCOMP ThDrop(_anchor anch);

proc_ent P00 = {0, "Gen", "ThGenIps", ThGenIps, 0, 0,  0, 0};
proc_ent P01 = {&P00, "Repl", "ThReplStr", ThReplStr, 0, 0,  0, 0};
proc_ent P02 = {&P01, "Concat", "ThConcatStr", ThConcatStr, 0, 0,  0, 0};
proc_ent P03 = {&P02, "Drop", "ThDrop", ThDrop, 0, 0,  0, 0};

IIP I00 = {"2000000"};
cnxt_ent C00 = {0, "!", "", 0, "Gen", "COUNT", 0, &I00, 0};
cnxt_ent C01 = {&C00, "Gen", "OUT", 0, "Repl", "IN", 0, 0, 50};
cnxt_ent C02 = {&C01, "Repl", "OUT", 0, "Concat", "IN", 0, 0, 50};
cnxt_ent C03 = {&C02, "Repl", "OUT", 1, "Concat", "IN", 1, 0, 50};
cnxt_ent C04 = {&C03, "Repl", "OUT", 2, "Concat", "IN", 2, 0, 50};
cnxt_ent C05 = {&C04, "Concat", "OUT", 0, "Drop", "IN", 0, 0, 50};



label_ent LABELTAB = {0, " ", "", &C05, &P03, 'L'};


void main() {
CppFBP(&LABELTAB, 0, 0, 1);  // time required
}
