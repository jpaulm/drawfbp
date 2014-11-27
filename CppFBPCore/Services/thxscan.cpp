#pragma optimize( "", off )
#include <stdio.h>
#include <malloc.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>

#include "cppfbp.h"
#include "thzcbs.h"
#define TRUE 1
#define FALSE 0

#if __OS2__ == 1
#define getc fgetc
#undef  EOF
#define EOF 0xff
#endif 


#define TC(label,ch) { if (curr_char != ch) goto label; \
    *o_ptr = curr_char; o_ptr++;  \
    curr_char = getc(fp); }

#define TCO(label,ch) { if (curr_char != ch) goto label;  \
	curr_char = getc(fp); }

#define TCIO(label,ch)  {if (curr_char != ch) goto label; }

#define TA(label)  {  if (!isalpha(curr_char)) goto label; \
	*o_ptr = curr_char; o_ptr++;  \
	curr_char = getc(fp);}

#define TAO(label) {  if (!isalpha(curr_char)) goto label; \
	curr_char = getc(fp);}

#define TN(label)   { if (!isdigit(curr_char)) goto label; \
	*o_ptr = curr_char; o_ptr++;  \
	curr_char = getc(fp);}

#define TNO(label) {  if (!isdigit(curr_char)) goto label; \
	curr_char = getc(fp);}

#define CC    {   *o_ptr = curr_char; o_ptr++;  \
	curr_char = getc(fp);}

#define SC      { curr_char = getc(fp); }

char curr_char;

int scan_blanks(FILE *fp);
int scan_sym(FILE *fp, char * out_str);

label_ent * find_label(label_ent *label_tab, char name[32], char file[10],
	int label_count);
int thxgatrs(char * comp);
proc_ent *find_or_build_proc(char * nm);


proc_ent *proc_tab;
label_ent *label_curr;
char comp_name[200];
bool eof_found = FALSE;
char eol = '\n';

/*
Currently, no label at beginning, and no subnet support.

thxscan scans off the free form network definition, generating fixed format definitions (FFNDs)

It has been converted to support NoFlo .fbp notation...

thxscan is either used by Thxgen to generate FFNDs, or by CppFBP in dynamic mode
*/

int thxscan(FILE *fp, label_ent *label_tab, char file_name[10])
{
	char *o_ptr;
	char out_str[255];
	//char fname[256];
	char out_num[8];
	int i, IIPlen, ret_code;
	char upstream_name[255];	
	char upstream_port_name[255];
	int upstream_elem_no;
	char procname[255];	


	proc_ent *proc_curr;
	//	proc_ent *proc_new;
	//	proc_ent *proc_find;
	cnxt_ent *cnxt_tab;
	cnxt_ent *cnxt_curr;
	cnxt_ent *cnxt_new;
	cnxt_ent *cnxt_hold;

//	label_ent *label_new;

	//int label_ct;
	bool eq_arrow;
	IIP *IIP_ptr;
	//FILE *fp2;
	int res;

	ret_code = 0;
	
	curr_char = getc(fp);
	proc_tab = 0;
	cnxt_tab = 0;	
	label_curr = label_tab;

	strcpy(label_curr->label," ");
	strcpy(label_curr->file, file_name);
	label_curr->ent_type = 'L';

	IIPlen = -1;
	out_num[0] = '\0';
	cnxt_hold = 0;
	

netloop:
	scan_blanks(fp);  // skip blanks, comments, etc.
	TCO(X0,'\'');
	goto Xs;
X0:
	if (scan_sym(fp, out_str) != 0) {  // this could be a network label or a process name...
		printf("Name error\n");
		ret_code = 4;
		goto exit;  
	}
	scan_blanks(fp);
	TCO(X2,':');
	strcpy(label_curr->label, out_str);  // it was a label		  
	printf("Scanning Network: %s\n",out_str);

bigloop:	
	
	scan_blanks(fp);
	cnxt_hold = 0;
	IIPlen = -1;
	TCO(X1,'\'');
Xs:
	o_ptr = out_str;	
	goto get_rest_of_IIP;

X1:
	if (scan_sym(fp, out_str) != 0) {  // this must be a process name...
		printf("Name error\n");
		ret_code = 4;
		goto exit;  
	}
X2:
	strcpy(procname, out_str);  
	printf("Procname: %s\n", procname);
	if (cnxt_hold != 0) {
		strcpy(cnxt_hold -> downstream_name, procname);
		cnxt_hold = 0;
	}

	proc_curr = find_or_build_proc(procname);

	res = scan_blanks(fp);
	
	if (2 == res)
		goto bigloop; 
	
	goto X3;

get_rest_of_IIP:    
	TCO(tbsl,EOF);  
	printf("EOF encountered within quoted string\n");
	ret_code = 4;
	goto exit;
tbsl:
	TCO(tbq,'\\');
	CC;
	goto get_rest_of_IIP;
tbq:
	TCO(copy,'\'');
	goto NQ2;
copy:
	CC;
	goto get_rest_of_IIP;
NQ2:   
	*o_ptr = '\0';
	IIPlen = o_ptr - out_str;	  
	IIP_ptr = (IIP *)malloc(IIPlen + 1);	  
	memcpy(IIP_ptr -> datapart, out_str, IIPlen);
	IIP_ptr -> datapart[IIPlen] = '\0';	  
	printf("IIP: %s\n", IIP_ptr -> datapart);
	scan_blanks(fp);
	//IIPlen = -1;
	goto tArrow;

 
	// scan off process name with optional component name

X3:
	TCO(NB1,'('); 
	scan_blanks(fp);
	TCO(rest_of_comp, ')');
	goto NN2;
rest_of_comp:	
	//scan_blanks(fp);
	TCO(NQ6,'"');           // this says that comp name may be surrounded by double quotes (not included in comp_name)
NQ6: 
	o_ptr = comp_name;      // if no data between brackets, comp_name will not be modified...

TB1: 
	TCO(NQ7, '"');
	scan_blanks(fp);

NQ7:  
	TCO(NB2, ')');
	goto NN2;
NB2:
	CC;
	goto TB1;

NN2:
	*o_ptr = '\0';

	if (strlen(comp_name) > 0) {
		strcpy(proc_curr -> comp_name, comp_name);
		printf("Comp name: %s\n",comp_name); 
	}

NB1: 
	// comp scanned off, if any
	strcpy(upstream_name, procname);	    // in case this proc is the upstream of another arrow
	res = scan_blanks(fp);
	
	if (res == 2)   {  
		//IIPlen = -1;          //  EOL encountered among blanks?
		goto bigloop;
	}
	TCO(NQ1,'?');
	proc_curr->trace = 1;
NQ1: 

	//if (cnxt_hold != 0) {
	//  strcpy(cnxt_hold->downstream_name, procname);
	//  cnxt_hold = 0;
	//}

	res = scan_blanks(fp);
	
	if (res == 2)  {        //  EOL encountered among blanks?
		//IIPlen = -1;
		goto bigloop;
	}

	//o_ptr = out_str;
	//TC(outport,'*');     /* automatic port */
	//*o_ptr = '\0';
	//IIPlen = -1;
	//goto GUy;

//outport:  -- same as upstream port
	res = scan_blanks(fp);

	// now we test for end of clause
	// if scan_blanks found an EOL, res will be set to 2, not 0 - if so, it is end of clause

	if (res == 2) {
		//IIPlen = -1;
		goto bigloop;
	}

	TCO(tsc,EOF);
	eof_found = TRUE;
	goto nxtnet;

tsc:
	TCO(tiip,';'); 
nxtnet:
	IIPlen = -1;
	scan_blanks(fp);
	TCO(nextnet,EOF); 
	goto exit;

tiip:	   
	IIPlen = -1;
	TCO(get_up_port,',');	
	
	scan_blanks(fp);
	goto bigloop; 

get_up_port:	
	o_ptr = out_str;
	TC(outport,'*');     /* automatic port */
	*o_ptr = '\0';
	
	goto GUy;

outport:
	if (scan_sym(fp,  out_str) != 0) {
		printf("Name error\n");
		ret_code = 4;
		goto exit;  
	}
GUy:
	scan_blanks(fp);
	
	strcpy(upstream_port_name, out_str);
	printf("Upstream port: %s\n", out_str);
	upstream_elem_no = 0;
	TCO(tArrow,'[');
	o_ptr = out_num;
GNx:  
	TN(NNx);
	goto GNx;
NNx:  
	TCO(elemerr,']');
	*o_ptr = '\0';
	upstream_elem_no = atoi(out_num);
	scan_blanks(fp);

tArrow: 
	scan_blanks(fp);
	eq_arrow = FALSE;
	TCO(tEq,'-');
	goto tGr;
tEq: 
	TCO(nArrow,'=');
	eq_arrow = TRUE;
tGr:
	TCO(nArrow,'>');
	printf("Arrow\n");
	cnxt_new = (cnxt_ent *) malloc (sizeof(cnxt_ent)); 
	cnxt_new->succ = 0;
	cnxt_new->dropOldest = false;
	if (cnxt_tab == 0) {
		cnxt_tab = cnxt_new;
		label_curr ->cnxt_ptr = cnxt_tab;		
		cnxt_curr = cnxt_tab;
	}
	else {		  
		cnxt_curr -> succ = cnxt_new;
		cnxt_curr = cnxt_new;
	}

	cnxt_hold = cnxt_new;
	if (IIPlen != -1) {
		strcpy(cnxt_hold->upstream_name, "!");
		cnxt_hold->upstream_port_name[0] = '\0';
		cnxt_hold->gen.IIPptr = IIP_ptr;
	}
	else {
		strcpy(cnxt_hold -> upstream_name, upstream_name);
		strcpy(cnxt_hold -> upstream_port_name, upstream_port_name);

		cnxt_hold -> upstream_elem_no = upstream_elem_no;
	}
	cnxt_hold->capacity = -1;
	scan_blanks(fp);
	TCO(ncap,'(');
	o_ptr = out_num;
GNz: 
	TN(NNz);
	goto GNz;
NNz: 
	TCO(caperr,')');
	*o_ptr = '\0';
	cnxt_hold->capacity = atoi(out_num);
	scan_blanks(fp);
	goto ncap;
caperr: 
	printf("Capacity error\n");
	ret_code = 4;
	goto exit;
ncap:
	cnxt_hold->downstream_elem_no = 0;

	/* Scan off downstream port name */
	o_ptr = out_str;
	TC(Y2a,'*');       /* automatic port */
	*o_ptr = '\0';
	strcpy(cnxt_hold->downstream_port_name, out_str);  /* ext. conn */
	goto is_outport;
Y2a: 
	if (scan_sym(fp,  out_str) != 0) {
		printf("Downstream port name error for %s %s\n",
			cnxt_hold->upstream_name,
			cnxt_hold->upstream_port_name);
		ret_code = 4;
		goto exit;  
	}
	strcpy(cnxt_hold->downstream_port_name, out_str);

is_outport:

	printf("Downstream port: %s\n", cnxt_hold->downstream_port_name);

	scan_blanks(fp);
	TCO(X1,'[');
	o_ptr = out_num;
GNy: 
	TN(NNy);
	goto GNy;
NNy:  
	TCO(elemerr,']');
	*o_ptr = '\0';
	cnxt_hold->downstream_elem_no = atoi(out_num);
	//cnxt_hold = 0;
	scan_blanks(fp);
	goto X1;   

nextnet:
	scan_blanks(fp);
	//TCO(nEOF2,EOF);  
	//   printf("End of Network Definition\n");
	//goto exit;

//nEOF2:
	/*
	label_new = (label_ent *)malloc (sizeof(label_ent));
	label_curr -> succ = label_new;
	label_curr = label_new;	  
	label_curr->label[0] = '\0';
	label_curr-> succ = 0;
	label_curr ->cnxt_ptr = cnxt_hold;
	label_curr ->proc_ptr = proc_curr;
	*/
	if (eof_found) {
		label_curr -> succ = 0;   // temporary fix as we are only generating one network for now
		goto exit;
	}

	goto netloop;


elemerr:
	printf("Port element error\n");
	ret_code = 4;
	goto exit;

nArrow: 
	printf("No arrow found\n");
	ret_code = 4;
exit:
	printf("\nSummary:\n");
	proc_curr = proc_tab;
	while (proc_curr != 0) {	
		printf(" Process: %s (%s)\n",proc_curr -> proc_name,
			proc_curr -> comp_name);
		proc_curr = proc_curr -> succ;
	}

	cnxt_hold = cnxt_tab;
	while (cnxt_hold != 0) {
		char up[200];
		char down[200];
		char elem[20];
		if (cnxt_hold -> upstream_name[0] != '!') {
			strcpy(up, cnxt_hold -> upstream_port_name);
			if (up[0] != '*') {
				strcat(up, "[");				
				_itoa(cnxt_hold -> upstream_elem_no, elem, 10);
				strcat(up, elem);
				strcat(up, "]");
			}
			strcpy(down, cnxt_hold -> downstream_port_name);
			if (down[0] != '*') {
				strcat(down, "[");
				_itoa(cnxt_hold -> downstream_elem_no, elem, 10);
				strcat(down, elem);
				strcat(down, "]");
			}
			printf(" Connection: %s %s -> %s %s\n",
			cnxt_hold -> upstream_name,
			up,			
			down,
			cnxt_hold -> downstream_name);
		}
		else {
			strcpy(down, cnxt_hold -> downstream_port_name);
			if (down[0] != '*') {
				strcat(down, "[");
				_itoa(cnxt_hold -> downstream_elem_no, elem, 10);
				strcat(down, elem);
				strcat(down, "]");
			}
			printf(" IIP: -> %s %s\n",
				down,
				cnxt_hold -> downstream_name);
			IIP_ptr = cnxt_hold -> gen.IIPptr;
			printf("    \'");
			int j = strlen(IIP_ptr -> datapart);
			for (i = 0; i < j; i++)
				printf("%c",IIP_ptr -> datapart[i]);
			printf("\'\n");
		}
		cnxt_hold = cnxt_hold -> succ;
	}
	if (fclose(fp) != 0) {
		printf("Close error\n");
		if (ret_code == 0)
			ret_code = 2;
	}
	if (ret_code > 0) {
		// printf("Scan error\n");
		return(ret_code);
	}
	// subnet processing?
	/*
	proc_last = proc_count;
	for (i = proc_hold; i < proc_last; i++) {
	proc_curr = &proc_tab[i];
	if (proc_curr -> proc_name[0] == '\0') continue;
	label_ct = find_label(label_tab, proc_curr -> comp_name, file_name, label_count);
	proc_curr -> composite = (label_ct > 0);
	if (proc_curr -> composite)
	proc_curr -> label_count = label_ct;
	else {
	strcpy(fname, proc_curr -> comp_name);
	if ((fp2 = fopen_s(strcat(fname, ".net"),"r"))
	!= NULL) {
	label_ct = label_count;
	strcpy(fname, proc_curr -> comp_name);
	thxscan(fp2, label_tab, fname);
	fclose(fp2);
	proc_curr -> composite = TRUE;
	//proc_curr -> label_count = label_ct;
	}
	else {
	// proc_curr -> must_run =
	//    (thxgatrs(proc_curr -> comp_name) > 0);
	proc_curr -> composite = FALSE;
	proc_curr -> faddr = 0;
	}
	}
	}
	*/

	// printf("Scan finished\n");
	return (ret_code);
}

/* 

Scan off blanks - returns: 
4 if EOF encountered in a comment
0 otherwise

if end of line encountered, returns 2 - 
this is only tested for at one place in the logic, where it can mean end of clause (NoFlo convention); 
elsewhere it is ignored

*/

int scan_blanks(FILE *fp)
{
	//extern char curr_char;
	int res = 0;

	for (;;) {
sbs:
		TCO(not_blank,' ');
		continue;
not_blank: 
		TCO(neol, eol);
		res = 2;
		continue;
neol:
		TCO(ncom,'#');   // comment runs from #-sign to end of line
		for (;;) {
			TCO(tasu,EOF);
			//printf("EOF encountered within comment\n");
			//res = 4;
			goto exit;
tasu:
			TCO(nnl,eol);		
			break;
nnl:        SC;  // skip character
		}
		goto sbs;		
ncom:
		break;
		

	}
exit: 	
	return(res);
}

/*
Scan off a network label or process name (this is used for ports as well, as we don't know if a string is a port until later...)
*/
inline int scan_sym(FILE *fp, char * out_str)
{
	//extern char curr_char;
	char * o_ptr;
//	int i;

	o_ptr = out_str;
X4:
	TA(NA4);	
	goto X5;
NA4: 
	TN(NN4);
	goto X5;
NN4: 
	TC(NU4,'_');
	goto X5;
NU4:
	TCO(ES4,'\\');
    CC;
	
X5:
	goto X4;
ES4:
	*o_ptr = '\0';           
	return(0);
}

label_ent * find_label(label_ent *label_tab, char name[32], char file[10],  int label_count )
{
	label_ent * label_new;
	label_new = label_tab;
	while (label_new != 0){

		if (label_new -> label[0] == '\0') 
			continue;  
		if (label_new -> ent_type != 'L') 
			continue;  
		if (strcmp(label_new->label, name) == 0  &&
			(strcmp(label_new->file, "\0") == 0 ||
			strcmp(label_new->file, file) == 0))
			break;
		label_new = label_new->succ;
	}
	return(label_new);
}

proc_ent * find_or_build_proc(char * name) {
	proc_ent * this_proc = proc_tab;	   
	proc_ent * last_proc = 0;
	while (this_proc != 0) {			
		if (strcmp(this_proc->proc_name, name) == 0) break;	
		last_proc = this_proc;
		this_proc = this_proc->succ;
	}

	if (this_proc == 0)  {   // not found
		proc_ent * proc_new = (proc_ent *) malloc(sizeof(proc_ent));		   
		if (proc_tab == 0){
			proc_tab = proc_new;	
			label_curr->proc_ptr = proc_tab;
		}
		else {
			last_proc -> succ = proc_new;
		}

		this_proc = proc_new;

		//proc_curr->proc_name[0] = '\0';	
		this_proc-> succ = 0;
		this_proc-> composite = 0;
		this_proc-> faddr = 0;
		strcpy(this_proc->proc_name, name);
		this_proc->trace = 0;
		this_proc->comp_name[0] = '\0';
	}
	return this_proc;
}




