#include <stdlib.h>
#include <ctype.h>
#include <string.h>

#include "urlencode.h"

static const char HEX_DIGITS[] = "0123456789ABCDEF";

size_t urlencode(
	const char* const uri,
	char* const destination
) {
	
	size_t index = 0;
	size_t offset = 0;
	size_t size = 0;
	
	unsigned char ch = 0;
	
	for (index = 0; (ch = (unsigned char) uri[index]) != '\0'; index++) {
		
		if (__builtin_expect(isalnum(ch) || ch == '-' || ch == '.' || ch == '_' || ch == '~', 1)) {
			if (destination != NULL) {
				destination[offset++] = ch;
			}
			
			size += 1;
		} else if (ch == ' ') {
			if (destination != NULL) {
				destination[offset++] = '%';
				destination[offset++] = '2';
				destination[offset++] = '0';
			}
			
			size += 3;
		} else {
			if (destination != NULL) {
				destination[offset++] = '%';
				destination[offset++] = HEX_DIGITS[ch >> 4];
				destination[offset++] = HEX_DIGITS[ch & 0x0F];
			}
			
			size += 3;
		}
	}
	
	if (destination != NULL) {
		destination[offset++] = '\0';
	}
	
	size += 1;
	
	return size;
	
}
