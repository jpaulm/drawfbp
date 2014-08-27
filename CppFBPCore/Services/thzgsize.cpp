#include "cppfbp.h"

   long thzgsize(Process *pptr, void **ptr)
{
	/*  This service takes the data part of an IP - if info is needed from the header,
	you have to back up over the length of the header */

   //process *sptr;
   IPh   *IPptr;
   
   long size;
   
   IPptr = (IPh   *) *ptr - 1;       /* back up size of header */
   
   size = IPptr->IP_size;
   return(size);
}
