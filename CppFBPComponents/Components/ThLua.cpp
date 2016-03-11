
#include "StdAfx.h"

extern "C" {
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}
#include "compsvcs.h"
#include <string.h>

int thl_recv(lua_State *L) ;
int thl_drop(lua_State *L) ;

int thl_crep(lua_State *L) ;
int thl_send(lua_State *L) ;
int thl_elct(lua_State *L) ;
int thl_clos(lua_State *L) ;
int thl_deref(lua_State *L) ;

int myprint(lua_State *L) ;

static void stackDump (lua_State *L) ;

static const luaL_Reg lualibs[] = {
	{"", luaopen_base},
	{LUA_LOADLIBNAME, luaopen_package},
	{LUA_TABLIBNAME, luaopen_table},
	{LUA_IOLIBNAME, luaopen_io},
	{LUA_OSLIBNAME, luaopen_os},
	{LUA_STRLIBNAME, luaopen_string},
	{LUA_MATHLIBNAME, luaopen_math},
	{LUA_DBLIBNAME, luaopen_debug},
	{NULL, NULL}
};

LUALIB_API void luaL_openlibs (lua_State *L) {
	const luaL_Reg *lib = lualibs;
	for (; lib->func; lib++) {
		lua_pushcfunction(L, lib->func);
		lua_pushstring(L, lib->name);
		lua_call(L, 1, 0);
	}
}

void report_errors(lua_State *L, int status)
{
  if ( status!=0 ) {
	char error[100];
    strcpy(error, "-- ");
	strcat(error, lua_tostring(L, -1));
	printf(error);
    lua_pop(L, 1); // remove error message
  }
}

THRCOMP ThLua(_anchor proc_anchor)
{

	int value;	

	port_ent port_tab[1];

	value = dfsdfpt(proc_anchor, 1, port_tab, "PROG");

	char pgmname [100];
	void * ptr;
	long size;
	char *type;

	value = dfsrecv(proc_anchor, &ptr, &port_tab[0], 0, &size, &type);
	memcpy(pgmname,ptr,size);
	pgmname[size] = '\0';

	value = dfsdrop(proc_anchor, &ptr);

	//lua_State *L = lua_open();   // obsolete
	lua_State *L = luaL_newstate();

	luaL_openlibs (L);

	lua_pushstring(L, "lua_dfsanchor");  /* push key */
	lua_pushlightuserdata(L, (void *)&proc_anchor);  /* push address */

	lua_settable(L, LUA_REGISTRYINDEX);

	lua_register(L, "dfsrecv", thl_recv);
	lua_register(L, "dfsdrop", thl_drop);
	lua_register(L, "dfscrep", thl_crep);
	lua_register(L, "dfssend", thl_send);
	lua_register(L, "dfselct", thl_elct);
	lua_register(L, "dfsclos", thl_clos);
	lua_register(L, "dfsderef", thl_deref);

	lua_register(L, "myprint", myprint);

	int s = luaL_loadfile(L, pgmname);

    if ( s==0 ) {
      // execute Lua program
      s = lua_pcall(L, 0, LUA_MULTRET, 0);
    }
	report_errors(L, s);
	lua_close(L);
	return(0);
}

int thl_recv(lua_State *L) {
	
	char port_and_elemno[100];
	
	char *type;


	const char* pn =  lua_tostring(L, -1);  
	
	strcpy (port_and_elemno, pn);
	
		
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;

	void * ptr;
	
	long size;
	int value = dfsrecvc(* anch, &ptr, port_and_elemno, &size, &type );
	lua_pushnumber(L, value);
	if (value == 0) {
		lua_pushlightuserdata(L, ptr);
		lua_pushnumber(L, size);
		lua_pushstring(L, type);
		return 4;
	} else 
		return 1;
}

int thl_drop(lua_State *L) {
	
	const void* IPaddr =  lua_topointer(L, -1);  
	
		
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;

	void * ptr = (void *) IPaddr;
	
//	long size;
	int value = dfsdrop(* anch, &ptr);
	lua_pushnumber(L, value);
	return 1;
}

int thl_crep(lua_State *L) {
	
	int i = lua_isstring(L, -1);  // make sure it's a string...
	if (i != 1) 
		return 4;
	const void * ptr = lua_tostring(L, -1);
	char * tptr = (char *) ptr;
	void * IPptr;				
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;
	
	auto len = strlen(tptr);
	++len;                          // it's a string, so leave room for terminating null!
	int value = dfscrep(* anch, &IPptr, static_cast<int>(len), "A");  

	strcpy((char *) IPptr , tptr);

	lua_pushnumber(L, value);
	lua_pushlightuserdata(L, (void *) IPptr);
	return 2;
}

int thl_send(lua_State *L) {

	char port_and_elemno[100];
	
	const void * pn = lua_tostring(L, -2);  // port name
	strcpy (port_and_elemno, (char *) pn);

	const void* IPaddr =  lua_topointer(L, -1); 

				
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;
	
	int value = dfssendc(* anch, (void **) &IPaddr, port_and_elemno);  
	lua_pushnumber(L, value);
	
	return 1;
}

int thl_elct(lua_State *L) {

	char port[100];
	
	const void * pn = lua_tostring(L, -1);  // port name
	strcpy (port, (char *) pn);
					
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;
	
	int value = dfselctc(* anch, port);  
	lua_pushnumber(L, value);
	
	return 1;
}

int thl_clos(lua_State *L) {

	char port_and_elemno[100];

	const char* pn =  lua_tostring(L, -1);  
	
	strcpy (port_and_elemno, pn);	
		
	lua_pushstring(L, "lua_dfsanchor");  /* push key */
    lua_gettable(L, LUA_REGISTRYINDEX);  /* retrieve value */

	_anchor * anch = (_anchor *) lua_topointer(L, -1);
	//Process * proc = (Process *) anch -> reserved;

	
	int value = dfsclosc(* anch, port_and_elemno );
	lua_pushnumber(L, value);
	return 1;
}

int thl_deref(lua_State *L) {
	const char* pn =  (char *) lua_topointer(L, 1);  	
	lua_pushstring(L, pn);
	return 1;
}

int myprint(lua_State *L) {
	const char* pn =  (char *) lua_topointer(L, 1);  	
	printf("Data: %s\n", pn);	
	lua_pop(L,-1);
	return 0;
}


static void stackDump (lua_State *L) {
      int i;
      int top = lua_gettop(L);
      for (i = 1; i <= top; i++) {  /* repeat for each level */
        int t = lua_type(L, i);
        switch (t) {
    
          case LUA_TSTRING:  /* strings */
            printf("`%s'", lua_tostring(L, i));
            break;
    
          case LUA_TBOOLEAN:  /* booleans */
            printf(lua_toboolean(L, i) ? "true" : "false");
            break;
    
          case LUA_TNUMBER:  /* numbers */
            printf("%g", lua_tonumber(L, i));
            break;
    
          default:  /* other values */
            printf("%s", lua_typename(L, t));
            break;
    
        }
        printf("  ");  /* put a separator */
      }
      printf("\n");  /* end the listing */
    }