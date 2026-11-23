#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>

#include <windows.h>
#include <fcntl.h>
#include <io.h>

#include "wio.h"

#define va_copy(destination, source) __va_copy(destination, source)

int wio_vfprintf(FILE* const stream, const char* const format, va_list list) {
	
	int length = -1;
	int wlength = 0;
	
	char* value = NULL;
	wchar_t* wvalue = NULL;
	
	va_list size_list;
	va_copy(size_list, list);
	
	wlength = vsnprintf(NULL, 0, format, size_list);
	
	va_end(size_list);
	
	if (wlength < 0 || wlength == INT_MAX) {
		goto end;
	}
	
	length = wlength;
	
	value = malloc((size_t) length + 1);
	
	if (value == NULL) {
		length = -1;
		goto end;
	}
	
	if (vsnprintf(value, (size_t) length + 1, format, list) < 0) {
		length = -1;
		goto end;
	}
	
	wlength = MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value, -1, NULL, 0);
	
	if (wlength == 0) {
		length = -1;
		goto end;
	}
	
	wvalue = malloc(((size_t) wlength) * sizeof(*wvalue));
	
	if (wvalue == NULL) {
		length = -1;
		goto end;
	}
	
	if (MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value, -1, wvalue, wlength) == 0) {
		length = -1;
		goto end;
	}
	
	if (fwprintf(stream, L"%ls", wvalue) < 0) {
		length = -1;
	}
	
	end:;
	
	free(value);
	free(wvalue);
	
	return length;
	
}

int wio_printf(const char* const format, ...) {
	
	int length = 0;
	
	va_list list;
	va_start(list, format);
	
	length = wio_vfprintf(stdout, format, list);
	
	va_end(list);
	
	return length;
	
}

int wio_fprintf(FILE* const stream, const char* const format, ...) {
	
	int length = 0;
	
	va_list list;
	va_start(list, format);
	
	length = wio_vfprintf(stream, format, list);
	
	va_end(list);
	
	return length;
	
}

int wio_enable_unicode(void) {
	
	int result = 0;
	
	if (_setmode(_fileno(stdout), _O_WTEXT) == -1) {
		result = -1;
	}
	
	if (_setmode(_fileno(stderr), _O_WTEXT) == -1) {
		result = -1;
	}
	
	if (_setmode(_fileno(stdin), _O_WTEXT) == -1) {
		result = -1;
	}
	
	return result;
	
}
