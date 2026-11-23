#include <stdlib.h>
#include <string.h>

char* strsub(const char* pattern, const char* replacement, const char* string) {
	
	const char* cursor = NULL;
	const char* match = NULL;
	
	char* value = NULL;
	char* output = NULL;
	
	size_t plen = 0;
	size_t rlen = 0;
	size_t slen = 0;
	size_t count = 0;
	size_t prefix = 0;
	size_t size = 0;
	
	plen = strlen(pattern);
	rlen = strlen(replacement);
	
	if (plen == 0) {
		return strdup(string);
	}
	
	if (strcmp(pattern, replacement) == 0) {
		return strdup(string);
	}
	
	slen = strlen(string);
	
	cursor = string;
	
	while ((match = strstr(cursor, pattern)) != NULL) {
		count++;
		cursor = match + plen;
	}
	
	if (count == 0) {
		return strdup(string);
	}
	
	size = (slen - (count * plen)) + (count * rlen) + 1;
	value = malloc(size);
	
	if (value == NULL) {
		return NULL;
	}
	
	cursor = string;
	output = value;
	
	while ((match = strstr(cursor, pattern)) != NULL) {
		prefix = (size_t) (match - cursor);
		
		memcpy(output, cursor, prefix);
		output += prefix;
		
		memcpy(output, replacement, rlen);
		output += rlen;
		
		cursor = match + plen;
	}
	
	memcpy(output, cursor, strlen(cursor) + 1);
	
	return value;
	
}
