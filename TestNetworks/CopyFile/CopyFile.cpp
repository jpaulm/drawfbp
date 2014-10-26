#include "thxdef.h"

#include <stdio.h>
#define FILE struct _iobuf

/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list
*/
void  CppFBP(label_ent* label_blk, bool dynam,  FILE * fp, bool timereq);

//__declspec(dllimport/dllexport) keywords, combined with extern "C"


THRCOMP ThFileWt(_anchor anch);
//THRCOMP ThCopy(_anchor anch);
THRCOMP ThCopyNL(_anchor anch);
THRCOMP ThFileRt(_anchor anch);

#define TRACE true
#define COMPOS true
#define DYNAM true
#define TIME_REQ true

int cap = 2;
int elem0 = 0;


proc_ent P0 = {NULL, "Read", "ThFileRt", ThFileRt, NULL, NULL,  !TRACE, !COMPOS};
proc_ent P1 = {&P0, "Show", "ThFileWt", ThFileWt, NULL, NULL,  !TRACE, !COMPOS};
proc_ent P2 = {&P1, "Copy", "ThCopyNL", ThCopyNL, NULL, NULL,  !TRACE, !COMPOS};
//proc_ent P2 = {&P1, "Copy", "ThCopy", ThCopy, NULL, NULL,  !TRACE, !COMPOS;

IIP I0 = {"..\\..\\TestData\\POMPIERS.FIL"};
IIP I2 = {"..\\..\\TestData\\output.fil"};
cnxt_ent C0 = {NULL, "!", "", 0, "Read", "OPT", elem0, &I0, 0};
cnxt_ent C1 = {&C0, "Read", "OUT", elem0, "Copy", "IN", elem0, NULL, cap};
cnxt_ent C2 = {&C1, "!", "", 0, "Show", "OPT", elem0, &I2, 0};
cnxt_ent C3 = {&C2, "Copy", "OUT", elem0, "Show", "IN", elem0, NULL, cap};


label_ent LABELTAB = {NULL, " ", "", &C3, &P2, 'L'};


void main() {
CppFBP(&LABELTAB, !DYNAM, NULL, TIME_REQ);  // time required
}
