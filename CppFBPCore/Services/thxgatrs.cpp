  #include <stdio.h>
    #include <io.h>
    #include <ctype.h>
    #include <stdlib.h>
    #include <string.h>
    //#include <setjmp.h>
    #include "thzcbs.h"
    #include "cppfbp.h"
    
        #define TRUE 1
    #define FALSE 0
    //#define _CRT_SECURE_NO_WARNINGS

    #define TC(label,ch)  if (curr_char != ch) goto label; \
                     *o_ptr = curr_char; o_ptr++;  \
                     curr_char = getc(fp);

    #define TCO(label,ch)  if (curr_char != ch) goto label;  \
                     curr_char = getc(fp);

    #define TA(label)    if (!isalpha(curr_char)) goto label; \
                     *o_ptr = curr_char; o_ptr++;  \
                     curr_char = getc(fp);

    #define TAO(label)   if (!isalpha(curr_char)) goto label; \
                     curr_char = getc(fp);

    #define TN(label)    if (!isdigit(curr_char)) goto label; \
                     *o_ptr = curr_char; o_ptr++;  \
                     curr_char = getc(fp);

    #define TNO(label)   if (!isdigit(curr_char)) goto label; \
                     curr_char = getc(fp);

    #define CC       *o_ptr = curr_char; o_ptr++;  \
                     curr_char = getc(fp);

    #define SC       curr_char = getc(fp);

   void  scan_blanks(FILE *fp);

   extern char curr_char;


  int thxgatrs(char * comp) {

       char *o_ptr;
       char out_str[255];
       int atr = 0;
       char fname[256];
       FILE *fp = NULL;

       strcpy(fname,comp);
	   strcat(fname,".atr");
	   FILE* pFile;
	   #ifdef WIN32
	   fopen_s(&pFile, fname, "r");
       #else
	   pFile = fopen(fname,"r");
       #endif
	   
       if (pFile == NULL)
          return(-1);

       do {curr_char = getc(fp);} while (curr_char == '\n');

       o_ptr = out_str;
       scan_blanks(fp);
       TCO(nEOF,EOF);
       goto exit;

  nEOF: for (;;) {
           TA(nA);
           continue;
  nA:      TN(nN);
           continue;
  nN:      TC(nus,'_');
           continue;
  nus:     break;
         }
        *o_ptr = '\0';
        if (strcmp(out_str,"MUST_RUN") == 0 ||
            strcmp(out_str,"Must_run") == 0 ||
            strcmp(out_str,"must_run") == 0)
            atr = TRUE;
          else
            atr = FALSE;
        scan_blanks(fp);

  exit: fclose(fp);
     return(atr);
}

 
 void scan_blanks(FILE *fp)
  {
     for (;;) {
	TCO(not_blank,' ');
	continue;
 not_blank: TCO(exit,'\n');
	}
 exit: return;
}      
