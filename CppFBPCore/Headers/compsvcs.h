
//#define THRCOMP  extern "C"  __declspec(dllexport) int __stdcall  
#define THRCOMP int
#define THRCOMPS  __declspec(dllexport) int __stdcall  // use with components in subnet
 

#ifndef THXANCH
#define THXANCH
   #include "thxanch.h"
#endif

   int dfscrep(_anchor proc_anchor, void **ptr, long size, char *type);
   int dfssend(_anchor proc_anchor, void **ptr, port_ent *peptr,
       int elem_no);
   int dfsrecv(_anchor proc_anchor, void **ptr, port_ent *peptr,
       int elem_no, long *size, char **type);
   int dfsdrop(_anchor proc_anchor, void **ptr);
   int dfsdfpt(_anchor proc_anchor, int port_count, port_ent *peptr, ...);
   int dfspush(_anchor proc_anchor, void **ptr);
   int dfspop(_anchor proc_anchor, void **ptr, long *size, char **type);
   int dfsclos(_anchor proc_anchor, port_ent *peptr,
       int elem_no);
   int dfstest(_anchor proc_anchor);
   long dfsgsize(_anchor proc_anchor, void **ptr);
   int dfselct(_anchor proc_anchor, port_ent *peptr);

   int dfssendc(_anchor proc_anchor, void **ptr, char * port);
   int dfsrecvc(_anchor proc_anchor, void **ptr, char * port, long *size, char **type);

   int dfsclosc(_anchor proc_anchor, char * port);
   int dfselctc(_anchor proc_anchor, char * port);
  
 