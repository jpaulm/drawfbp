//#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>
#include "thzcbs.h"
#include "cppfbp.h"

int thzdrop(Process *proc, void **ptr)
{
   IPh   *IPptr;
   IP   *tptr;
   
   if (proc->trace) MSG1("%s Drop start\n", proc -> procname);
   IPptr = (IPh   *) *ptr - 1;       /* back up size of header */
   tptr = (IP   *) IPptr;
   if (tptr -> datapart[IPptr -> IP_size] != guard_value)
      MSG1("Guard byte corrupted - %s\n", proc->procname);
   if (IPptr -> owner != proc)
     MSG1("IP header corrupted - %s\n", proc->procname);
   if (IPptr -> on_stack)
     MSG1("IP on stack - %s\n", proc->procname);
   
   boost::lock_guard<boost::mutex> lock(proc -> network -> heapmtx);
   free(IPptr);
   //lock.~lock_guard();
    
   proc -> owned_IPs--;
   if (proc->trace) MSG1("%s Drop end\n",proc -> procname);
   *ptr = 0;
   return(0);
}
