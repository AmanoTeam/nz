#include <stddef.h>
#include <stdio.h>

#if defined(_WIN32) && defined(_UNICODE)
	#include "wio.h"
#endif

#include "term/screen.h"

void download_progress_callback(const size_t total, const size_t current) {
	
	erase_line();
	
	printf("+ Fetching packages: %zu out of %zu\r", current, total);
	
	fflush(stdout);
	
}
