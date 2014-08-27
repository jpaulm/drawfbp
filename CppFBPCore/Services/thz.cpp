 #include <stdarg.h>
   //#include <setjmp.h>
   #include <stdio.h>
   #include <string.h>
   #include "thxiip.h"
   #include "cppfbp.h"


   int thzsend(Process *proc, void **ptr, port_ent *peptr, int elem_no);
   int thzrecv(Process *proc, void **ptr, port_ent *peptr, int elem_no,
       long *size, char **typep);
   int thzcrep(Process *proc, void **ptr, long IPsize, char *type);
   int thzdrop(Process *proc, void **ptr);
   int thzdfpt(Process *proc, int port_count, port_ent *peptr);
   int thzpush(Process *proc, void **ptr);
   int thzpop(Process *proc, void **ptr, long *size, char **typep);
   int thzclos(Process *proc, port_ent *peptr, int elem_no);
   long thzgsize(Process *proc, void **ptr);
   int thzelct(Process * proc, port_ent *peptr);

   int thzsendc(Process *proc, void **ptr, char * port);
   int thzrecvc(Process *proc, void **ptr, char * port,
       long *size, char **typep);
   int thzclosc(Process *proc, char * port);
   int thzelctc(Process * proc, char * port);


   int thz(int code, Process *proc, ...)
   {
   va_list ap;
   int value = 0;
	   int port_count, elem_no;
   long IPsize;
   long *size;
   char *type;
   char **typep;
   port_ent *peptr;
   void **ptr;
   char * port;
  
   va_start (ap, proc);
   if (code == 0)
	   return 0;
   switch (code) {
   case 1: ptr = va_arg(ap,void **);
           peptr = va_arg(ap,port_ent *);
	   elem_no = va_arg(ap,int);
	   value = thzsend(proc, ptr, peptr, elem_no); break;
   case 2: ptr = va_arg(ap,void **);
	   peptr = va_arg(ap,port_ent *);
	   elem_no = va_arg(ap,int);
           size = va_arg(ap,long *);
	   typep = va_arg(ap, char **);
	   value = thzrecv(proc, ptr, peptr, elem_no,
	     size, typep); break;
   case 3: ptr = va_arg(ap,void **);
           IPsize = va_arg(ap,long);
	   type = va_arg(ap,char *);
	   value = thzcrep(proc, ptr, IPsize, type); break;
   case 4: ptr = va_arg(ap,void **);
	   value = thzdrop(proc, ptr); break;
   case 5: port_count = va_arg(ap,int);
           peptr = va_arg(ap,port_ent *);
	   value = thzdfpt(proc, port_count, peptr); break;
   case 6: ptr = va_arg(ap,void **);
	   value = thzpush(proc, ptr); break;
   case 7: ptr = va_arg(ap,void **);
           size = va_arg(ap,long *);
	   typep = va_arg(ap, char **);
	   value = thzpop(proc, ptr, size, typep); break;
   case 8: peptr = va_arg(ap,port_ent *);
	   elem_no = va_arg(ap,int);
	   value = thzclos(proc, peptr, elem_no); break;

   case 9: ptr = va_arg(ap,void **);          
	  IPsize = thzgsize(proc, ptr); 
	   return IPsize; break;

case 10: //ptr = va_arg(ap,void **); 
	peptr = va_arg(ap,port_ent *);
	   value = thzelct(proc, peptr); 
	   break;

   case 11: ptr = va_arg(ap,void **);
           port = va_arg(ap,char *);	 
	 value = thzsendc(proc, ptr, port); 
	   break;
   case 12: ptr = va_arg(ap,void **);
	      port = va_arg(ap,char *);
	      size = va_arg(ap,long *);
	   typep = va_arg(ap, char **);
	   value = thzrecvc(proc, ptr, port,
	     size, typep); break;
	case 13:    port = va_arg(ap,char *);	  
 	  value = thzclosc(proc, port); 
	   break;
	case 14: //ptr = va_arg(ap,void **); 
	port = va_arg(ap,char *);	
	  value = thzelctc(proc, port); 
	   break;
   }
   va_end(ap);
   if (code < 10 && proc -> trace && value > 0)
       MSG2("%s: Service retcode %d\n",
	  proc -> procname, value);
     

   return(value);

}
   