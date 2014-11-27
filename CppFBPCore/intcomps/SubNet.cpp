
#include "cppfbp.h"
#include "thzcbs.h"

#include <stdio.h>

class SubNet: Network {
public: 
	SubNet();
	void go();
};

SubNet::SubNet() {

} 

void SubNet::go() {
//	callDefine();
       // boolean res = true;
      //  for (Component comp : getComponents().values()) {
       //   res &= comp.checkPorts();
     //   }
      //  if (!res)
      //	  FlowError.complain("One or more mandatory connections have been left unconnected: " + getName());
    //    initiate();
        // activateAll();
        // don't do deadlock testing in subnets - you need to consider the whole net!
   //     deadlockTest = false;
        waitForAll();
}