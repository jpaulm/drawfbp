#include "thxdef.h"

#include <stdio.h>
#include<cstdlib>
#include<iostream>
#include<string.h>
#include<fstream>
//#include<dirent.h>
#define FILE _iobuf

/* This is the interpretive form of CopyFile */

void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

void main() {
	bool DYNAM = true;
	bool TIMEREQ = true;

	FILE * f = fopen("CopyFile.fbp", "r"); 
	if (f == NULL) {
		printf("Trying to open: CopyFile.fbp\n");
    perror ("The following error occurred");
	system("pause");  // to see console
	}
  else
	CppFBP(0, DYNAM, f, TIMEREQ);   
}
