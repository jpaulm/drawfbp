#include "stdafx.h"
#include <dos.h>  
#if defined _WIN32
#include <windows.h>
#endif

#include <stdarg.h>

#include "thxdef.h"
#include "thzcbs.h"


void   CppSub(label_ent * label_blk, bool dynam, FILE * fp, bool timereq, _anchor proc_anchor)    {
	_anchor anch; 

	Network * subnet = new Network;
	Process * mother = (Process *) proc_anchor.reserved;	
	anch.reserved = mother;
	subnet -> go(label_blk, dynam,  fp,  timereq, proc_anchor);
	delete subnet;
	
}