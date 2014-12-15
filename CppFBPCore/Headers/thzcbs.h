#include <boost/thread/condition.hpp>
#include <boost/thread/thread.hpp>
#include <boost/thread/cv_status.hpp>

#ifndef THXDEF
#define THXDEF
#include "thxdef.h"
#endif

// In what follows, IP means Information Packet  


class Cdl {                  // countdown latch
public:
	Cdl(int ct);
	int wait();  // 1 = deadlock; 0 = count completed
	void count_down();

private:
	int count;
	boost::mutex mutex;
	boost::condition_variable_any cond_var;
} ;


/* 
cp and cp_elem handle the connections between a port and its owning process -
each cp block is followed by an array of cp element blocks, each of which points at a connection (Cnxt), an IIP or zero
*/
class Cnxt;
struct Port_elem {     // port control block element -
	//   port is represented by port control block,
	//   followed by an array of port control block
	//   elements
	//-----------------------------------------------
	union cp_union {Cnxt *connxn; struct _IIP *IIPptr;}
	gen;     //  this field may point at a connection or
	//    an Initial IP (IIP)  or zero (i.e. unused)
	//  (an IIP is like a parameter - it is specified
	//    in the network, and then turned into an
	//    actual IP at run-time by THZRECV) 
	bool closed;   //  port element is closed
	bool is_IIP;   //  port element points at an IIP
	bool subdef;   //  port elem is defined in a subnet definition
} ;

typedef  Port_elem cp_elem;

class Port {          //  port control block (see above)
	//-----------------------------------------
public:
	Port *succ;  // next port in input port chain or output
	//  port chain - these are separate chains
	char port_name[32];   // port name

	int elem_count;       //  count of elements in port 
	bool direction;   //  input/output port: TRUE = output
	//   0 = input
	//   1 = output 

	struct Port_elem elem_list[1]; // array of port control block elements

} ;


struct _IPh {                 //  Information Packet header
	//-------------------------------------
	char  *type;            //  ptr to string specifying IP 'type'
	struct _IPh   *next_IP; //  ptr to next IP in connection or stack
	struct _IPh   *prev_IP; //  ptr to previous IP in connection or
	//      stack 
	void  *owner;           //  ptr to 'owner' - may be process or
	//      connection
	int reserved;              //  padding
	bool on_stack;         //  IP is on stack - TRUE or FALSE
	long IP_size;              //  size of IP - excluding header
} ;

typedef  _IPh IPh;



struct _IP {                  //  Information Packet
	//--------------------------------------
	struct _IPh IP_header;     //    header, followed by 0 - 32767 
	char unsigned datapart[32767];  //   bytes
} ;

typedef  _IP IP;


#define guard_value 219  /* solid rectangle */

class Cnxt;
class Network;
class Process {             // process control block
	//--------------------------------------
public:
	char procname[128];         // process name
	char compname[200];         // component name
	//Process *next_proc;   // ptr to next process in chain
	//       of ready processes (dynamic)
	//jmp_buf state;             // state of component - used by longjmp
	//    from scheduler back to component 
	Network *network;            // ptr to application control block
	Process *next_proc;  // ptr to next sibling process
	//   within subnet (static)
	Process *mother_proc;  // ptr to 'mother' process 
	Port *in_ports;        // ptr to first input port control block
	Port *out_ports;       // ptr to first output port control block
	//struct _IPh   *first_owned_IP;   // ptr to first IP owned by this
	//     process 
	Port *begin_port;      // ptr to 'beginning' port - if specified,
	//   process is delayed until IP arrives
	//   at this port
	Port *end_port;        // ptr to 'ending' port - if specified,
	//   process sends a signal to this port
	//   when it finally terminates  
	struct _port_ent int_pe;   // 'port_ent' block internal to process
	//    control block  - allows 'ending' port
	//    logic to use THZSEND
	void *int_ptr;             // holds ptr to IP created by 'ending'
	//    port logic 
	int value;                 // holds return code from 'ending' port
	//    call to THZSEND

	int ( __stdcall *faddr) (_anchor anch);   // address of code to be
	//     executed by this process

	_anchor proc_anchor;  // anchor to be passed to service calls
	struct _IPh   *stack;   //  ptr to first IP in process stack -
	//     managed by THZPUSH and THZPOP
	char status;               //  status of process execution:
	//     values defined in #defines below

	Cnxt * waiting_cnxt;   // connection process is waiting on
	bool trace;            //  trace required for process
	//bool terminating;      //  process is terminating
	bool must_run;         //  process must run at least once
	//unsigned has_run;          //  process has run at least once
	bool composite;        //  process is 'mother' of a subnet

	long owned_IPs;       // number of owned IPs

	boost::thread thread;   
	void run();

	void dormwait();
	int run_test();


	
	boost::condition canGo;
	boost::mutex mtx;


	/* following #defines are possible values of 'status' above */

#define NOT_STARTED           ' '    // not initiated
#define ACTIVE                'A'    // active
#define DORMANT               'D'    // waiting to be triggered
#define SUSPENDED_ON_SEND     'P'    // suspended on send 
#define SUSPENDED_ON_RECV     'G'    // suspended on receive
	//#define INITIATED             'I'    // initiated
	//#define READY_TO_RESUME       'R'    // ready to resume
#define TERMINATED            'T'    // terminated


	Process(){
		Process::status = NOT_STARTED ;
	}

	 
	void activate(); 
	 
	

};



class Cnxt {                // control block for a connection
	//--------------------------------------  
public:
	char name[128];             // name
	Process *fed_proc;         // ptr to process 'fed' by this connection
	//  (can only be one) 
	
	struct _IPh   *first_IPptr;  // ptr to first IP in connection 
	struct _IPh   *last_IPptr;   // ptr to last IP in connection
	Cnxt *succ;        // successor connection block - used for cleanup
	long max_IPcount;          // maximum count of IPs allowed = capacity
	long IPcount;              // actual count of IPs in connection
	int  total_upstream_proc_count;   // total no. of upstream port
	//      elements            
	int  nonterm_upstream_proc_count; // no. of upstream port elements
	//    which have not terminated - 0 means
	//    the connection is closed
	
	bool closed;


	//private:
	boost::condition buffer_not_full, buffer_not_empty;
	boost::mutex mtx;
} ;

class Network
{
public:
	char name[128];             // name of application

	Process* first_child_proc;  // ptr to first child process
	Process* first_child_comp;  // ptr to first child component

	struct _IPh   * alloc_ptr;  // ptr to allocated IP
	long alloc_lth;                // length of requested IP
	bool dynam;                     // DLLs being dynamically loaded (interpretive mode)

	Cnxt *first_cnxt;

	Cdl * latch;   // count down latch
	bool active;
	
	bool possibleDeadlock;
	bool deadlock;
	boost::mutex heapmtx;

   
    void waitForAll();
    void thxfcbs();
	bool deadlock_test();
    void go(label_ent * label_blk, bool dynam, FILE * fp, bool timereq, _anchor proc_anchor); 
};


class SubNet:Network
{
public:
//	char name[32];             // name of subnet
	void go(label_ent * label_blk, bool dynam, FILE * fp, bool timereq, _anchor proc_anchor); 
};