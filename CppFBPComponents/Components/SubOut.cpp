//#pragma comment(lib, "CppFBPCore")
#include "StdAfx.h"
#include "dllheader.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "compsvcs.h"
#include "thzcbs.h"
#define OUTPUT 1

THRCOMP SubOut(_anchor proc_anchor)
{
	void *ptr;	
	char pname[256];
	int value;
	long size;
	char *type;
	port_ent port_tab[2];

	port_ent mother_port;	

	value = dfsdfpt(proc_anchor, 2, port_tab,"NAME","IN");

	value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);

	memcpy(pname,ptr,size);
	pname[size] = '\0';

	value = dfsdrop(proc_anchor, &ptr);

	Process* proc = (Process *) proc_anchor.reserved;
	proc = proc -> mother_proc;

	Port* cpp = proc -> out_ports;
	while (cpp != 0)
	{
		if (0 == strcmp(cpp->port_name, pname))
			break;
		cpp = cpp -> succ;
	}

	if (cpp == 0) {
		printf ("Port name %s not found\n",
			pname); 
		return(8);
	}

	strcpy(mother_port.port_name, cpp -> port_name);
	//cpp -> direction = OUTPUT;
	mother_port.cpptr = cpp;
	mother_port.elem_count = cpp -> elem_count;
	mother_port.ret_code = 0;
	

	value = dfsrecv(proc_anchor, &ptr, &port_tab[1], 0, &size, &type);
	while (value == 0) {
		value = dfssend(proc_anchor, &ptr, &mother_port, 0);
		value = dfsrecv(proc_anchor, &ptr, &port_tab[1], 0, &size, &type);
	}

	return(0);
} 