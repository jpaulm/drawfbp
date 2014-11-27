// TryLuaDyn.cpp : Defines the entry point for the console application.
//

#include "thxdef.h"

#include <stdio.h>
#include<cstdlib>
#include<iostream>
#include<string.h>
#include<fstream>
//#include<dirent.h>
//#define FILE _iobuf

/* This is the interpretive form of TryInfQ */

void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

void main() {
		
	const char* fname =  "TryInfQ.fbp";
	FILE *f;

#ifdef WIN32
    errno_t err;
    if( (err  = fopen_s( &f, fname, "r" )) !=0 ) {
#else
    if ((f = fopen(fname, "r")) == NULL) {
#endif
        fprintf(stderr, "Cannot open source file %s!\n", fname);
    }
	if (f == NULL) {
	//printf("Trying to open: TryInfQ.fbp\n");
    perror ("The following error occurred");
	
	}
  else
    CppFBP(0, DYNAM, f, TIME_REQ);   
	system("pause");  // to see console
}