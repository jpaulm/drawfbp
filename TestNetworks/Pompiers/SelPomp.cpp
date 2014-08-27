#include "StdAfx.h"
#include "dllheader.h"
#include "compsvcs.h"
#include <string.h>
#include <stdlib.h>

   THRCOMP SelPomp(_anchor anch) {
 
 
  void *ptr;
  int value;
  long size;
  char *type;
  port_ent port_tab[3];
  char sal[10] = " ";
  long salp;  /* salaire d'un individuel */
  long sallim; /* salaire pour comparaison */

 
  value = dfsdfpt(anch, 3, port_tab, "SAL", "IN","OUT");

/* process parameter */
  value = dfsrecv(anch, &ptr, &port_tab[0], 0, &size, &type);
  strncpy(sal, (char *) ptr, 8); 
  sallim = atol(sal);
  value = dfsdrop(anch, &ptr);

/* process input entities */
  value = dfsrecv(anch, &ptr, &port_tab[1], 0, &size, &type);
  while (value == 0) {
      strncpy(sal, (char *) ptr, 8); 
      salp = atol(sal);  
      if  (salp >= sallim)
            value = dfssend(anch, &ptr, &port_tab[2], 0);
        else
	    value = dfsdrop(anch, &ptr);
      value = dfsrecv(anch, &ptr, &port_tab[1], 0, &size, &type);
      }
    return(0);
}
