
#include "stdafx.h"

#pragma comment(lib, "CppFBPCore")
#pragma comment(lib, "CppFBPComponents")

#include "thxdef.h"

#include <stdio.h>
#define FILE struct _iobuf

/* This is not the THREADS internal structure - this 
is a structured representation of the free-form connection list
*/
void  CppFBP(label_ent* label_blk, bool dynam,  FILE * fp, bool timereq);

THRCOMP ThFileWt(_anchor anch);
THRCOMP InfQueue(_anchor anch);
THRCOMP ThFileRd(_anchor anch);

int cap = 2;
int elem0 = 0;
/*
This test case links to InfQueue statically, and then runs it under the "Copy" process
*/

proc_ent P0 = {NULL, "Read", "ThFileRd", ThFileRd, NULL, NULL,  !TRACE, !COMPOS};
proc_ent P1 = {&P0, "Write", "ThFileWt", ThFileWt, NULL, NULL,  !TRACE, !COMPOS};
proc_ent P2 = {&P1, "Copy", "InfQueue", InfQueue, NULL, NULL,  !TRACE, COMPOS};

IIP I0 = {"..\\..\\TestData\\POMPIERS.FIL"};
IIP I1 = {"..\\..\\TestData\\output.fil"};
cnxt_ent C0 = {NULL, "!", "", 0, "Read", "OPT", elem0, &I0, 0};
cnxt_ent C1 = {&C0, "Read", "OUT", elem0, "Copy", "IN", elem0, NULL, cap};
cnxt_ent C2 = {&C1, "!", "", 0, "Write", "OPT", elem0, &I1, 0};
cnxt_ent C3 = {&C2, "Copy", "OUT", elem0, "Write", "IN", elem0, NULL, cap};


label_ent LABELTAB = {NULL, " ", "", &C3, &P2, 'L'};


void main() {
CppFBP(&LABELTAB, !DYNAM, NULL, TIME_REQ);  // time required
}
