/* 
external control blocks - used in network definitions
*/
 
#ifndef THXANCH
#define THXANCH
#include "thxanch.h"
#endif


#define THRCOMP int __stdcall
 

struct _IIP
   {
        char datapart[32767];
   };

typedef  _IIP IIP;


   struct _proc_ent {
	_proc_ent *succ;
     char proc_name[32];
     char comp_name[200];

     int (__stdcall *faddr) (_anchor anch);
	 
     void *proc_block;   // used as a temporary placeholder by thxbnet
	 void * label_ptr;   // points to a label for subnets	 
     bool trace;
     bool composite;
     bool must_run;  // new - doesn't seem to hurt, even when CopyFile not modified!
     };

typedef  _proc_ent proc_ent;

   class Cnxt;
   struct _cnxt_ent {
	   _cnxt_ent *succ;
     char upstream_name[32];       /* if 1st char is !,        */
     char upstream_port_name[32];     /* connxn points at IIP */
     int upstream_elem_no;
     char downstream_name[32];
     char downstream_port_name[32];
     int downstream_elem_no;
     union cnxt_union {IIP * IIPptr; Cnxt *connxn;} gen;
     int capacity;
	 bool dropOldest;     // new - doesn't seem to hurt, even when CopyFile not modified!
     };

typedef  _cnxt_ent cnxt_ent;

struct _label_ent {
	_label_ent *succ;
	 char label[32];
     char file[10];
	 _cnxt_ent *cnxt_ptr;
	 _proc_ent *proc_ptr;
     char ent_type;
	 };

typedef  _label_ent label_ent;

#define THXDEF
#define TRACE true
#define COMPOS true
#define DYNAM true
#define TIME_REQ true
