
//#include <setjmp.h>
#include <stdio.h>
//#include <malloc.h>
#include <string.h>
#include "thzcbs.h"
#include "cppfbp.h"
#define FALSE 0
#define TRUE 1
#define INPUT 0
#define OUTPUT 1


int thzrecv(Process *proc, void **ptr, port_ent *peptr, int elem_no,
	long *size, char **typep);
int thzdrop(Process *proc, void **ptr);
int thzsend(Process *proc, void **ptr, port_ent *peptr, int elem_no);

int thzsendc(Process *proc, void **ptr, char * port)
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
	Port * cpp = proc -> out_ports;
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
	
	int value = thzsend(proc, ptr, &pe, elem_no);

	//free (pe);
	return value;
}

int thzsend(Process *proc, void **ptr, port_ent *peptr, int elem_no)
{
	/*  This service takes the data part of an IP - if info is needed from the header,
	you have to back up over the length of the header */

	Process *sptr;
	IPh   *IPptr;
	IPh   *prevIP;
	//IPh   *optr;
	//IPh   *qptr;
	IP   *tptr;
	long size;
	char *type;
	Port *cpp;
	Cnxt *cnp;
	int value = 0;

	if (peptr -> ret_code == 2) {             
		if (proc -> trace) MSG1("%s Sending to unconnected port\n", proc -> procname);
		return(2);
	}

	cpp = (Port *)peptr -> cpptr;
	if (proc -> trace) MSG3("%s Send start %s[%d]\n", proc -> procname,
		cpp -> port_name, elem_no);
	
	cnp = (Cnxt *) cpp -> elem_list[elem_no].gen.connxn;
	if (cnp == 0) return(2);

	boost::unique_lock<boost::mutex> lock (cnp -> mtx);  
	
	//cnp ->lock.lock();
	

	if (cnp -> closed) {
		value = 2;
		goto retn;	
	}

	IPptr = (IPh   *) *ptr - 1;       /* back up size of header */
	tptr = (IP *)IPptr;
	if (tptr -> datapart[IPptr -> IP_size] != guard_value)
		MSG1("Guard byte corrupted\n", proc->procname);
	if (IPptr -> on_stack)
		MSG1("IP on stack\n", proc->procname);

	if (cpp -> direction != OUTPUT) {
		MSG2("%s %s SEND Wrong direction\n",
			proc -> procname, peptr -> port_name);
		value = 3;
		goto retn;
	}
	if (elem_no < 0 || elem_no >= cpp ->elem_count) {
		MSG2("%s %s SEND Element Number negative or too high\n",
			proc -> procname, peptr -> port_name);
		value = 3;
		goto retn;
	}
	if (cnp == 0) {
		value = 2;
		goto retn;	
	}

	if (cpp -> elem_list[elem_no].closed) {
		value = 1;
		goto retn;	
	}

	if (cnp -> nonterm_upstream_proc_count == 0) {
		value = 1;
		goto retn;
	}

	for (;;) {
		
		if (cnp -> IPcount < cnp -> max_IPcount) break;
		
		proc -> status = SUSPENDED_ON_SEND;
		proc -> waiting_cnxt = cnp;
		if (proc -> trace) MSG1 ("%s Send susp\n", proc -> procname);		

		cnp-> buffer_not_full.wait(lock);

		if (proc -> trace) MSG1 ("%s Send resume\n", proc -> procname);
		proc -> status = ACTIVE;
		proc -> waiting_cnxt = 0;

	}


	if (cpp -> elem_list[elem_no].closed) {
		value = 1;
		goto retn;
	}
	if (cnp -> nonterm_upstream_proc_count == 0) {
		value = 1;
		goto retn;
	}

	if (IPptr -> owner != proc)
		MSG1("IP header corrupted\n", proc->procname);
	IPptr -> owner = cnp;
	//optr = IPptr -> prev_IP;
	//qptr = IPptr -> next_IP;

	prevIP = cnp -> last_IPptr;
	if (prevIP != 0)
		prevIP -> next_IP = IPptr;
	else
		cnp -> first_IPptr = IPptr;

	cnp -> last_IPptr = IPptr;
	IPptr -> next_IP = 0;


	if ((cnp -> IPcount) == 0)   /* if it was empty, enable fedproc */
	{
		sptr = cnp -> fed_proc;

		if (sptr -> status == NOT_STARTED || sptr -> status == DORMANT) {

			if (sptr -> begin_port == 0) {
							
				if (sptr -> trace)
					printf("%s Initiated\n",sptr -> procname);
				 //sptr -> status = ACTIVE;
				 sptr -> activate();
			}
			else
				if (sptr -> begin_port -> elem_list[0].gen.connxn == cnp) {
					
					if (sptr -> trace)
						printf("%s Initiated\n",sptr -> procname);
					
					sptr ->activate();
					proc -> int_pe.cpptr = proc -> begin_port;
					proc -> value = thzrecv(proc, &proc -> int_ptr,  //????
						&proc -> int_pe, 0, &size, &type);
					proc -> value = thzdrop(proc, &proc -> int_ptr);
				}
		}
		else 	
			cnp->buffer_not_empty.notify_all();		
	}
	
	++cnp -> IPcount;
	if (proc -> trace) MSG1("%s Send end\n", proc -> procname);
	*ptr = 0;
	value = 0;
	proc -> owned_IPs--;
retn:
	//cnp->lock.unlock();
	//cnp -> lock.~unique_lock();	
	proc -> network -> active = TRUE;
	return(value);
}
