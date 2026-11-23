#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <stdio.h>
#include <errno.h>
#include <ctype.h>

#include "query.h"
#include "biggestint.h"
#include "urlencode.h"
#include "urldecode.h"
#include "strsplit.h"
#include "fs/fstream.h"

#define QUERY_MAX_SIZE ((1024 * 1024) * 16)

static const char AND = '&';
static const char EQUAL[] = "=";

static const char* const BOOL_VAL_TRUE[] = {
	"TRUE",
	"True",
	"true",
	"YES",
	"Yes",
	"yes",
	"1",
};

static const char* const BOOL_VAL_FALSE[] = {
	"FALSE",
	"False",
	"false",
	"NO",
	"No",
	"no",
	"0"
};

extern char** environ;

enum hquery_num {
	HQUERY_INT,
	HQUERY_UINT,
	HQUERY_FLOAT
};

static int put_parameter(
	hquery_t* const query,
	hquery_param_t* const parameter
) {
	
	size_t size = 0;
	hquery_param_t* parameters = NULL;
	
	if (sizeof(*query->parameters) * (query->offset + 1) > query->size) {
		size = query->size + sizeof(*query->parameters) * (query->offset + 1);
		parameters = realloc(query->parameters, size);
		
		if (parameters == NULL) {
			return -1;
		}
		
		query->size = size;
		query->parameters = parameters;
	}
	
	query->parameters[query->offset++] = *parameter;
	
	return 0;
	
}

hquery_param_t* query_get_param(
	hquery_t* const query,
	const char* const key
) {
	
	int status = 0;
	
	size_t index = 0;
	
	hquery_param_t* parameter = NULL;
	
	if (key == NULL) {
		return NULL;
	}
	
	for (index = 0; index < query->offset; index++) {
		parameter = &query->parameters[index];
		
		if (query->options & HQUERY_OPT_IGNORE_CASE) {
			status = strcasecmp(parameter->key, key);
		} else {
			status = strcmp(parameter->key, key);
		}
		
		if (status != 0) {
			continue;
		}
		
		return parameter;
	}
	
	return NULL;
	
}

hquery_param_t* query_get_item(
	hquery_t* const query,
	const size_t index
) {
	
	hquery_param_t* parameter = NULL;
	
	if (index >= query->offset) {
		return parameter;
	}
	
	parameter = &query->parameters[index];
	
	return parameter;
	
}

char* query_get_string(
	hquery_t* const query,
	const char* const key
) {
	
	const hquery_param_t* const parameter = query_get_param(query, key);
	
	if (parameter == NULL || parameter->value == NULL) {
		return NULL;
	}
	
	return parameter->value;
	
}

static int get_numeric_value(
	const enum hquery_num type,
	const char* const source,
	bigint_storage_t* destination
) {
	
	int err = 0;
	
	char* end = NULL;
	
	bigint_t a = 0;
	biguint_t b = 0;
	bigfloat_t c = 0;
	
	switch (type) {
		case HQUERY_INT: {
			errno = 0;
			a = strtobi(source, &end, 10);
			
			if (errno == ERANGE || end == source) {
				err = -1;
				break;
			}
			
			memcpy(destination, &a, sizeof(a));
			
			break;
		}
		case HQUERY_UINT: {
			errno = 0;
			b = strtobui(source, &end, 10);
			
			if (errno == ERANGE || end == source) {
				err = -1;
				break;
			}
			
			memcpy(destination, &b, sizeof(b));
			
			break;
		}
		case HQUERY_FLOAT: {
			errno = 0;
			c = strtobf(source, &end);
			
			if (errno == ERANGE || end == source) {
				err = -1;
				break;
			}
			
			memcpy(destination, &c, sizeof(c));
			
			break;
		}
		default:
			err = -1;
			break;
	}
	
	return err;
	
}

static int query_get_numeric(
	hquery_t* const query,
	const enum hquery_num type,
	const char* const key,
	bigint_storage_t* destination
) {
	
	int err = 0;
	
	const char* const source = query_get_string(query, key);
	
	if (source == NULL) {
		return -1;
	}
	
	err = get_numeric_value(type, source, destination);
	
	return err;
	
}

const char* param_get_string(const hquery_param_t* const param) {
	return param->value;
}

int param_get_bool(const hquery_param_t* const param) {
	
	size_t index = 0;
	const char* value = NULL;
	
	for (index = 0; index < sizeof(BOOL_VAL_TRUE) / sizeof(*BOOL_VAL_TRUE); index++) {
		value = BOOL_VAL_TRUE[index];
		
		if (strcmp(param->value, value) != 0) {
			continue;
		}
		
		return 1;
	}
	
	
	for (index = 0; index < sizeof(BOOL_VAL_FALSE) / sizeof(*BOOL_VAL_FALSE); index++) {
		value = BOOL_VAL_FALSE[index];
		
		if (strcmp(param->value, value) != 0) {
			continue;
		}
		
		return 0;
	}
	
	return -1;
	
}

int query_get_bool(
	hquery_t* const query,
	const char* const key
) {
	
	const hquery_param_t* const parameter = query_get_param(query, key);
	
	if (parameter == NULL || parameter->value == NULL) {
		return -1;
	}
	
	return param_get_bool(parameter);
	
}

bigint_t param_get_int(const hquery_param_t* const param) {
	
	bigint_storage_t value = {0};
	const int err = get_numeric_value(HQUERY_INT, param->value, &value);
	
	if (err != 0) {
		return BIGINT_MAX;
	}
	
	return *(bigint_t*) &value;
	
}

bigint_t query_get_int(
	hquery_t* const query,
	const char* const key
) {
	
	bigint_storage_t value = {0};
	const int err = query_get_numeric(query, HQUERY_INT, key, &value);
	
	if (err != 0) {
		return BIGINT_MAX;
	}
	
	return *(bigint_t*) &value;
	
}

biguint_t param_get_uint(const hquery_param_t* const param) {
	
	bigint_storage_t value = {0};
	const int err = get_numeric_value(HQUERY_UINT, param->value, &value);
	
	if (err != 0) {
		return BIGUINT_MAX;
	}
	
	return *(biguint_t*) &value;
	
}

biguint_t query_get_uint(
	hquery_t* const query,
	const char* const key
) {
	
	bigint_storage_t value = {0};
	const int err = query_get_numeric(query, HQUERY_UINT, key, &value);
	
	if (err != 0) {
		return BIGUINT_MAX;
	}
	
	return *(biguint_t*) &value;
	
}

bigfloat_t param_get_float(const hquery_param_t* const param) {
	
	bigint_storage_t value = {0};
	const int err = get_numeric_value(HQUERY_FLOAT, param->value, &value);
	
	if (err != 0) {
		return BIGFLOAT_MAX;
	}
	
	return *(bigfloat_t*) &value;
	
}

bigfloat_t query_get_float(
	hquery_t* const query,
	const char* const key
) {
	
	bigint_storage_t value = {0};
	const int err = query_get_numeric(query, HQUERY_FLOAT, key, &value);
	
	if (err != 0) {
		return BIGFLOAT_MAX;
	}
	
	return *(bigfloat_t*) &value;
	
}

int query_add_string(
	hquery_t* const query,
	const char* const key,
	const char* const value
) {
	
	int err = 0;
	
	char* replacement = NULL;
	
	hquery_param_t* param = NULL;
	hquery_param_t parameter = {0};
	
	if (!(query->options & HQUERY_OPT_DUPLICATES)) {
		param = query_get_param(query, key);
		
		if (param != NULL) {
			if (strcmp(param->value, value) == 0) {
				goto end;
			}
			
			replacement = strdup(value);
			
			if (replacement == NULL) {
				err = -1;
				goto end;
			}
			
			free(param->value);
			param->value = replacement;
			replacement = NULL;
			
			goto end;
		}
	}
	
	parameter.key = strdup(key);
	
	if (parameter.key == NULL) {
		err = -1;
		goto end;
	}
	
	parameter.value = strdup(value);
	
	if (parameter.value == NULL) {
		err = -1;
		goto end;
	}
	
	err = put_parameter(query, &parameter);
	
	end:;
	
	free(replacement);
	
	if (err != 0) {
		param_free(&parameter);
	}
	
	return err;
	
}

int query_add_buffer(
	hquery_t* const query,
	const char* const key,
	const size_t key_size,
	const char* const value,
	const size_t value_size
) {
	
	char* a = NULL;
	char* b = NULL;
	
	char* replacement = NULL;
	
	hquery_param_t* param = NULL;
	hquery_param_t parameter = {0};
	
	int err = 0;
	
	a = malloc(key_size + 1);
	
	if (a == NULL) {
		err = -1;
		goto end;
	}
	
	memcpy(a, key, key_size);
	a[key_size] = '\0';
	
	b = malloc(value_size + 1);
	
	if (b == NULL) {
		err = -1;
		goto end;
	}
	
	memcpy(b, value, value_size);
	b[value_size] = '\0';
	
	if (!(query->options & HQUERY_OPT_DUPLICATES)) {
		param = query_get_param(query, a);
		
		if (param != NULL) {
			if (strcmp(param->value, b) == 0) {
				goto end;
			}
			
			replacement = b;
			free(param->value);
			param->value = replacement;
			replacement = NULL;
			
			b = NULL;
			
			goto end;
		}
	}
	
	parameter.key = a;
	parameter.value = b;
	
	a = NULL;
	b = NULL;
	
	err = put_parameter(query, &parameter);
	
	end:;
	
	free(replacement);
	
	free(a);
	free(b);
	
	if (err != 0) {
		param_free(&parameter);
	}
	
	return err;
	
}

static int query_add_numeric(
	hquery_t* const query,
	const enum hquery_num type,
	const char* const key,
	const void* const value
) {
	
	int err = 0;
	int wsize = 0;
	
	char* wtmp = NULL;
	
	bigint_t a = 0;
	biguint_t b = 0;
	bigfloat_t c = 0;
	
	switch (type) {
		case HQUERY_INT: {
			a = *(const bigint_t*) value;
			
			wsize = snprintf(NULL, 0, "%"FORMAT_BIGGEST_INT_T, a);
			
			if (wsize < 1) {
				err = -1;
				goto end;
			}
			
			wtmp = malloc((size_t) wsize + 1);
			
			if (wtmp == NULL) {
				err = -1;
				goto end;
			}
			
			snprintf(wtmp, (size_t) wsize + 1, "%"FORMAT_BIGGEST_INT_T, a);
			
			break;
		}
		case HQUERY_UINT: {
			b = *(const biguint_t*) value;
			
			wsize = snprintf(NULL, 0, "%"FORMAT_BIGGEST_UINT_T, b);
			
			if (wsize < 1) {
				err = -1;
				goto end;
			}
			
			wtmp = malloc((size_t) wsize + 1);
			
			if (wtmp == NULL) {
				err = -1;
				goto end;
			}
			
			snprintf(wtmp, (size_t) wsize + 1, "%"FORMAT_BIGGEST_UINT_T, b);
			
			break;
		}
		case HQUERY_FLOAT: {
			c = *(const bigfloat_t*) value;
			
			wsize = snprintf(NULL, 0, "%"FORMAT_BIGGEST_FLOAT_T, c);
			
			if (wsize < 1) {
				err = -1;
				goto end;
			}
			
			wtmp = malloc((size_t) wsize + 1);
			
			if (wtmp == NULL) {
				err = -1;
				goto end;
			}
			
			snprintf(wtmp, (size_t) wsize + 1, "%"FORMAT_BIGGEST_FLOAT_T, c);
			
			break;
		}
		default:
			err = -1;
			goto end;
	}
	
	err = query_add_string(query, key, wtmp);
	
	end:;
	
	free(wtmp);
	
	return err;
	
}

int query_add_int(
	hquery_t* const query,
	const char* const key,
	const bigint_t value
) {
	
	const int err = query_add_numeric(query, HQUERY_INT, key, &value);
	return err;
	
}

int query_add_uint(
	hquery_t* const query,
	const char* const key,
	const biguint_t value
) {
	
	const int err = query_add_numeric(query, HQUERY_UINT, key, &value);
	return err;
	
}

int query_add_float(
	hquery_t* const query,
	const char* const key,
	const bigfloat_t value
) {
	
	const int err = query_add_numeric(query, HQUERY_FLOAT, key, &value);
	return err;
	
}

size_t query_dump_string(
	const hquery_t* const query,
	char* const destination
) {
	
	size_t size = 1;
	size_t offset = 0;
	size_t index = 0;
	
	size_t key_size = 0;
	size_t value_size = 0;
	
	const int status = query->options & HQUERY_OPT_URL_ENCODE;
	
	const hquery_param_t* parameter = NULL;
	
	const size_t subsep_size = strlen(query->subsep);
	
	for (index = 0; index < query->offset; index++) {
		parameter = &query->parameters[index];
		
		if (index != 0 && query->sep != '\0') {
			if (destination != NULL) {
				destination[offset] = query->sep;
			}
			
			offset++;
			size++;
		}
		
		if (parameter->key != NULL) {
			if (status) {
				key_size = urlencode(parameter->key, (destination == NULL) ? (destination) : (destination + offset)) - 1;
			} else {
				key_size = strlen(parameter->key);
				
				if (destination != NULL) {
					memcpy(destination + offset, parameter->key, key_size);
				}
			}
			
			offset += key_size;
			size += key_size;
		}
		
		if (destination != NULL) {
			if (subsep_size == 1) {
				destination[offset] = query->subsep[0];
			} else {
				memcpy(destination + offset, query->subsep, subsep_size);
			}
		}
		
		offset += subsep_size;
		size += subsep_size;
		
		if (parameter->value != NULL) {
			if (status) {
				value_size = urlencode(parameter->value, (destination == NULL) ? (destination) : (destination + offset)) - 1;
			} else {
				value_size = strlen(parameter->value);
				
				if (destination != NULL) {
					memcpy(destination + offset, parameter->value, value_size);
				}
			}
			
			offset += value_size;
			size += value_size;
		}
	}
	
	if (destination != NULL) {
		destination[offset] = '\0';
	}
	
	return size;
	
}

void param_free(hquery_param_t* const parameter) {
	
	free(parameter->key);
	free(parameter->value);
	
	memset(parameter, 0, sizeof(*parameter));
	
}

void query_free(hquery_t* const query) {
	
	size_t index = 0;
	
	hquery_param_t* parameter = NULL;
	
	for (index = 0; index < query->offset; index++) {
		parameter = &query->parameters[index];
		param_free(parameter);
	}
	
	free(query->parameters);
	
	memset(query, 0, sizeof(*query));
	
}

int query_load_string(
	hquery_t* const query,
	const char* const string
) {
	
	int err = 0;
	
	strsplit_t split = {0};
	strsplit_part_t part = {0};
	
	hquery_param_t param = {0};
	
	const char* key_begin = NULL;
	const char* key_end = NULL;
	
	const char* value_begin = NULL;
	const char* value_end = NULL;
	
	const char* sub = NULL;
	
	size_t key_size = 0;
	size_t value_size = 0;
	
	const size_t subsep_size = strlen(query->subsep);
	
	char sep[2];
	
	sep[0] = query->sep;
	sep[1] = '\0';
	
	strsplit_init(&split, &part, string, sep);
	
	while (strsplit_next(&split, &part) != NULL) {
		if (part.size == 0) {
			continue;
		}
		
		key_begin = part.begin;
		key_end = part.begin + part.size;
		
		if (subsep_size == 0) {
			continue;
		}
		
		if (subsep_size == 1) {
			sub = memchr(part.begin, (unsigned char) query->subsep[0], part.size);
		} else {
			sub = memmem(part.begin, part.size, query->subsep, subsep_size);
		}
		
		if (sub == NULL) {
			continue;
		}
		
		key_end = sub;
		value_begin = sub + subsep_size;
		value_end = part.begin + part.size;
		
		while (key_begin < key_end && isspace((unsigned char) *key_begin)) {
			key_begin++;
		}
		
		while (key_end > key_begin && isspace((unsigned char) key_end[-1])) {
			key_end--;
		}
		
		if (key_begin == key_end) {
			continue;
		}
		
		while (value_begin < value_end && isspace((unsigned char) *value_begin)) {
			value_begin++;
		}
		
		while (value_end > value_begin && isspace((unsigned char) value_end[-1])) {
			value_end--;
		}
		
		key_size = (size_t) (key_end - key_begin);
		value_size = (size_t) (value_end - value_begin);
		
		param.key = malloc(key_size + 1);
		
		if (param.key == NULL) {
			err = -1;
			break;
		}
		
		memcpy(param.key, key_begin, key_size);
		param.key[key_size] = '\0';
		
		param.value = malloc(value_size + 1);
		
		if (param.value == NULL) {
			param_free(&param);
			err = -1;
			break;
		}
		
		memcpy(param.value, value_begin, value_size);
		param.value[value_size] = '\0';
		
		if (query->options & HQUERY_OPT_URL_DECODE) {
			urldecode(param.key, param.key);
			urldecode(param.value, param.value);
		}
		
		err = put_parameter(query, &param);
		
		if (err != 0) {
			param_free(&param);
			break;
		}
	}
	
	if (err != 0) {
		query_free(query);
	}
	
	return err;
	
}

int query_load_environ(hquery_t* const query) {
	
	int err = 0;
	
	size_t index = 0;
	size_t key_size = 0;
	
	const char* item = NULL;
	const char* equal = NULL;
	
	char* key = NULL;
	char* value = NULL;
	
	query_init(query, 0, NULL);
	
	if (environ == NULL) {
		err = -1;
		goto end;
	}
	
	query->options &= ~(HQUERY_OPT_URL_DECODE|HQUERY_OPT_IGNORE_CASE);
	
	for (index = 0; environ[index] != NULL; index++) {
		item = environ[index];
		
		equal = strchr(item, '=');
		
		if (equal == NULL) {
			continue;
		}
		
		key_size = (size_t) (equal - item);
		
		key = malloc(key_size + 1);
		
		if (key == NULL) {
			err = -1;
			goto end;
		}
		
		memcpy(key, item, key_size);
		key[key_size] = '\0';
		
		value = strdup(equal + 1);
		
		if (value == NULL) {
			err = -1;
			goto end;
		}
		
		err = query_add_string(query, key, value);
		
		free(key);
		key = NULL;
		
		free(value);
		value = NULL;
		
		if (err != 0) {
			goto end;
		}
	}
	
	end:;
	
	free(key);
	free(value);
	
	if (err != 0) {
		query_free(query);
	}
	
	return err;
	
}

void query_init(
	hquery_t* const query,
	const char sep,
	const char* subsep
) {
	
	query_free(query);
	
	query->sep = sep;
	
	if (query->sep == '\0') {
		query->sep = AND;
	}
	
	query->subsep = EQUAL;
	
	if (subsep != NULL) {
		query->subsep = subsep;
	}
	
	query->options |= (HQUERY_OPT_URL_ENCODE|HQUERY_OPT_URL_DECODE);
	
}

int query_load_file(
	hquery_t* const query,
	const char* const filename
) {
	
	int err = 0;
	
	fstream_t* stream = NULL;
	char* buffer = NULL;
	
	int64_t file_size = 0;
	ssize_t rsize = 0;
	
	stream = fstream_open(filename, FSTREAM_READ);
	
	if (stream == NULL) {
		err = -1;
		goto end;
	}
	
	file_size = fstream_size(stream);
	
	if (file_size < 1 || file_size > QUERY_MAX_SIZE) {
		err = -1;
		goto end;
	}
	
	buffer = malloc((size_t) (file_size + 1));
	
	if (buffer == NULL) {
		err = -1;
		goto end;
	}
	
	rsize = fstream_read(stream, buffer, (size_t) file_size);
	
	if (rsize < 0 || rsize != (ssize_t) file_size) {
		err = -1;
		goto end;
	}
	
	buffer[(size_t) rsize] = '\0';
	
	err = query_load_string(query, buffer);
	
	if (err != 0) {
		err = -1;
		goto end;
	}
	
	end:;
	
	free(buffer);
	
	if (err != 0) {
		query_free(query);
	}
	
	fstream_close(stream);
	
	return err;
	
}

int query_dump_file(
	hquery_t* const query,
	const char* const filename
) {
	
	int err = 0;
	
	size_t size = 0;
	
	fstream_t* stream = NULL;
	char* buffer = NULL;
	
	stream = fstream_open(filename, FSTREAM_WRITE);
	
	if (stream == NULL) {
		err = -1;
		goto end;
	}
	
	size = query_dump_string(query, NULL);
	
	buffer = malloc(size);
	
	if (buffer == NULL) {
		err = -1;
		goto end;
	}
	
	query_dump_string(query, buffer);
	
	size--;
	
	err = fstream_write(stream, buffer, size);
	
	if (err != FSTREAM_SUCCESS) {
		err = -1;
		goto end;
	}
	
	end:;
	
	fstream_close(stream);
	free(buffer);
	
	return err;
	
}