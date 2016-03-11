//#pragma comment(lib, "CppFBPCore")
#include "StdAfx.h"

/* THDROP drops all entities received on IN port.

   CALLING THDROP:
   input data stream -> IN destroy(THDROP);

   LIMITATIONS:
   None.

   DEFAULTS:
   None.
*/

  #include <stdio.h>
  #include <string.h>
  #include "compsvcs.h"



  THRCOMP  ThDrop(_anchor proc_anchor)
{
  void *ptr;
  int value;
  long size;
  char *type;
  port_ent port_tab[1];

  value = dfsdfpt(proc_anchor, 1, port_tab,"IN");
     value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
     while (value == 0) {
         value = dfsdrop(proc_anchor, &ptr);
         value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
         }
  return(0);
}

 