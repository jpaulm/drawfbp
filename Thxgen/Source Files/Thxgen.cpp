#include <stdafx.h>
#include <stdarg.h>
#include <stdio.h>
#include <io.h>
#include <ctype.h>
#include <setjmp.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>

#define GEN_BOOST
#include "cppfbp.h"
#include "thzcbs.h"

//typedef enum {FALSE, TRUE} Boolean;

// #define _CRT_SECURE_NO_WARNINGS 
/* 

This module scans off free-form network definitions (using thxscan), and converts them to 
fixed-format network definitions (FFNDs) (label_ent, proc_ent, cnxt_ent, etc.).

Output is written to {user}\Resource Files\{name}.cpp

*/
int thxscan(FILE *fp, label_ent *label_tab, char file_name[10]);

void main(int argc, char *argv[])  {

	cnxt_ent *cnxt_ptr;
	proc_ent *proc_ptr;

	label_ent *label_tab, *label_ptr;
	char gen_area [200];
	//char gen_comp_area[200];
	char * gen_ptr;
	FILE *fp, *fpo = 0;
	IIP *IIPptr;
	int proc_count, cnxt_count, iip_count;	
	int label_count = 0;
	int proc_ct = 0;
	char fname[256];
	char file_name[10];

	struct modent {
		char name[32+1];
	    }   modents[400];

	int modentnas = 0;
	int res = 0;

	if (argc != 2) {
		printf("You forgot to enter the file name\n");
		res = 8; goto retn;;
	}
	strcpy(fname,argv[1]);
	strcat(fname,".fbp");	
	
	  #ifdef WIN32
    errno_t err;
    if( (err  = fopen_s( &fp, fname, "r" )) !=0 ) {
#else
    if ((f = fopen(fname, "r")) == NULL) {
#endif
        fprintf(stderr, "Cannot open file %s!\n", fname);
		res = 8; goto retn;
    }  
	char home[255];
	
	char sep[2];
	#ifdef _WIN32
      strcpy(sep,"\\");
	  strcpy(home, getenv("HOMEDRIVE"));
	  strcat(home, getenv("HOMEPATH"));
    #else
      strcpy(sep,"/");
	  strcpy(home, getenv("HOME"));
    #endif
	strcpy(fname,home);
	strcat(fname,sep); 
	strcat(fname,argv[1]);
	strcat(fname,".cpp");
	//FILE** pFile;
	 #ifdef WIN32
    
    if( (err  = fopen_s( &fpo, fname, "w" )) !=0 ) {
#else
    if ((fpo = fopen(fname, "w")) == NULL) {
#endif
		printf("Cannot open Output Network: %s\n", fname);
		res = 8; goto retn; 
	} 
	printf("Creating Output Network: %s\n", fname);
	 

	label_tab = (label_ent *) malloc(sizeof(label_ent));   
	label_tab ->succ = 0;

	strcpy(file_name,"\0");

	if (thxscan(fp, label_tab, file_name) != 0) {
		printf("Scan error\n");
		res = 8; goto retn; 
	}

	gen_ptr = gen_area;
	strcpy(gen_ptr, "/* Generated Code */\n\n");
	fputs(gen_area,fpo);

	//gen_ptr = gen_area;
	//strcpy(gen_ptr, "#include \"stdafx.h\"\n");
	//fputs(gen_area,fpo);
	
	gen_ptr = gen_area;
	strcpy(gen_ptr, "#include \"thxdef.h\"\n");
	fputs(gen_area,fpo);

	/*
	gen_ptr = gen_area;
	strcpy(gen_ptr, "#include \"thxanch.h\"\n");
	fputs(gen_area,fpo);


	gen_ptr = gen_area;
	strcpy(gen_ptr, "#include \"thxscan.h\"\n");
	fputs(gen_area,fpo);

	*/
	gen_ptr = gen_area;
	strcpy(gen_ptr, "#include <stdio.h>\n");
	fputs(gen_area,fpo);

	gen_ptr = gen_area;
	strcpy(gen_ptr, "#define FILE struct _iobuf\n");
	fputs(gen_area,fpo);

	gen_ptr = gen_area;
	strcpy(gen_ptr,
		"void  CppFBP(label_ent * label_blk, bool dynam,  FILE * fp, bool timereq);\n");
	fputs(gen_area,fpo);
	//gen_ptr = gen_area;
	//strcpy(gen_ptr,  "bool NO_TRACE = false; bool TRACE = true;\n");
	//fputs(gen_area,fpo);

	proc_count = 0;
	iip_count = 0;
	cnxt_count = 0;
	char comp_name_end[20];

	label_ptr = label_tab;
	while (label_ptr != 0) {     
		proc_ptr = label_ptr -> proc_ptr;
		
		while (proc_ptr != 0) {
			int j = strlen(proc_ptr -> comp_name) - 1;	
			int k = j;

			while (j >= 0) {
				if (proc_ptr -> comp_name[j] == '/')
					break;
				j--; 
			}
			memcpy(comp_name_end, &proc_ptr->comp_name[j + 1], k - j);
			comp_name_end[k-j] = '\0';

			j = -1;
			
			for (int i=0; i < modentnas; i++) {
				if (strcmp(comp_name_end, modents[i].name) == 0) {
					j = i;
					break;
				}
			} 
			if (j == -1) {
				strcpy(modents[modentnas].name, comp_name_end);
				j = modentnas;
				modentnas ++;

				if (!proc_ptr -> composite) {
					gen_ptr = gen_area;
					strcpy(gen_ptr, "THRCOMP ");
					gen_ptr = strchr(gen_ptr, '\0');
					strcpy(gen_ptr, comp_name_end);
					gen_ptr = strchr(gen_ptr,'\0');
					strcpy(gen_ptr, "(anchor anch);\n");
					fputs(gen_area,fpo);
				}
			}
			gen_ptr = gen_area;
			strcpy(gen_ptr, "proc_ent P");
			gen_ptr = strchr(gen_ptr, '\0');

			_itoa(proc_count,gen_ptr,10);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, " = {");
			gen_ptr = strchr(gen_ptr,'\0');

			if (proc_count == 0) {
				strcpy(gen_ptr, "0");	
			}
			else {
				strcpy(gen_ptr, "&P");
				gen_ptr = strchr(gen_ptr,'\0');
				int i = proc_count - 1;
				_itoa(i,gen_ptr,10);
			}
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, ", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, proc_ptr -> proc_name);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, "\", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, proc_ptr -> comp_name);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, "\", ");
			gen_ptr = strchr(gen_ptr,'\0');

			//if (!proc_ptr -> composite) {
				// strcpy(gen_ptr, "(int ( PASCAL*)(anchor anch))");
				//gen_ptr = strchr(gen_ptr,'\0');  
				strcpy(gen_ptr, comp_name_end);
				gen_ptr = strchr(gen_ptr,'\0');
				strcpy(gen_ptr, ", 0, 0,");
				gen_ptr = strchr(gen_ptr,'\0');

			//}
			//else {
				//strcpy(gen_ptr, "0, 0, 0, ");
				//gen_ptr = strchr(gen_ptr,'\0');  
			//	strcpy(gen_ptr, proc_ptr -> comp_name);
			//	gen_ptr = strchr(gen_ptr,'\0');
			//	strcpy(gen_ptr, " 0, 0,");
			//	gen_ptr = strchr(gen_ptr,'\0');
			//}
			if (proc_ptr -> trace)
				strcpy(gen_ptr, " TRACE, ");       // trace  
			else
				strcpy(gen_ptr, " !TRACE, ");       // no trace  
			gen_ptr = strchr(gen_ptr,'\0');
			//_itoa(proc_ptr -> composite, gen_ptr, 10);
			strcpy(gen_ptr, " !COMPOS");       // no compos 
			gen_ptr = strchr(gen_ptr,'\0');
			strcpy(gen_ptr, "};\n");
			proc_count++;

			fputs(gen_area, fpo);
			proc_ptr = proc_ptr->succ;
		}  

		cnxt_ptr = label_ptr -> cnxt_ptr;

		while (cnxt_ptr != 0) {

				if (cnxt_ptr -> upstream_name[0] == '!') {
				gen_ptr = gen_area;
				strcpy(gen_ptr, "IIP I");
				gen_ptr = strchr(gen_ptr, '\0');

				_itoa(iip_count,gen_ptr,10);
				gen_ptr = strchr(gen_ptr,'\0');				

				strcpy(gen_ptr, " = {");
				gen_ptr = strchr(gen_ptr,'\0');
				IIPptr = cnxt_ptr -> gen.IIPptr;

				strcpy(gen_ptr, "\"");
				gen_ptr = strchr(gen_ptr,'\0');

				int j = strlen(IIPptr -> datapart);
				for (int i = 0; i < j; i++) {
					if (IIPptr -> datapart[i] == '\\')
						strcpy (gen_ptr, "\\\\");
					else
						if (IIPptr -> datapart[i] == '\'')
							strcpy (gen_ptr, "\\\'");
						else
							if (IIPptr -> datapart[i] == '\"')
								strcpy (gen_ptr, "\\\"");
							else
								if (IIPptr -> datapart[i] == '\?')
									strcpy (gen_ptr, "\\\?");
								else {
									*gen_ptr = IIPptr -> datapart[i];
									gen_ptr++;
									*gen_ptr = '\0';
								}
								gen_ptr = strchr(gen_ptr,'\0');
				}

				strcpy(gen_ptr, "\"};\n");

				fputs(gen_area,fpo);
			}

			gen_ptr = gen_area;
			strcpy(gen_ptr, "cnxt_ent C");
			gen_ptr = strchr(gen_ptr, '\0');
			_itoa(cnxt_count,gen_ptr,10);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, " = {");
			gen_ptr = strchr(gen_ptr,'\0');

			if (cnxt_count == 0) {
				strcpy(gen_ptr, "0");	
			}
			else {
				strcpy(gen_ptr, "&C");
				gen_ptr = strchr(gen_ptr,'\0');
				int i = cnxt_count - 1;
				_itoa(i,gen_ptr,10);
			}
			gen_ptr = strchr(gen_ptr,'\0');
			strcpy(gen_ptr, ", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, cnxt_ptr -> upstream_name);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, "\", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			if (cnxt_ptr -> upstream_name[0] == '!') {
				strcpy(gen_ptr, " \", 0");
				gen_ptr = strchr(gen_ptr,'\0');
			}
			else {
				strcpy(gen_ptr, cnxt_ptr -> upstream_port_name);
				gen_ptr = strchr(gen_ptr,'\0');

				strcpy(gen_ptr, "\", ");
				gen_ptr = strchr(gen_ptr,'\0');

				_itoa(cnxt_ptr -> upstream_elem_no,gen_ptr,10);
				gen_ptr = strchr(gen_ptr,'\0');
			}
			strcpy(gen_ptr, ", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, cnxt_ptr -> downstream_name);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, "\", \"");
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, cnxt_ptr -> downstream_port_name);
			gen_ptr = strchr(gen_ptr,'\0');

			strcpy(gen_ptr, "\", ");
			gen_ptr = strchr(gen_ptr,'\0');

			_itoa(cnxt_ptr -> downstream_elem_no,gen_ptr,10);
			gen_ptr = strchr(gen_ptr,'\0');

			if (cnxt_ptr -> upstream_name[0] == '!') {
				strcpy(gen_ptr, ", &I");
				gen_ptr = strchr(gen_ptr,'\0');
				_itoa(iip_count,gen_ptr,10);
				gen_ptr = strchr(gen_ptr,'\0');
				strcpy(gen_ptr, ", 0};\n");
				iip_count++;
			}
			else {
				strcpy(gen_ptr, ", 0, ");
				gen_ptr = strchr(gen_ptr,'\0');
				if (cnxt_ptr -> capacity == -1)
					strcpy(gen_ptr, "6");
				else
					_itoa(cnxt_ptr -> capacity,gen_ptr,10);
				gen_ptr = strchr(gen_ptr,'\0');
				strcpy(gen_ptr, "};\n");
			}

			fputs(gen_area, fpo);

		
			cnxt_count++;
			cnxt_ptr = cnxt_ptr -> succ;
		}
		label_ptr = label_ptr->succ;
	}


	gen_ptr = gen_area;
	//strcpy(gen_ptr, "label_ent LABELTAB = {0, \" \", \"\", &C0, &P0, \'L\'};\n");
	strcpy(gen_ptr, "label_ent LABELTAB = {0, \" \", \"\", &C");

	gen_ptr = strchr(gen_ptr,'\0');
				int i = cnxt_count - 1;
				_itoa(i,gen_ptr,10);

				gen_ptr = strchr(gen_ptr,'\0');
			strcpy(gen_ptr, ", &P");
			gen_ptr = strchr(gen_ptr,'\0');
			i = proc_count - 1;
				_itoa(i,gen_ptr,10);

				gen_ptr = strchr(gen_ptr,'\0');
				strcpy(gen_ptr, ", \'L\'};\n");

	fputs(gen_area,fpo);

	gen_ptr = gen_area;
	strcpy(gen_ptr, "void main() {\n");
	fputs(gen_area,fpo);
	gen_ptr = gen_area;
	strcpy(gen_ptr, "CppFBP(&LABELTAB, 0, 0, 0);\n}\n");

	fputs(gen_area,fpo);
	strcpy(gen_area, "Gen finished\nOutput written to: ");
	strcat(gen_area, fname);
	strcat(gen_area, "\n");
	printf(gen_area);
	// free(cnxt_tab);
	// free(proc_tab);
	free(label_tab);
	fclose(fp);
	fclose(fpo);   
	
retn:
	system("pause");  // to see console
	return;
}