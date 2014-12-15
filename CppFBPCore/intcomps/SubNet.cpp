
#include "cppfbp.h"
#include "thzcbs.h"

#include <stdio.h>

 void SubNet::go(label_ent * label_blk, bool dynam, FILE * fp, bool timereq, _anchor proc_anchor) {
	Process * mother = (Process *) proc_anchor.reserved;
	_anchor anch; 
	anch.reserved = mother;
 	Network::go(label_blk, dynam, fp, timereq, anch);
 }