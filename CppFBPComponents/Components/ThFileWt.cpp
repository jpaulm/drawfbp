#include "StdAfx.h"
#include "dllheader.h"

#include <string.h>
#include "compsvcs.h"
/* THFILEWT writes incoming entities to the file named on port OPT and
   optionally passes the entities on to an output stream.

   CALLING THFILEWT:
   "filename.ext" -> OPT write_ents(THFILEWT),
   input data stream -> IN write_ents [ OUT -> output data stream ];

   LIMITATIONS:
   Any special characters which cause problems for C's PUTC will not
   be written properly.

   DEFAULTS:
   None

*/

  //#include <setjmp.h>
  #include <stdio.h>
  

  THRCOMP ThFileWt (_anchor proc_anchor) {
 
 
  void *ptr;
  char *dptr;
  int value, i;
  long size;
  char *type;
  char fname[256];
  int ch;
  port_ent port_tab[3];
  FILE *fp;
  //char buffer[256];
  

  value = dfsdfpt(proc_anchor, 3, port_tab,"OPT","IN","OUT");

/*obtain filename parameter
*/
  value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
  memcpy(fname,ptr,size);
  value = dfsdrop(proc_anchor, &ptr);
  fname[size] = '\0';
  #ifdef WIN32
    errno_t err;
    if( (err  = fopen_s( &fp, fname, "w" )) !=0 ) {
#else
    if ((f = fopen(fname, "w")) == NULL) {
#endif
        fprintf(stderr, "Cannot open file %s!\n", fname);
		return(8);
    }

    value = dfsrecv(proc_anchor, &ptr, &port_tab[1], 0, &size, &type);
    while (value == 0)
      {
      dptr = (char *) ptr;
      for (i = 0; i < size; i++) {
         ch = (int) * (dptr + i);
         value = fputc(ch, fp);
        }
      value = fputc('\n',fp);

      value = dfssend(proc_anchor, &ptr, &port_tab[2], 0);
     if (value == 2)
	 value = dfsdrop(proc_anchor, &ptr);
      value = dfsrecv(proc_anchor, &ptr, &port_tab[1], 0, &size, &type);
      }
  fclose(fp);
  return(0);
}