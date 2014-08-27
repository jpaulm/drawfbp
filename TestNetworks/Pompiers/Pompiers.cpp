#include "thxdef.h"
#include <stdio.h>
/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list
*/
void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);
bool NO_TRACE = false;
bool TRACE = true;
THRCOMP ThFileRd(_anchor anch);
THRCOMP SelPomp(_anchor anch);
THRCOMP ThFileWt(_anchor anch);

proc_ent P0 = {0, "Lire", "ThFileRd", ThFileRd, 0, 0, TRACE, 0};
proc_ent P1 = {&P0, "Select", "SelPomp", SelPomp, 0, 0, TRACE, 0};
proc_ent P2 = {&P1, "Ecrire", "ThFileRt", ThFileWt, 0, 0, TRACE, 0};

IIP I0 = {"C:/Users/Paul/Documents/Business/C++Stuff/CppFBP/Pompiers/POMPIERS.FIL"};
IIP I1 = {"100000"};
IIP I2 = {"C:/Users/Paul/Documents/Business/C++Stuff/CppFBP/Pompiers/RICHES.FIL"};
cnxt_ent C0 = {0, "!", "", 0, "Lire", "OPT", 0, &I0, 0};
cnxt_ent C1 = {&C0, "Lire", "OUT", 0, "Select", "IN", 0, 0, 1};
cnxt_ent C2 = {&C1, "Select", "OUT", 0, "Ecrire", "IN", 0, 0, 1};
cnxt_ent C3 = {&C2, "!", "", 0, "Select", "SAL", 0, &I1, 0};
cnxt_ent C4 = {&C3, "!", "", 0, "Ecrire", "OPT", 0, &I2, 0};

label_ent LABELTAB = {0, " ", "", &C4, &P2, 'L'};


void main() {
CppFBP(&LABELTAB, 0, 0, 0);
}
