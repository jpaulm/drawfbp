//#include <setjmp.h>
#include <stdio.h>
//#include <malloc.h>
#include <string.h>
#include "thzcbs.h"
#include "cppfbp.h"

int thzpush(Process *pptr, void **ptr)
{
   IPh   *IPptr;
   //IPh   *optr;
  // IPh   *qptr;
   IP   *tptr;

   if (pptr -> trace) MSG1("%s Push start\n", pptr -> procname);
   IPptr = (IPh   *) *ptr - 1;       /* back up size of header */
   tptr = (IP *)IPptr;
   if (tptr -> datapart[IPptr -> IP_size] != guard_value)
      MSG1("Guard byte corrupted\n", pptr->procname);
   if (IPptr -> owner != pptr)
     MSG1("IP header corrupted\n", pptr->procname);
   if (IPptr -> on_stack)
     MSG1("IP on stack\n", pptr->procname);
   //optr = IPptr -> prev_IP;
   //qptr = IPptr -> next_IP;
  // if (optr != 0)
  //     optr -> next_IP = qptr;
  //   else
   //    pptr -> first_owned_IP = qptr;
  // if (qptr != 0)
  //   qptr -> prev_IP = optr;
   IPptr -> next_IP = pptr -> stack;
   pptr -> stack = IPptr;
   IPptr -> on_stack = TRUE;
   if (pptr -> trace) MSG1("%s Push end\n",pptr -> procname);
   *ptr = 0;
   pptr -> owned_IPs--;
   return(0);
}
