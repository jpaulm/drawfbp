// TryLuaDyn.cpp : Defines the entry point for the console application.
//

#include "thxdef.h"

#include <stdio.h>
#include<cstdlib>
#include<iostream>
#include<string.h>
#include<fstream>
//#include<dirent.h>
#define FILE _iobuf

#define BOOST_FILESYSTEM_NO_DEPRECATED
#include <boost/filesystem.hpp>
using namespace boost::filesystem;

/* This is the interpretive form of TryLua */

void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);

void main() {
	bool DYNAM = true;
	bool TIMEREQ = true;
	path p = path("..");
	path q = canonical(p);
	
	std::cout << p;
	std::cout << q;
	int i;
	if (exists(p)) 
		i = 1;
	else
		i = 0;

	FILE * f = fopen("..\\DynNetworks\\TryLuaDyn\\TryLua.fbp", "r"); 
	
	CppFBP(0, DYNAM, f, TIMEREQ);   
}