#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "strsplit.h"

void strsplit_init(
	strsplit_t* const strsplit,
	strsplit_part_t* const part,
	const char* const string,
	const char* const sep
) {
	
	memset(strsplit, 0, sizeof(*strsplit));
	memset(part, 0, sizeof(*part));
	
	if (string == NULL || sep == NULL) {
		return;
	}
	
	strsplit->sstart = string;
	strsplit->send = strchr(string, '\0');
	
	strsplit->sep = sep;
	strsplit->sep_size = strlen(sep);
	strsplit->pbegin = string;
	
	strsplit->pend = strstr(string, sep);
	strsplit->eof = 0;
	
}

const strsplit_part_t* strsplit_next(
	strsplit_t* const strsplit,
	strsplit_part_t* const part
) {
	
	const char* pbegin = NULL;
	const char* pend = NULL;
	
	const char* start = NULL;
	const char* end = NULL;
	
	size_t part_size = 0;
	
	if (strsplit->sstart == NULL || strsplit->sep == NULL) {
		return NULL;
	}
	
	if (strsplit->eof) {
		return NULL;
	}
	
	if (*strsplit->sep == '\0') {
		strsplit->eof = 1;
		return NULL;
	}
	
	if (strsplit->pend == NULL) {
		strsplit->pend = strsplit->send;
	}
	
	pbegin = strsplit->pbegin;
	pend = strsplit->pend;
	part_size = (size_t) (pend - pbegin);
	
	start = pbegin;
	end = pend;
	
	part->begin = (char*) start;
	part->size = part_size;
	
	if (part_size > 0) {
		while (start != end && isspace((unsigned char) *start)) {
			start++;
		}
		
		while (end != start && isspace((unsigned char) end[-1])) {
			end--;
		}
		
		if (start == end) {
			part->begin = NULL;
			part->size = 0;
		} else {
			part->begin = (char*) start;
			part->size = (size_t) (end - start);
		}
	}
	
	strsplit->eof = (pend == strsplit->send);
	
	if (strsplit->eof && part_size < 1) {
		return NULL;
	}
	
	if (pbegin != strsplit->sstart) {
		part->index++;
	}
	
	strsplit->cur_pbegin = pbegin;
	strsplit->cur_pend = pend;
	
	if (strsplit->eof) {
		strsplit->pbegin = NULL;
		strsplit->pend = NULL;
	} else {
		strsplit->pbegin = pend + strsplit->sep_size;
		strsplit->pend = strstr(strsplit->pbegin, strsplit->sep);
	}
	
	return part;
	
}

int strsplit_resize(
	const strsplit_t* const strsplit,
	strsplit_part_t* const part
) {
	
	if (part->begin == NULL || strsplit->cur_pend == NULL) {
		return -1;
	}
	
	if (part->begin > strsplit->cur_pend) {
		return -1;
	}
	
	if (part->begin + part->size > strsplit->cur_pend) {
		part->size = (size_t) (strsplit->cur_pend - part->begin);
	}
	
	return 0;
	
}

size_t strsplit_size(
	strsplit_t* const strsplit,
	strsplit_part_t* const part
) {
	
	strsplit_t split = {0};
	strsplit_part_t p = {0};
	size_t size = 0;
	
	(void) part;
	
	strsplit_init(&split, &p, strsplit->sstart, strsplit->sep);
	
	while (strsplit_next(&split, &p) != NULL) {
		size++;
	}
	
	return size;
	
}

char* strsplit_findstr(strsplit_part_t* const part, const char* const pattern) {
	
	if (part->begin == NULL) {
		return NULL;
	}
	
	return memmem(part->begin, part->size, pattern, strlen(pattern));
	
}
