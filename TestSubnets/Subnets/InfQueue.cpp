
#include "StdAfx.h"
#include "thxdef.h"
#include <stdio.h>
#define FILE struct _iobuf

void  CppSub(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq, _anchor proc_anchor);

THRCOMP InfQueue(_anchor proc_anchor)
{
	
THRSUBC  ThFileRd(anchor anch);
THRSUBC  ThFileWt(anchor anch);
THRSUBC  SubIn(anchor anch);
THRSUBC  SubOut(anchor anch);

proc_ent P0 = {0, "SubIn", "SubIn", SubIn, 0, 0, !TRACE, 0};
proc_ent P1 = {&P0, "Ecrire", "ThFileWt", ThFileWt, 0, 0, !TRACE, 0};
proc_ent P2 = {&P1, "Lire", "ThFileRd", ThFileRd, 0, 0, !TRACE, 0};
proc_ent P3 = {&P2, "SubOut", "SubOut", SubOut, 0, 0, !TRACE, 0};


IIP I0 = {"IN"};
cnxt_ent C0 = {0, "!", " ", 0, "SubIn", "NAME", 0, &I0, 0};
cnxt_ent C1 = {&C0, "SubIn", "OUT", 0, "Ecrire", "IN", 0, 0, 6};
IIP I1 = {"c:\\temp\\infqueue.dat"};
cnxt_ent C2 = {&C1, "!", " ", 0, "Ecrire", "OPT", 0, &I1, 0};

cnxt_ent C3 = {&C2, "Ecrire", "*", 0, "Lire", "*", 0, 0, 6};

cnxt_ent C4 = {&C3, "!", " ", 0, "Lire", "OPT", 0, &I1, 0};
IIP I2 = {"OUT"};
cnxt_ent C5 = {&C4, "!", " ", 0, "SubOut", "NAME", 0, &I2, 0};
cnxt_ent C6 = {&C5, "Lire", "OUT", 0, "SubOut", "IN", 0,  0, 6};

label_ent LABELTAB = {0, " ", "", &C6, &P3, 'L'};

//startSubnet:
CppSub(&LABELTAB, 0, 0, 0, proc_anchor);
return(0);	
}