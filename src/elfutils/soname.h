#include <stdlib.h>

#include "elf.h"

const char* elf_get_soname(const Elf_Ehdr* const ehdr) {
	
	const Elf_Phdr* phdr = NULL;
	unsigned int phnum = 0;
	
	size_t index = 0;
	size_t size = 0;
	
	size_t begin = 0;
	size_t end = 0;
	
	const Elf_Dyn* dyn = NULL;
	
	const unsigned char* dynamic = NULL;
	size_t dynamic_off = SIZE_MAX;
	size_t dynamic_size = SIZE_MAX;
	
	size_t strtab_vaddr = SIZE_MAX;
	size_t strtab_size = SIZE_MAX;
	size_t strtab_off = SIZE_MAX;
	
	size_t soname_off = SIZE_MAX;
	
	const char* soname = NULL;
	const char* base = (char*) ehdr;
	
	if (ehdr->e_phentsize != sizeof(*phdr)) {
		return soname;
	}
	
	phdr = (Elf_Phdr*) (base + ehdr->e_phoff);
	phnum = ehdr->e_phnum;
	
	for (index = 0; index < phnum; index++) {
		if (phdr[index].p_type != PT_DYNAMIC) {
			continue;
		}
		
		dynamic_off = phdr[index].p_offset;
		dynamic_size = phdr[index].p_filesz;
		
		break;
	}
	
	if (dynamic_off == SIZE_MAX) {
		return soname;
	}
	
	dynamic = (unsigned char*) (base + dynamic_off);
	
	dyn = (Elf_Dyn*) dynamic;
	size = dynamic_size / sizeof(*dyn);
	
	for (index = 0; index < size; index++) {
		switch (dyn[index].d_tag) {
			case DT_NULL:
				index = size;
				break;
			case DT_STRTAB:
				strtab_vaddr = (size_t) dyn[index].d_un.d_ptr;
				break;
			case DT_STRSZ:
				strtab_size = (size_t) dyn[index].d_un.d_val;
				break;
			case DT_SONAME:
				soname_off = (size_t) dyn[index].d_un.d_val;
				break;
		}
	}
	
	if (strtab_vaddr == SIZE_MAX || strtab_size == SIZE_MAX) {
		return soname;
	}
	
	if (soname_off == SIZE_MAX || soname_off >= strtab_size) {
		return soname;
	}
	
	for (index = 0; index < phnum; index++) {
		if (phdr[index].p_type != PT_LOAD) {
			continue;
		}
		
		begin = phdr[index].p_vaddr;
		end   = phdr[index].p_vaddr + phdr[index].p_memsz;
		
		if (!(strtab_vaddr >= begin && strtab_vaddr < end)) {
			continue;
		}
		
		strtab_off = strtab_vaddr - begin + phdr[index].p_offset;
		
		break;
	}
	
	if (strtab_off == SIZE_MAX) {
		return soname;
	}
	
	soname = (const char*) (base + strtab_off + soname_off);
	
	return soname;
	
}