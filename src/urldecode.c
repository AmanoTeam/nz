#include <stdlib.h>
#include <ctype.h>
#include <string.h>

#include "urldecode.h"

size_t urldecode(
	const char* const uri,
	char* const destination
) {
	
	size_t index = 0;
	size_t offset = 0;
	
	unsigned char a = 0;
	unsigned char b = 0;
	unsigned char c = 0;
	unsigned char d = 0;
	
	for (index = 0; (a = (unsigned char) uri[index]) != '\0'; index++) {
		if (a == '%' && uri[index + 1] != '\0' && uri[index + 2] != '\0') {
			b = (unsigned char) uri[index + 1];
			c = (unsigned char) uri[index + 2];
			
			if (isxdigit(b) && isxdigit(c)) {
				d = (unsigned char) (
					(((b & 0x0F) + ((b >> 6) * 9)) << 4) |
					((c & 0x0F) + ((c >> 6) * 9))
				);
				
				if (d != '\0') {
					if (destination != NULL) {
						destination[offset] = d;
					}
					
					offset++;
					index += 2;
					
					continue;
				}
			}
		}
		
		if (destination != NULL) {
			destination[offset] = a;
		}
		
		offset++;
	}
	
	if (destination != NULL) {
		destination[offset] = '\0';
	}
	
	offset++;
	
	return offset;
	
}
