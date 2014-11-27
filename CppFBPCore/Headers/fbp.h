
#include <dos.h>  
#if defined _WIN32
#include <windows.h>
#endif
#include <stdarg.h>

//#ifdef _DEBUG
//   #ifndef DBG_NEW
//      #define DBG_NEW new ( _NORMAL_BLOCK , __FILE__ , __LINE__ )
//      #define new DBG_NEW
//   #endif
//#endif  // _DEBUG

#define MSG0(text)  {printf(text); \

#define MSG1(text,par1)  {printf(text, par1); \
						 }
// Define 2-parameter macro for tracing

#define MSG2(text,par1,par2) {printf(text, par1, par2); \
					  }

// Define 3-parameter macro for tracing

#define MSG3(text,par1,par2,par3) { printf(text, par1, par2, par3); \
				   }


