//#pragma comment(lib, "CppFBPCore")
#include "StdAfx.h"
#include "dllheader.h"
#include "compsvcs.h"
#include <string.h>

THRCOMP ThPsThru(_anchor proc_anchor)

// This component simply sends what it receives
{
	void *ptr;
	int value;
	long size;
	char *type;
	port_ent port_tab[2];

	value = dfsdfpt(proc_anchor, 2, port_tab,"IN","OUT");

	value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	while (value == 0) {		
		value = dfssend(proc_anchor, &ptr, &port_tab[1], 0);  
		
		if (value != 0) {
			dfsdrop(proc_anchor, &ptr);
			return(1);
		}
		value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	}

	return(0);
}

