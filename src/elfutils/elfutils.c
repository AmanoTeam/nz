#include <stdlib.h>
#include <string.h>

#undef Elf_Ehdr
#define Elf_Ehdr Elf64_Ehdr

#undef Elf_Phdr
#define Elf_Phdr Elf64_Phdr

#undef Elf_Dyn
#define Elf_Dyn Elf64_Dyn

#undef elf_get_soname
#define elf_get_soname elf64_get_soname

#include "soname.h"

#undef Elf_Ehdr
#define Elf_Ehdr Elf32_Ehdr

#undef Elf_Phdr
#define Elf_Phdr Elf32_Phdr

#undef Elf_Dyn
#define Elf_Dyn Elf32_Dyn

#undef elf_get_soname
#define elf_get_soname elf32_get_soname

#include "soname.h"

#undef elf_get_soname

#include "elf.h"
#include "fs/fstream.h"
#include "elfutils/elfutils.h"

void elfutils_free(elfutils_t* const data) {
	
	free(data->data);
	memset(data, 0, sizeof(*data));
	
}

int elf_load_file(elfutils_t* const data, const char* const name) {
	
	fstream_t* stream = NULL;
	char* buffer = NULL;
	
	long int file_size = 0;
	ssize_t rsize = 0;
	
	int status = 0;
	int err = 0;
	
	Elf64_Ehdr* ehdr = NULL;
	
	stream = fstream_open(name, FSTREAM_READ);
	
	if (stream == NULL) {
		err = -1;
		goto end;
	}
	
	file_size = fsream_size(stream);
	
	if (file_size == FSTREAM_ERROR) {
		err = -1;
		goto end;
	}
	
	buffer = malloc((size_t) file_size);
	
	if (buffer == NULL) {
		err = -1;
		goto end;
	}
	
	rsize = fstream_read(stream, buffer, (size_t) file_size);
	
	if (rsize == FSTREAM_ERROR) {
		err = -1;
		goto end;
	}
	
	data->data = (unsigned char*) buffer;
	data->size = (size_t) rsize;
	
	ehdr = (Elf64_Ehdr*) data->data;
	
	if (data->size < sizeof(Elf64_Ehdr) || memcmp(ehdr->e_ident, ELFMAG, SELFMAG) != 0) {
		err = -1;
		goto end;
	}
	
	data->class = ehdr->e_ident[EI_CLASS];
	
	end:;
	
	if (err != 0) {
		elfutils_free(data);
	}
	
	fstream_close(stream);
	
	return err;
	
}

const char* elfutils_get_soname(elfutils_t* const data) {
	
	switch (data->class) {
		case ELFCLASS64:
			return elf64_get_soname((Elf64_Ehdr*) data->data);
		case ELFCLASS32:
			return elf32_get_soname((Elf32_Ehdr*) data->data);
	}
	
	return NULL;
	
}

	