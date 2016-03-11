//#pragma comment(lib, "CppFBPCore")
#include "StdAfx.h"

/* THSORT is a simple Sort.

CALLING THSORT:   
input data stream -> IN sort(THSORT)  OUT -> output data stream ;

LIMITATIONS:
This component will just do a simple compare on the contents of the incoming IPs.
For simplicity, it will handle a maximum of 2000 IPs.
DEFAULTS:
None

*/

//#include <setjmp.h>
#include <stdio.h>
#include <string.h>
#include "compsvcs.h"  

THRCOMP ThSort (_anchor proc_anchor) {
	void *ptr;	
	void *dptrm, *dptr;
	char * cptrm, *cptr;
	int value, i;
	long size;
	char *type;
	port_ent port_tab[2];
	void** sort_tab;
	int count, lowest;
	bool first;
	bool finished = false;
	long m, n, k;

	sort_tab = new void*[2000];
	for (i = 0; i < 2000; i++)
		sort_tab[i] = 0;
	value = dfsdfpt(proc_anchor, 2, port_tab,"IN","OUT");
	i = 0;
	value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	while (value == 0) {	  
		sort_tab[i] = (unsigned char *) ptr;
		i++;
		value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	}
	count = i;
	value = dfstest(proc_anchor);	
	lowest = 0;
	for (;;) {
		finished = true;
		first = true;
		for (i = 0; i < count; i++) {
			if (sort_tab[i] != 0) {				
				finished = false;
				dptr = sort_tab[i];
				if (first) {
					dptrm = dptr;
					first = false;
					lowest = i;
					m = dfsgsize(proc_anchor, &dptrm);
				}				  
				else {
					n = dfsgsize(proc_anchor, &dptr);
					k = n;
					if (n > m)
						k = m;
					cptrm = (char *) dptrm;
					cptr = (char *) dptr;
					if (memcmp(dptrm, dptr, k) > 0) {
						dptrm = dptr;
						lowest = i;
					}
					else if (memcmp(dptrm, dptr, k) == 0 && m > n) {
						dptrm = dptr;
						lowest = i;
					}
				}
			}			
		}
		if (finished)
			break;
		ptr = sort_tab[lowest];
		sort_tab[lowest] = 0;
		value = dfssend(proc_anchor, &ptr, &port_tab[1], 0);
	}	 

	return(0);
}