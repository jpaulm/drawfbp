#include "thxdef.h"

#include <stdio.h>
#define FILE _iobuf

/* This is the interpretive form of CopyFile */

void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

void main() {
	bool DYNAM = true;
	bool TIMEREQ = true;

	FILE * f = fopen("..\\CopyFileDyn\\CopyFile.fbp", "r"); 
	
	CppFBP(0, DYNAM, f, TIMEREQ);   
}
