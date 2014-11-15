//#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>
//#include <malloc.h>
#include <string.h>
#include "thxiip.h"
#include "cppfbp.h"
#define FALSE 0
#define TRUE 1
#define INPUT 0
#define OUTPUT 1

void extern thziclos(Process *proc, Port  * cpp, int elem_no);

int thzclos(Process *proc, port_ent *peptr, int elem_no);

int thzclosc(Process *proc, char * port ){
	int elem_no = 0;
	char port_name[100];
	char * p = strchr(port, '[');
	char * q;
	port_ent pe;

	if (p == 0) 
		strcpy (port_name, port);
	else {
		q = strchr(p, ']');
		long n = q - p - 1;
		char no[10];		
		strncpy(no, p + 1, n);
		elem_no = atoi(no);
		char * r = port;
		strncpy (port_name, port, p - r);
		port_name[p - r] = '\0';
	}

	//port_ent* pe = (port_ent *) malloc(sizeof(port_ent));   

	strcpy(pe.port_name, port_name);
	Port * cpp = proc -> in_ports;
	while (cpp != 0)
	{
		
			if (0 == strcmp(cpp -> port_name, port_name)){
				break;
			}
		 
		cpp = cpp -> succ;
	}
	pe.cpptr = cpp;
	pe.elem_count = cpp -> elem_count;  
	pe.ret_code = 0;
	
	int value = thzclos(proc, &pe, elem_no);

	//free (pe);
	return value;
}

int thzclos(Process *proc, port_ent *peptr, int elem_no)
{
	//Process *sptr;
	Port *cpp;
	//Cnxt *cnp;
	//IPh   *IPptr;
	//IPh   *nextIP;

	cpp = (Port *)peptr -> cpptr;
	if (peptr -> ret_code == 2)
		return(2);     

	if (cpp -> elem_list[elem_no].closed)
		return(1);

	if (proc->trace) MSG1("%s Close\n",proc -> procname);

	thziclos(proc, cpp, elem_no);

	
	cpp -> elem_list[elem_no].closed = TRUE;

	if (proc->trace) MSG1("%s Close end\n", proc -> procname);
	return(0);
}

