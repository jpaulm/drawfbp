#pragma optimize( "", off )
// #include <tchar.h>
/** 
This is the C++FBP main-line, working from a set of linked, fixed-layout, structures encoding the relationships 
between processes, connections, ports and IIPs - they will be referred to as fixed-format network definitions (FFNDs).

*/

#include "stdafx.h"
#include <dos.h>  
#if defined _WIN32
#include <windows.h>
#endif

#include <stdarg.h>
#include <stdio.h>
//#include <io.h>
#include <ctype.h>
// #include <setjmp.h>
//#include <stdlib.h>
#include <malloc.h>
#include <string.h>
#include <time.h>
//#include <stdlib.h>

#include "thxiip.h"
#include "cppfbp.h"
#include "thxscan.h"
#include <tchar.h>
#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>
#include <boost/thread/condition.hpp>
#include <boost/thread/thread.hpp>

#include <boost/ptr_container/ptr_list.hpp>

#define FILE struct _iobuf

void thxfcbs(Appl *appl_ptr);

void disp_IP(IPh   * this_IP);
/* first param of thxbnet is subnet address; last is whole net address */
int thxbnet(label_ent * label_ptr, Process *mother,
	Appl *appl_ptr, label_ent *label_tab);
int findmod(char * p);

int thxscan(FILE *fp, label_ent *label, char file_name[10]);

int thzsend(Process *pptr, void **ptr, port_ent *peptr, int elem_no);
int thzcrep(Process *pptr, void **ptr, long IPsize, char *type);
int thzdrop(Process *pptr, void **ptr);
void thziclos(Process * proc, cp * cpp, int i);

//void thzputs(Process * p, char * buffer); 

static void run(Process * proc);

int run_test(Process * proc);

bool deadlock_test_sw = TRUE;
Appl *appl_ptr;

void   CppFBP( label_ent * label_blk, bool dynam, FILE * fp, bool timereq)    {

	int i;
	bool attsw;

	//Appl *appl_ptr;
	Process *this_proc;
	cnxt_ent *cnxt_tab;
	proc_ent *proc_tab;
	label_ent *label_tab;
	label_ent *label_curr;
	label_ent *label_new;
	//int label_count = 0;
	//int proc_count = 0;
	//int cnxt_count = 0;
	char file_name[10];

	cp *cpp;
	time_t st_time, end_time;
	double secs;
	//freopen("my_log.txt", "w", stdout);
	st_time = time(NULL);
	//_CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_CHECK_ALWAYS_DF | _CRTDBG_LEAK_CHECK_DF);

	label_tab = (label_ent *)malloc(sizeof(label_ent));
	label_curr = label_tab;
	label_ent * label_this = label_blk;
	if (!dynam) {
		for (;;) {
			strcpy(label_curr->label,
				label_this->label);
			strcpy(label_curr->file,
				label_this->file);
			label_curr->cnxt_ptr = 
				label_this->cnxt_ptr;
			label_curr->proc_ptr = 
				label_this->proc_ptr;
			label_curr->ent_type = 
				label_this->ent_type;
			label_this = label_this->succ;
			if (label_this == 0) break;
			label_new = (label_ent *)malloc(sizeof(label_ent));  // handle subnets
			label_curr->succ = label_new;
			label_curr = label_new;
		}

		cnxt_tab = label_tab -> cnxt_ptr;
		proc_tab = label_tab -> proc_ptr;
	}
	else {
		label_tab->succ = 0;
		file_name[0] = '\0'; 

		//if (fp == 0) {
		//	printf("File does not exist: %s\n", fp);
		//	exit(4);
		//}
		if (thxscan(fp, label_tab, file_name) != 0) {

			printf("Scan error\n");
			exit(4);
		}
	}
	appl_ptr = new Appl;
	strcpy(appl_ptr -> name,"APPL");
	//appl_ptr -> first_ready_proc = 0;
	appl_ptr -> first_child_proc = 0;
	appl_ptr -> first_child_comp = 0;
	appl_ptr -> first_cnxt = 0;
	appl_ptr -> dynam = dynam;
	appl_ptr -> active = FALSE;
	appl_ptr -> possibleDeadlock = FALSE;
	appl_ptr -> deadlock = FALSE;
	
	thxbnet(label_tab, 0, appl_ptr, label_tab);  // why two params the same?!

	int thread_count = 0;

	this_proc = (Process*) appl_ptr -> first_child_proc;
	while (this_proc != 0)
	{
		thread_count ++;
		this_proc = this_proc -> next_proc;
	}

	/* Look for processes with no input ports */

	appl_ptr -> latch =  new Cdl(thread_count);

	Cnxt * cnxt_ptr = (Cnxt *) appl_ptr -> first_cnxt;
	while (cnxt_ptr != 0) {	 
		cnxt_ptr -> closed = FALSE;
		cnxt_ptr = cnxt_ptr -> succ;
	}

	this_proc = (Process*) appl_ptr -> first_child_proc;
	while (this_proc != 0)
	{
		this_proc -> appl_ptr = appl_ptr;
		attsw = TRUE;
		cpp = this_proc -> in_cps;
		while (cpp != 0) {	 
			for (i = 0; i < cpp -> elem_count; i++) {
				if (cpp -> elem_list[i].gen.connxn != 0 &&
					! cpp -> elem_list[i].is_IIP)
					attsw = FALSE;
			}
			cpp = cpp -> succ;
		}
		if (attsw)
		{
			// add to ready chain
			//this_proc -> status = INITIATED;

			if (this_proc -> trace)
				printf("%s Initiated\n",this_proc -> procname);

			this_proc -> activate();
		}
		//thread_count ++;
		this_proc = this_proc -> next_proc;
	}

	appl_ptr -> active = TRUE;

	//boost::chrono::milliseconds msec(500);
	bool deadlock_test(Appl * ptr);

	for (;;) {
		//Cdl * l = appl_ptr -> latch;
		int res = appl_ptr -> latch -> wait(appl_ptr) ;  // 1 means count hit zero; 0 means timeout interval expired
        if (res == 1)
			break;  	
		if (res == 0 && deadlock_test_sw && deadlock_test(appl_ptr))
			break;
	}

	delete appl_ptr -> latch;


	//if (dynam) {                  -- check this out!
	//	free(cnxt_tab);
	//	free(proc_tab);		
	//}


	//free (appl_ptr -> latch);  
	thxfcbs(appl_ptr);
	free(label_tab);

	end_time = time(NULL);
	secs = difftime(end_time, st_time);
	if (timereq){
		printf("Elapsed time in seconds: %5.3f\n",secs);
		
	}
	//_CrtDumpMemoryLeaks();
	//char c; std::cin>>c;   // to see console
	system("pause");  // to see console
	exit(0);
}


  Cdl::Cdl(int ct)  {
	  count = ct;
  }


	/// Blocks until the latch has counted down to zero or hit no. of msecs, whichever comes first .
	int Cdl::wait(void * appl_ptr)
	{
		boost::unique_lock<boost::mutex> lock( mutex );

		boost::chrono::milliseconds msec(500);
		int res; 
		//while ( count > 0 )
		//{   			
			res = cond_var.wait_for(lock, msec);
			if (res == boost::cv_status::timeout)  			
				return 0;     //time expired			
				 	      
		    else   if (count == 0)
				return 1;    //countdown hit zero
			
			else 
				return -1;   // countdown did not hit zero, and interval not up!
		//}
		//return 1;  //countdown hit zero
	}
		

	/// Decrement the count and notify anyone waiting if we reach zero.
	/// @note count must be greater than 0 or undefined behavior
	void Cdl::count_down()
	{
		//boost::lock_guard<boost::mutex> lock(mutex);
		boost::unique_lock<boost::mutex> lock(mutex );
		assert( count );
		if ( --count == 0 )
		{
			cond_var.notify_all();
		}
	}



void inline Process::run(Process * proc) {

	//proc -> status = ACTIVE;
	for( ; ; ) {
		if (2 == run_test(proc)  && !proc -> must_run) 
			break;
		cp* cpp = proc -> in_cps;
	    while (cpp != 0) {	       
		   for (int i = 0; i < cpp -> elem_count; i++) {
			  if (cpp -> elem_list[i].gen.connxn == 0)
			  	  continue;
			  if (! cpp -> elem_list[i].is_IIP)
				  cpp -> elem_list[i].closed = FALSE;	
		    }
		    cpp = cpp -> succ;
	    }
		//
		// execute component code!
		proc -> value =
			proc -> faddr (proc -> proc_anchor);	
		// component code returns

		if (proc -> value > 0)
			printf ("Process %s returned with value %d\n",
			proc -> procname, proc -> value); 
		if (proc -> owned_IPs > 0)
			printf("%s Deactivated with %d IPs not disposed of\n",
			proc -> procname, proc -> owned_IPs);

		if (proc -> trace)
			printf("%s Deactivated with retcode %d\n",
			proc -> procname, proc -> value);

		if (proc -> value > 4) {
			//proc -> terminating = TRUE;
			break;
		}

		int res;
		/* res =  0 if data in cnxt; 1 if upstream not closed;
		2 if upstream closed */
		
		res = run_test(proc);

		if (res == 2) break;
		if (res == 0) continue;

		dormwait(proc);
	}
	
	

	/*
	Process * lpProc;
	// This should only be done at termination time!
	if (lpProc -> end_cp != 0) {
	lpProc -> value = thzcrep(lpProc,
	&lpProc -> int_ptr, 0, "\0");
	lpProc -> int_pe.cpptr = lpProc -> end_cp;
	lpProc -> value = thzsend(lpProc,
	&lpProc -> int_ptr, &lpProc -> int_pe, 0);
	if (lpProc -> value > 0)
	lpProc -> value = thzdrop(lpProc,
	&lpProc -> int_ptr);
	//break;
	}

	*/
	//close_output_cps(proc);
	

	cp * cpp = proc -> out_cps;
	while (cpp != 0)
	{
		for (int i = 0; i < cpp -> elem_count; i++) {
			if (cpp -> elem_list[i].gen.connxn == 0)
				continue;
			/*value = */ thziclos(proc, cpp, i);
		}
		cpp = cpp -> succ;
	}

	//close_input_cps(proc);
	cpp = proc -> in_cps;
	while (cpp != 0)
	{
		for (int i = 0; i < cpp -> elem_count; i++) {
			if (cpp -> elem_list[i].gen.connxn == 0)
				continue;
			if (! cpp -> elem_list[i].is_IIP)
					thziclos(proc, cpp, i);
		}
		cpp = cpp -> succ;
	}
	//printf("Terminated %s\n", proc -> procname);
	proc -> status = TERMINATED;
	if (proc -> trace)
		printf("%s Terminated with retcode %d\n",
		proc -> procname, proc -> value);
	//proc -> thread.join();
	proc -> appl_ptr -> latch -> count_down();
} 

void inline Process::dormwait(Process * proc) {
	boost::unique_lock<boost::mutex> lock (proc -> mtx );
	proc -> status = DORMANT;
	proc -> canGo.wait(lock);
	proc -> status = ACTIVE;
	
}

int run_test(Process * proc) {
        int res  = 2;
		Cnxt * cnp;
		cp * cpp = proc -> in_cps;
		while (cpp != 0)
		{
			for (int i = 0; i < cpp -> elem_count; i++) {
				cnp = (Cnxt *)cpp -> elem_list[i].gen.connxn;

				if (cnp == 0)
					continue;
				if (cpp -> elem_list[i].is_IIP) {
					cpp -> elem_list[i].closed = FALSE;
					continue;
				}
				boost::unique_lock<boost::mutex>lock  (cnp ->mtx );
				if (cnp -> IPcount > 0){
					res  = 0;  // data in upstream connection
					lock.unlock();
                    lock.~unique_lock();					
					break;
				}

				if (cnp -> nonterm_upstream_proc_count == 0 ) 					
					cnp -> closed = TRUE;                   
				else				
					res  = 1;    // no data in connection, but upstream processes not all closed!
                lock.unlock();
                lock.~unique_lock();
			}
			if (res == 0)
				break;
			cpp = cpp -> succ;
		}
		return res;
}

bool deadlock_test(Appl * appl_ptr) {

	//testTimeouts(freq);
	if (appl_ptr -> active) {
		appl_ptr -> active = FALSE; // reset flag every 1/2 sec
	} else if (!appl_ptr -> possibleDeadlock) {
		appl_ptr -> possibleDeadlock = TRUE;
	} else {
		appl_ptr -> deadlock = TRUE; // well, maybe
		// so test state of components

		class ABC
		{
		public:
			char c [100];
		};
		boost::ptr_list<ABC> msgs;
		boost::ptr_list<ABC>::iterator iterMsg;

		ABC * s1 = new ABC;
		strcpy (s1 ->c, "Network has deadlocked\n");
		msgs.push_front(s1);  		// add in case msgs are printed


		bool deadlock = TRUE;
		Process * this_proc = (Process *) appl_ptr -> first_child_proc;
		//Cnxt * cnp;
		bool terminated = TRUE;
		while (this_proc != 0)
		{
			char  status [30];
			switch (this_proc -> status) {
			case NOT_STARTED:				
				strcpy(status, "Not Started\n");
				terminated = FALSE;
				break;
			case ACTIVE:				
				strcpy(status, "Active\n");
				terminated = FALSE;
				deadlock = FALSE;
				break;
			case DORMANT:				
				strcpy(status, "Inactive\n");
				terminated = FALSE;
				break;
			case SUSPENDED_ON_SEND:	
				terminated = FALSE;
				strcpy(status, "Suspended on Send\n");
				//cnp = (Cnxt *) this_proc -> waiting_cnxt;
				//if (cnp -> IPcount < cnp ->max_IPcount) {
				//	cnp->buffer_not_full.notify_all();
				//	deadlock = FALSE;
				//}
				break;
			case SUSPENDED_ON_RECV:		
				terminated = FALSE;
				strcpy(status, "Suspended on Receive\n");
				break;
				//case INITIATED:
				//	printf (" Process %s Initiated\n", this_proc -> procname);
				//	break;
				//case READY_TO_RESUME:
				//	printf (" Process %s Ready to Resume\n", this_proc -> procname);
				//	break;
			case TERMINATED:				
				strcpy(status, "Terminated\n");
				break;
			}
			char msg[100];
			strcpy (msg, "Process ");
			strcat (msg, this_proc -> procname);
			strcat (msg, " ");
			strcat (msg, status);
			ABC * s1 = new ABC;
			strcpy(s1 -> c, msg);
			msgs.push_front(s1); 
			this_proc = this_proc -> next_proc;
		}


		//if (listCompStatus(msgs)) { // if TRUE, it is a deadlock
		//          interruptAll();
		if (deadlock && !terminated) {

			for (iterMsg = msgs.begin(); iterMsg != msgs.end(); iterMsg++)
			{
				printf(iterMsg -> c );
			}

			//char c; std::cin>>c;   // to see console

			//		kill everything!
			Process * this_proc = (Process *) appl_ptr -> first_child_proc;
			boost::thread::id nat =  boost::thread::id::id();   // Not a Thread
			while (this_proc != 0)
			{
				boost::thread::id id = this_proc -> thread.get_id();
				if (nat != id)
				       this_proc -> thread.interrupt();
				this_proc = this_proc -> next_proc;
			}
			return TRUE;
		}
		// one or more components haven't started or
		// are in a long wait
		appl_ptr -> deadlock = FALSE;
		appl_ptr -> possibleDeadlock = FALSE;
		
	}
	return FALSE;
}


void disp_IP(IPh   * this_IP) {   // not currently used...
	char * dptr;
	IPh   * ip;
	int i;
	unsigned j;
	long size;
	char *type;

	dptr = (char *) ((IP *) this_IP) -> datapart;
	ip = this_IP;
	size = ip -> IP_size;
	type = ip -> type;
	printf ("IP not disposed of - Length: %ld, Type: %s, Data:",
		size,  type);
	for (i = 0; i < size; i++)  {
		j = (int) *(dptr + i);
		printf("%c",j);
	}
	printf("%c\n", (int) *(dptr + size));
}


