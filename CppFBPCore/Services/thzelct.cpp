#include "thzcbs.h"
#include "cppfbp.h"

int thzelctc(Process *proc, char * port)
{
	int elem_no = 0;
	char port_name[100];
	char * p = strchr(port, '[');
	char * q;
	if (p == 0) 
		strcpy_s (port_name, port);
	else {
		q = strchr(p, ']');
		auto n = q - p - 1;
		char no[10];		
		strncpy_s(no, p + 1, n);
		elem_no = atoi(no);
		char * r = port;
		strncpy_s (port_name, port, p - r);
		port_name[p - r] = '\0';
	}
	Port * cpp = proc -> in_ports;
	while (cpp != 0)
	{

		if (0 == strcmp(cpp -> port_name, port_name)){
			break;
		}

		cpp = cpp -> succ;
	}
	if (cpp == 0) {
		cpp = proc -> out_ports;
		while (cpp != 0)
		{

			if (0 == strcmp(cpp -> port_name, port_name)){
				break;
			}

			cpp = cpp -> succ;
		}
		if (cpp == 0) {
			MSG2 ("%s Port %s not found\n", proc -> procname, port_name);	
			return -1;
		}
	}
	return cpp -> elem_count;
}

int thzelct(Process *proc, port_ent *peptr)
{
	/*  This service returns the no. of elements in a cpp */

	Port * cpp = (Port *) peptr ->cpptr;
	return cpp->elem_count;

}
