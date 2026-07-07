struct elfutils {
	int class;
	unsigned char* data;
	size_t size;
};

typedef struct elfutils elfutils_t;

const char* elfutils_get_soname(elfutils_t* const data);

int elf_load_file(elfutils_t* const data, const char* const name);
void elfutils_free(elfutils_t* const data);
