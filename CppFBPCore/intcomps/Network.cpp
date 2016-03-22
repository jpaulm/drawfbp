#pragma optimize( "", off )
#include "stdafx.h"
#include <dos.h>  
#if defined _WIN32
#include <windows.h>
#endif

#include <stdarg.h>
#include <stdio.h>
#include <conio.h>
#include "thzcbs.h"
#include "cppfbp.h"

#include <boost/thread/condition.hpp>
#include <boost/thread/thread.hpp>

#include <boost/ptr_container/ptr_list.hpp>

#define FILE struct _iobuf


void disp_IP(IPh   * this_IP);
/* first param of thxbnet is subnet address; last is whole net address */
int thxbnet(label_ent * label_ptr, Process *mother,
	Network * network, label_ent *label_tab);
//int findmod(char * p);

int thxscan(FILE *fp, label_ent *label, char file_name[10]);

int thzsend(Process *pptr, void **ptr, port_ent *peptr, int elem_no);
int thzcrep(Process *pptr, void **ptr, long IPsize, char *type);
int thzdrop(Process *pptr, void **ptr);
void thziclos(Process * proc, Port * cpp, int i);

//void thzputs(Process * p, char * buffer); 

//static void run(Process * proc);

bool deadlock_test_sw = TRUE;


void Network::go(label_ent * label_blk, bool dynam, FILE * fp, bool timereq, _anchor proc_anchor) {	
	Process *this_proc;
	cnxt_ent *cnxt_tab;
	proc_ent *proc_tab;
	label_ent *label_tab;
	label_ent *label_curr;
	label_ent *label_new;
	
	char file_name[10];

	Port *cpp;
	time_t st_time, end_time;
	double secs;
	st_time = time(NULL);

	Process * mother = (Process *) proc_anchor.reserved;

	_CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_CHECK_ALWAYS_DF | _CRTDBG_LEAK_CHECK_DF);

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
	//network = new Appl;
	//network = this;
	if (proc_anchor.reserved == NULL)
		strcpy(name,"APPL");
	else
		strcpy(name,"SUBNET");
	//first_ready_proc = 0;
	first_child_proc = 0;
	first_child_comp = 0;
	first_cnxt = 0;
	dynam = dynam;
	active = FALSE;
	possibleDeadlock = FALSE;
	deadlock = FALSE;

	thxbnet(label_tab, mother, this, label_tab);  // first param is network/subnet, 2nd is "mother" process", 4th is network as a whole

	int thread_count = 0;

	this_proc = (Process*) first_child_proc;
	while (this_proc != 0)
	{
		thread_count ++;
		this_proc = this_proc -> next_proc;
	}

	/* Look for processes with no input ports */

	latch =  new Cdl(thread_count);

	Cnxt * cnxt_ptr = (Cnxt *) first_cnxt;
	while (cnxt_ptr != 0) {	 
		cnxt_ptr -> closed = FALSE;
		cnxt_ptr = cnxt_ptr -> succ;
	}

	this_proc = (Process*) first_child_proc;
	while (this_proc != 0)
	{
		this_proc -> network = this;
		this_proc-> self_starting = TRUE;
		cpp = this_proc -> in_ports;
		while (cpp != 0) {	 
			for (int i = 0; i < cpp -> elem_count; i++) {
				if (cpp -> elem_list[i].gen.connxn != 0 &&
					! cpp -> elem_list[i].is_IIP)
					this_proc->self_starting = FALSE;
			}
			cpp = cpp -> succ;
		}
		if (this_proc -> self_starting)
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

	active = TRUE;

	waitForAll();
	delete latch;


	//if (dynam) {                  -- check this out!
	//	free(cnxt_tab);
	//	free(proc_tab);		
	//}


	//free (latch);  
	thxfcbs();
	//free(label_tab);

	end_time = time(NULL);
	secs = difftime(end_time, st_time);
	if (timereq){
		printf("Elapsed time in seconds: %5.3f\n",secs);
	}

	if (strcmp(name, "SUBNET") != 0) {
	  //_CrtDumpMemoryLeaks();
	  //char c; 
	  printf("Press enter to terminate\n");
	  std::cin.get();  // to see console
	  //system("pause");  // to see console
	  exit(0);
	}
}


Cdl::Cdl(int ct)  {
	count = ct;
}

void Network::waitForAll() {
	//boost::chrono::milliseconds msec(500);
	

	for (;;) {
		//Cdl * l = latch;
		int res = latch -> wait() ;  // 1 means count hit zero; 0 means timeout interval expired
		if (res == 1)
			break;  	
		if (res == 0 && deadlock_test_sw && deadlock_test())
			break;
	}
}

/// Blocks until the latch has counted down to zero or hit no. of msecs, whichever comes first .
int Cdl::wait()
{
	boost::unique_lock<boost::mutex> lock( mutex );

	boost::chrono::milliseconds msec(500);
	boost::cv_status res; 
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

void Process::activate() {
	    HINSTANCE hDLL;
		char procname_in_dll[255];
		char dllname[255];
		char szBuf[80];
		//typedef int (__stdcall* LPFNDLLFUNC)(_anchor);
        //LPFNDLLFUNC lpfnDllFunc; 		

		if (status == NOT_STARTED) {
			status = ACTIVE;
			//printf("Activate %s", compname);
			if (faddr == NULL) {
				//if (!composite) {
				//	strcpy(szBuf, "No link but not marked composite: ");
				//			strcat(szBuf, compname);
				//			MessageBox(NULL, szBuf, "Subnet specification", MB_ICONHAND);
				//}

				/* Load the CppFBPComponents module. This logic assumes all components in one dll file */
					
					strcpy(dllname, "TestSubnets.dll");
					hDLL = LoadLibrary(dllname);
					if (hDLL == NULL) {
						strcpy(szBuf, "LoadLibrary failed: ");
						strcat(szBuf, dllname);
						MessageBox(NULL, szBuf, "Library Functions", MB_ICONHAND);
					}
					else {

						/* Retrieve the address of the actual function. */
						
						strcpy_s(procname_in_dll, compname);

						faddr = (LPFNDLLFUNC)	GetProcAddress(hDLL, procname_in_dll);

						if (faddr == NULL) {
							GetLastError();
							strcpy(szBuf, "GetProcAddress failed: ");
							strcat(szBuf, procname_in_dll);
							MessageBox(NULL, szBuf, "Library Functions", MB_ICONHAND);
						}
					}					
			}
			//else {
			boost::thread thread(&Process::run, this);  
			//}
		}
		else if (status == DORMANT) {		
			canGo.notify_all();			
		}
	}

void Process::run() {

	//proc -> status = ACTIVE;

	/*
	run_test() returns 0 if data waiting
	1 if no data waiting, but not all upstream connections are closed
	2 if no input ports or input ports are only IIP ports or all input ports closed
	*/

	for( ; ; ) {
		if (2 == run_test()  && ! must_run && ! self_starting) 
			break;
		Port * cpp = in_ports;
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
		//
		value =
			faddr (proc_anchor);	
		//
		// component code returns
		//

		if (value > 0)
			printf ("Process %s returned with value %d\n",
			procname, value); 
		if (owned_IPs > 0)
			printf("%s Deactivated with %d IPs not disposed of\n",
			procname, owned_IPs);

		cpp = in_ports;
		while (cpp != 0)
		{
			for (int i = 0; i < cpp -> elem_count; i++) {
				Cnxt * cnp = cpp -> elem_list[i].gen.connxn;
				if (cnp == 0)
					continue;
				if (cpp -> elem_list[i].is_IIP) 
					cpp -> elem_list[i].closed = TRUE;	
			}
			cpp = cpp -> succ;
		}
		if (trace)
			printf("%s Deactivated with retcode %d\n",
			procname, value);

		if (value > 4) {
			//proc -> terminating = TRUE;
			break;
		}

		int res;
		/* res =  0 if data in cnxt; 1 if upstream not closed;
		2 if upstream closed */

		res = run_test();

		if (res == 2) break;
		if (res == 0) continue;

		dormwait();
	}



	/*
	Process * lpProc;
	// This should only be done at termination time!
	if (lpProc -> end_port != 0) {
	lpProc -> value = thzcrep(lpProc,
	&lpProc -> int_ptr, 0, "\0");
	lpProc -> int_pe.cpptr = lpProc -> end_port;
	lpProc -> value = thzsend(lpProc,
	&lpProc -> int_ptr, &lpProc -> int_pe, 0);
	if (lpProc -> value > 0)
	lpProc -> value = thzdrop(lpProc,
	&lpProc -> int_ptr);
	//break;
	}

	*/
	//close_outputPorts(proc);


	Port * cpp = out_ports;
	while (cpp != 0)
	{
		for (int i = 0; i < cpp -> elem_count; i++) {
			if (cpp -> elem_list[i].gen.connxn == 0)
				continue;
			/*value = */ thziclos(this, cpp, i);
		}
		cpp = cpp -> succ;
	}

	//close_inputPorts(proc);
	cpp = in_ports;
	while (cpp != 0)
	{
		for (int i = 0; i < cpp -> elem_count; i++) {
			if (cpp -> elem_list[i].gen.connxn == 0)
				continue;
			if (! cpp -> elem_list[i].is_IIP)
				thziclos(this, cpp, i);
		}
		cpp = cpp -> succ;
	}
	//printf("Terminated %s\n", proc -> procname);
	status = TERMINATED;
	if (trace)
		printf("%s Terminated with retcode %d\n",
		procname, value);
	//proc -> thread.join();
	network -> latch -> count_down();
} 

void inline Process::dormwait() {
	boost::unique_lock<boost::mutex> lock (mtx);
	status = DORMANT;
	canGo.wait(lock);
	status = ACTIVE;

}

int Process::run_test() {

	/*  
	returns 0 if data waiting
	1 if no data waiting, but not all upstream connections are closed
	2 if no input ports or input ports are only IIP ports or all input ports closed
	*/
	int res  = 2;
	Cnxt * cnp;
	Port * cpp = in_ports;
	while (cpp != 0)
	{
		for (int i = 0; i < cpp -> elem_count; i++) {
			cnp = cpp -> elem_list[i].gen.connxn;

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

bool Network::deadlock_test() {

	//testTimeouts(freq);
	if (active) {
		active = FALSE; // reset flag every 1/2 sec
	} else if (!possibleDeadlock) {
		possibleDeadlock = TRUE;
	} else {
		deadlock = TRUE; // well, maybe
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
		Process * this_proc = (Process *) first_child_proc;
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
			Process * this_proc = (Process *) first_child_proc;
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
		deadlock = FALSE;
		possibleDeadlock = FALSE;

	}
	return FALSE;
}

void Network::thxfcbs()
{
	Port *cpp, *oldPortp;
	Process *this_proc;
	Cnxt *this_cnxt;

	
		this_proc = first_child_proc;
	    while (this_proc != 0) {
		first_child_proc = this_proc -> next_proc;
		  cpp = this_proc -> out_ports;
		  while (cpp != 0) {
			oldPortp = cpp;
			cpp = cpp -> succ;
			free(oldPortp);
		  }
		  cpp = this_proc -> in_ports;
		  while (cpp != 0) {
			//for (i = 0; i < cpp -> elem_count; i++) {
				//Cnxt * cnp = (Cnxt *) cpp -> elem_list[i].gen.connxn;
				//if (cnp != 0) {						
					//free(cnp);
				//}
			//}
			oldPortp = cpp;
			cpp = cpp -> succ;
			free(oldPortp);
		  }
		  //old_proc = this_proc;
		  //this_proc = this_proc -> next_proc;
		  this_proc -> mtx.~mutex();
		  this_proc -> canGo.~condition_variable_any();
		  delete this_proc;
		  this_proc = first_child_proc;
	}

	this_cnxt = first_cnxt;
	while (this_cnxt != 0) {
		first_cnxt = this_cnxt -> succ;
		delete this_cnxt;
		this_cnxt = first_cnxt;
	}

	//if (!deadsw) {
		//delete network;
		//printf("Done\n");
	//}
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
 