#ifndef DLLHEADER_H_INCLUDED
#define DLLHEADER_H_INCLUDED

#ifdef _WIN32
# define EXPORT extern "C" __declspec (dllexport)
#elif
#    define EXPORT
#endif

#endif
