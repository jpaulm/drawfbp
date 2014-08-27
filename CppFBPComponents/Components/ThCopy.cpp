#include "StdAfx.h"
#include "dllheader.h"

#include <string.h>
#include "compsvcs.h"
// This component simply makes a shallow copy, drops the original, and sends out the copy

THRCOMP ThCopy(_anchor proc_anchor)
{
	void *ptr;
	void *ptr2;
	int value;
	long size;
	char *type;
	port_ent port_tab[2];

	value = dfsdfpt(proc_anchor, 2, port_tab,"IN","OUT");

	value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	while (value == 0) {
		value = dfscrep(proc_anchor, &ptr2, size, type);
		memcpy(ptr2,ptr,size);
		value = dfssend(proc_anchor, &ptr2, &port_tab[1], 0);  
		dfsdrop(proc_anchor, &ptr);
		if (value != 0) {
			dfsdrop(proc_anchor, &ptr);
			return(1);
		}
		value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	}

	return(0);
}

