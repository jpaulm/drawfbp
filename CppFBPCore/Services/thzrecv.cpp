//#include <setjmp.h>
#include <stdio.h>
#include <string.h>
#include "thxiip.h"
#include "cppfbp.h"
#include <stdlib.h>
//#define FALSE 0
//#define TRUE 1
#define INPUT 0
#define OUTPUT 1

int thzrecv(Process *proc, void **ptr, port_ent *peptr, int elem_no,
	long *size, char **typep);

int thzcrep(Process *proc, void **ptr, long IPsize, char *type);

int thzrecvc(Process *proc, void **ptr, char * port, 
	long *size, char **typep)
{
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
	cp * cpp = proc -> in_cps;
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
	
	int value = thzrecv(proc, ptr, &pe, elem_no, size, typep);

	//free (pe);
	return value;
}


int thzrecv(Process *proc, void **ptr, port_ent *peptr, int elem_no,
	long *size, char **typep)
{

	/*  This service returns the data part of an IP - if info is needed from the header,
	you have to back up over the length of the header */

	//Process *sptr;
	IPh   *IPptr;
	//IPh   *optr;
	IP   *tptr;
	int value = 0;
	cp *cpp;
	Cnxt *cnp;
	IIP *IIPptr;
	IPh   *created_IIP_ptr;
	void *created_ptr;
	//extern void run(Process * proc);

	if (peptr -> ret_code == 2) {
		if (proc -> trace) MSG1("%s Recv from unconnected port\n", proc -> procname);
		return(2);
	}
	cpp = (cp *)peptr -> cpptr;
	if (proc -> trace) MSG3("%s Recv start %s[%d]\n", proc -> procname,
		cpp -> port_name, elem_no);

	if (cpp -> elem_list[elem_no].is_IIP) {
		IIPptr = cpp -> elem_list[elem_no].gen.IIPptr;
		int j = strlen(IIPptr -> datapart);
		value = thzcrep(proc, &created_ptr, j + 1, "OPTIONS");
		created_IIP_ptr = (IPh   *) created_ptr - 1;
		memcpy (created_ptr, IIPptr -> datapart, j + 1);
		cpp -> elem_list[elem_no].closed = TRUE;
		*ptr = created_ptr;
		*size = created_IIP_ptr -> IP_size;
		*typep = created_IIP_ptr -> type;

		if (proc -> trace) MSG1("%s Recv end\n", proc -> procname);
		return(0);
	}
	
	cnp = (Cnxt *) cpp -> elem_list[elem_no].gen.connxn;
	if (cnp == 0) return(2);
	
	//cnp ->lock.lock();
	//cnp -> lock = boost::unique_lock<boost::mutex> (cnp -> mtx); 
	boost::unique_lock<boost::mutex> lock (cnp -> mtx);
	if (cnp->closed && cnp->first_IPptr == 0) {     // connection closed AND drained
		if (proc -> trace) MSG1("%s Recv end of stream\n", proc -> procname);
		value = 2;
		goto retn;		
	}

	//if (proc -> trace) MSG3("%s Recv start %s[%d]\n", proc -> procname,
	//	cpp -> port_name, elem_no);

	if (elem_no < 0 || elem_no >= cpp ->elem_count) {
		MSG2("%s %s RECV Element Number negative or too high\n",
			proc -> procname, peptr -> port_name);
		value = 3;
		goto retn;			
	}
	if (cpp ->direction != INPUT) {
		MSG2("%s %s RECV Wrong direction\n",
			proc -> procname, peptr -> port_name);
		value = 3;
		goto retn;	
	}


	if (cpp -> elem_list[elem_no].closed) {
		if (proc -> trace) MSG1("%s Recv end no data\n", proc -> procname);
		value = 1;
		goto retn;
	}

			
	for (;;) {
		
		if (cnp -> first_IPptr != 0) goto X;

		if (cnp -> nonterm_upstream_proc_count == 0  /* ||
			cpp -> elem_list[elem_no].closed */ )
		{
			if (proc -> trace) MSG1("%s Recv end no data\n", proc -> procname);
			value = 1;
			goto retn;
		}

		proc -> status = SUSPENDED_ON_RECV;
		proc -> waiting_cnxt = cnp;
		if (proc -> trace) MSG1("%s Recv susp\n", proc -> procname);
		cnp-> buffer_not_empty.wait(lock);
		
		if (proc -> trace) MSG1("%s Recv resume\n", proc -> procname);
		proc -> status = ACTIVE;
		proc -> waiting_cnxt = 0;
	}

X: IPptr = cnp -> first_IPptr;
	if ((cnp -> first_IPptr = IPptr -> next_IP) == 0)
		cnp -> last_IPptr = 0;

	tptr = (IP *) IPptr;
	if (tptr -> datapart[IPptr -> IP_size] != guard_value)
		MSG1("Guard byte corrupted\n", proc->procname);
	if (IPptr -> owner != cnp)
		MSG1("IP header corrupted\n", proc->procname);
	*ptr = tptr -> datapart;
	*size = IPptr -> IP_size;
	*typep = IPptr -> type;
	
	IPptr -> next_IP = 0;
	IPptr -> prev_IP = 0;
	
	IPptr -> owner = proc;
	if (cnp ->IPcount == cnp -> max_IPcount)  
	   cnp->buffer_not_full.notify_all();
	
	--cnp -> IPcount; 

	if (proc -> trace) MSG1("%s Recv end\n", proc -> procname);
	proc -> owned_IPs++;
	value = 0;
retn:
	
	proc -> appl_ptr -> active = TRUE;
	return(value);
}