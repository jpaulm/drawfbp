//#include <setjmp.h>
#include <stdio.h>
//#include <malloc.h>
#include <string.h>
#include "thzcbs.h"
#include "cppfbp.h"

int thzdfpt(Process *pptr, int port_count, port_ent *peptr)
{
	Port *cpp;
	int i;

	if (pptr->trace) MSG1("%s DFPT Start\n", pptr->procname);
	for (i = 0; i < port_count; i++)
	{
		cpp = pptr -> in_ports;
		while (cpp != 0) {
			if (strcmp((peptr+i)-> port_name, cpp -> port_name) == 0) break;
			cpp = cpp -> succ;
		}
		if (cpp != 0){
			(peptr+i)-> cpptr = cpp;
			(peptr+i) -> elem_count = cpp -> elem_count;
			(peptr+i)-> ret_code = 0;
		}
		else {
			cpp = pptr -> out_ports;
			while (cpp != 0) {

				if (strcmp((peptr+i) -> port_name, cpp -> port_name) == 0) break;
				cpp = cpp -> succ;
			}
			if (cpp == 0)
				(peptr+i)-> ret_code = 2;
			else {
				(peptr+i)-> cpptr = cpp;
				(peptr+i)-> elem_count = cpp -> elem_count;
				(peptr+i)-> ret_code = 0;
			}
		}
	}
	if (pptr->trace) MSG1("%s DFPT End\n", pptr->procname);

	return(0);
}
