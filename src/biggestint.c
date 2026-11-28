#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <limits.h>

#include "biggestint.h"

#define NUMERATOR 1233
#define SHIFT 12

#if !defined(ULLONG_WIDTH)
	#define ULLONG_WIDTH 64
#endif

static const unsigned long long POWERS[] = {
	1ULL,
	10ULL,
	100ULL,
	1000ULL,
	10000ULL,
	100000ULL,
	1000000ULL,
	10000000ULL,
	100000000ULL,
	1000000000ULL,
	10000000000ULL,
	100000000000ULL,
	1000000000000ULL,
	10000000000000ULL,
	100000000000000ULL,
	1000000000000000ULL,
	10000000000000000ULL,
	100000000000000000ULL,
	1000000000000000000ULL,
	10000000000000000000ULL
};

static size_t __uintlen(const unsigned long long value) {
	
	const unsigned long long val = value + (value == 0);
	const unsigned int bits = ((unsigned int) ULLONG_WIDTH) - ((unsigned int) __builtin_clzll(val));
	const unsigned int index = (bits * NUMERATOR) >> SHIFT;
	
	return ((size_t) index) + ((size_t) (val >= POWERS[index]));
	
}

size_t intlen(const bigint_t value) {
	/*
	Calculates the number of digits required to represent this
	integer as a string.
	*/
	
	const int negative = (value < 0);
	unsigned long long magnitude = (unsigned long long) value;
	
	if (negative) {
		magnitude = 0ULL - magnitude;
	}
	
	return __uintlen(magnitude) + ((size_t) negative);
	
}

char* int_stringify(const bigint_t value) {
	
	char buffer[sizeof(bigint_t) * 3 + 3];
	char* string = NULL;
	
	const int wsize = snprintf(
		buffer,
		sizeof(buffer),
		"%"FORMAT_BIGGEST_INT_T,
		value
	);
	
	if (wsize < 0 || ((size_t) wsize) >= sizeof(buffer)) {
		return NULL;
	}
	
	string = malloc(((size_t) wsize) + 1);
	
	if (string == NULL) {
		return string;
	}
	
	memcpy(string, buffer, ((size_t) wsize) + 1);
	
	return string;
	
}

size_t uintlen(const biguint_t value) {
	/*
	Calculates the number of digits required to represent this
	unsigned integer as a string.
	*/
	
	return __uintlen((unsigned long long) value);
	
}

char* uint_stringify(const biguint_t value) {
	
	char buffer[sizeof(biguint_t) * 3 + 3];
	char* string = NULL;
	
	const int wsize = snprintf(
		buffer,
		sizeof(buffer),
		"%"FORMAT_BIGGEST_UINT_T,
		value
	);
	
	if (wsize < 0 || ((size_t) wsize) >= sizeof(buffer)) {
		return NULL;
	}
	
	string = malloc(((size_t) wsize) + 1);
	
	if (string == NULL) {
		return string;
	}
	
	memcpy(string, buffer, ((size_t) wsize) + 1);
	
	return string;
	
}

size_t intptrlen(const intptr_t value) {
	/*
	Calculates the number of digits required to represent this
	integer as a string.
	*/
	
	return intlen((bigint_t) value);
	
}

size_t uintptrlen(const uintptr_t value) {
	/*
	Calculates the number of digits required to represent this
	unsigned integer as a string.
	*/
	
	return uintlen((biguint_t) value);
	
}
