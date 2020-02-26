//#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include "thzcbs.h"
#include "cppfbp.h"



int thzcrep(Process *pptr, void **ptr, long IPsize, char *type)
{

	/*  This service returns the data part of an IP - if info is needed from the header,
	you have to back up over the length of the header */
	
   long totsize;
   IPh   *IPptr;
  
   IP   *tptr;
   
   if (pptr->trace) MSG1("%s Crep start\n", pptr -> procname);
   if (IPsize > 64000 || IPsize < 0) {
      MSG2("Invalid IP size in CREP: %ld - %s\n", IPsize, pptr -> procname);
      return(8);
      }
   int i = sizeof(IPh);
   totsize = IPsize + i + 1;          // room for guard
    
   boost::lock_guard<boost::mutex> lock(pptr -> network -> heapmtx);
   IPptr = (IPh *) malloc(totsize);
   //lock.~lock_guard();

   IPptr -> IP_size = IPsize;
   IPptr -> owner = pptr;
   IPptr -> type = type;
   
   IPptr -> next_IP = 0;
   IPptr -> prev_IP = 0;
   IPptr -> on_stack = FALSE;
   //pptr -> first_owned_IP = IPptr;
   tptr = (IP *) IPptr;
   *ptr = tptr -> datapart;
   tptr -> datapart[IPsize] = guard_value;
   pptr -> owned_IPs++;
   if (pptr->trace) MSG1("%s Crep end\n",pptr -> procname);
   return(0);
}
