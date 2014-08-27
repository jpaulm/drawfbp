#include "StdAfx.h"
#include "dllheader.h"
  
  #include <string.h>
  #include <stdio.h>
  #include <stdlib.h>

  #include "compsvcs.h"


  THRCOMP ThGenIps(_anchor proc_anchor)
{
  void *ptr;
  char *p;
  char string[256];
  int value, i;
  long size, count;
  char *type;
  port_ent port_tab[3];
  //char buffer[256];

  value = dfsdfpt(proc_anchor, 3, port_tab,"COUNT","OUT","PREFIX");

  value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
  if (value > 0) {
     printf("THGENIPS: No number specified\n");
  //   dfsputs(proc_anchor, buffer);
     return(8);
     }
  memcpy(string,ptr,size);
  string[size] = '\0';
  count = atol(string);

  value = dfsdrop(proc_anchor, &ptr);

  strcpy(string, "Testing ");
  value = dfsrecv(proc_anchor, &ptr, &port_tab[2], 0, &size, &type);
  if (value == 0) {
     memcpy(string,ptr,size);
     string[size] = '\0';
     value = dfsdrop(proc_anchor, &ptr);
  }
 // dfstest(proc_anchor);
  for (i = 0; i < count; i++) {
      value = dfscrep(proc_anchor, &ptr, 32,"G");
      strcpy((char *) ptr, string);
      p = strchr((char *) ptr, '\0');
      _itoa(i, p, 10);
      value = dfssend(proc_anchor, &ptr, &port_tab[1], 0);
      }
  return(0);
} 