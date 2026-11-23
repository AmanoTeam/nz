#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>

#if defined(_WIN32)
	#include <windows.h>
	#include <fileapi.h>
	#include <fcntl.h>
	#include <wchar.h>
#endif

#if !defined(_WIN32)
	#include <stdio.h>
	#include <fcntl.h>
	#include <unistd.h>
	#include <sys/file.h>
#endif

#include "fs/fstream.h"

#if defined(_WIN32) && defined(_UNICODE)
	#include "fs/absrel.h"
	#include "fs/sep.h"
#endif

#if defined(__ANDROID__) && !defined(__LP64__) && __ANDROID_API__ < 24
	#define fseeko fseek
	#define ftello ftell
#endif

#if defined(_WIN32)
	static DWORD* fstream_getmode(const fstream_mode_t mode, DWORD flags[2]) {
		
		DWORD* access = &flags[0];
		DWORD* disposition = &flags[1];
		
		switch (mode) {
			case FSTREAM_WRITE:
				(*access) |= GENERIC_WRITE;
				(*disposition) |= CREATE_ALWAYS;
				break;
			case FSTREAM_READ:
				(*access) |= GENERIC_READ;
				(*disposition) |= OPEN_EXISTING;
				break;
			case FSTREAM_APPEND:
				(*access) |= FILE_APPEND_DATA | GENERIC_WRITE;
				(*disposition) |= OPEN_ALWAYS;
				break;
			case FSTREAM_TRUNCATE:
				(*access) |= GENERIC_READ | GENERIC_WRITE;
				(*disposition) |= OPEN_EXISTING;
				break;
			default:
				return NULL;
		}
		
		return flags;
	}
#else
	static const char* fstream_getmode(const fstream_mode_t mode) {
		
		const char* flags = NULL;
		
		switch (mode) {
			case FSTREAM_WRITE:
				flags = "wb";
				break;
			case FSTREAM_READ:
				flags = "rb";
				break;
			case FSTREAM_APPEND:
				flags = "ab";
				break;
			case FSTREAM_TRUNCATE:
				flags = "r+b";
				break;
			default:
				flags = NULL;
				break;
		}
		
		return flags;
		
	}
#endif

fstream_t* fstream_open(const char* const filename, const fstream_mode_t mode) {
	/*
	Opens a file on disk.
	
	Returns a null pointer on error.
	*/
	
	fstream_t* stream = NULL;
	
	#if defined(_WIN32)
		#if defined(_UNICODE)
			size_t prefixs = 0;
			wchar_t* wfilename = NULL;
			int wfilenames = 0;
		#endif
		
		DWORD values[2] = {0, 0};
		
		DWORD access = 0;
		DWORD disposition = 0;
		
		const DWORD share = FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE;
		const DWORD flags = FILE_ATTRIBUTE_NORMAL;
		
		HANDLE handle = INVALID_HANDLE_VALUE;
		
		int fd = -1;
		int fdflags = 0;
		
		if (fstream_getmode(mode, values) == NULL) {
			goto end;
		}
		
		access = values[0];
		disposition = values[1];
		
		#if defined(_UNICODE)
			/* This prefix is required to support long paths in Windows 10+ */
			prefixs = (isabsolute(filename) && filename[0] != PATHSEP) ? wcslen(WIN10_LONG_PATH_PREFIX) : 0;
			
			wfilenames = MultiByteToWideChar(CP_UTF8, 0, filename, -1, NULL, 0);
			
			if (wfilenames == 0) {
				goto end;
			}
			
			wfilename = malloc((prefixs + (size_t) wfilenames) * sizeof(*wfilename));
			
			if (wfilename == NULL) {
				goto end;
			}
			
			if (prefixs > 0) {
				wcscpy(wfilename, WIN10_LONG_PATH_PREFIX);
			}
			
			if (MultiByteToWideChar(CP_UTF8, 0, filename, -1, wfilename + prefixs, wfilenames) == 0) {
				goto end;
			}
			
			handle = CreateFileW(wfilename, access, share, NULL, disposition, flags, NULL);
		#else
			handle = CreateFileA(filename, access, share, NULL, disposition, flags, NULL);
		#endif
		
		if (handle == INVALID_HANDLE_VALUE) {
			goto end;
		}
		
		if (mode == FSTREAM_APPEND) {
			SetLastError(ERROR_SUCCESS);
			
			if (SetFilePointer(handle, 0, NULL, FILE_END) == INVALID_SET_FILE_POINTER && GetLastError() != ERROR_SUCCESS) {
				goto end;
			}
		}
		
		switch (mode) {
			case FSTREAM_READ:
				fdflags = _O_RDONLY | _O_BINARY | _O_NOINHERIT;
				break;
			case FSTREAM_WRITE:
				fdflags = _O_WRONLY | _O_BINARY | _O_NOINHERIT;
				break;
			case FSTREAM_APPEND:
				fdflags = _O_WRONLY | _O_APPEND | _O_BINARY | _O_NOINHERIT;
				break;
			default:
				fdflags = _O_RDWR | _O_BINARY | _O_NOINHERIT;
				break;
		}
		
		fd = _open_osfhandle((intptr_t) handle, fdflags);
		
		if (fd == -1) {
			goto end;
		}
	#else
		FILE* file = NULL;
		const char* flags = fstream_getmode(mode);
		
		int fd = -1;
		
		if (flags == NULL) {
			goto end;
		}
		
		file = fopen(filename, flags);
		
		if (file == NULL) {
			goto end;
		}
		
		fd = fileno(file);
		
		if (fd == -1) {
			goto end;
		}
		
		if (fcntl(fd, F_SETFD, FD_CLOEXEC) == -1) {
			goto end;
		}
	#endif
	
	stream = malloc(sizeof(*stream));
	
	if (stream == NULL) {
		goto end;
	}
	
	#if defined(_WIN32)
		stream->stream = handle;
		stream->fd = fd;
	#else
		stream->stream = file;
	#endif
	
	stream->mode = mode;
	
	end:;
	
	#if defined(_WIN32) && defined(_UNICODE)
		free(wfilename);
	#endif
	
	if (stream == NULL) {
		#if defined(_WIN32)
			if (fd != -1) {
				_close(fd);
			} else if (handle != INVALID_HANDLE_VALUE) {
				CloseHandle(handle);
			}
		#else
			if (file != NULL) {
				fclose(file);
			}
		#endif
	}
	
	return stream;
	
}

fstream_t* fstream_fdopen(const int fd, const fstream_mode_t mode) {
	/*
	Opens a file descriptor.
	
	Returns a null pointer on error.
	*/
	
	fstream_t* stream = NULL;
	
	int err = 0;
	
	#if defined(_WIN32)
		DWORD values[2] = {0, 0};
		HANDLE handle = 0;
	#else
		FILE* file = NULL;
		const char* flags = NULL;
	#endif
	
	stream = malloc(sizeof(*stream));
	
	if (stream == NULL) {
		err = -1;
		goto end;
	}
	
	#if defined(_WIN32)
		if (fstream_getmode(mode, values) == NULL) {
			err = -1;
			goto end;
		}
		
		handle = (HANDLE) _get_osfhandle(fd);
		
		if (handle == INVALID_HANDLE_VALUE) {
			err = -1;
			goto end;
		}
		
		stream->stream = handle;
		stream->fd = fd;
	#else
		flags = fstream_getmode(mode);
		
		if (flags == NULL) {
			err = -1;
			goto end;
		}
		
		file = fdopen(fd, flags);
		
		if (file == NULL) {
			err = -1;
			goto end;
		}
		
		stream->stream = file;
	#endif
	
	stream->mode = mode;
	
	end:;
	
	if (err != 0) {
		free(stream);
		stream = NULL;
	}
	
	return stream;
	
}

int fstream_lock(fstream_t* const stream) {
	/*
	Lock the file.
	
	Returns (0) on success, (-1) on error.
	*/
	
	int status = 0;
	
	#if defined(_WIN32)
		OVERLAPPED overlapped;
	#else
		int fd = 0;
	#endif
	
	#if defined(_WIN32)
		memset(&overlapped, 0, sizeof(overlapped));
		
		status = LockFileEx(stream->stream, LOCKFILE_EXCLUSIVE_LOCK, 0, MAXDWORD, MAXDWORD, &overlapped) == TRUE;
	#else
		fd = fileno(stream->stream);
		
		if (fd == -1) {
			return FSTREAM_ERROR;
		}
		
		status = flock(fd, LOCK_EX) == 0;
	#endif
	
	if (!status) {
		return FSTREAM_ERROR;
	}
	
	return FSTREAM_SUCCESS;
	
}

ssize_t fstream_read(fstream_t* const stream, char* const buffer, const size_t size) {
	/*
	Reads a block of data.
	
	Returns (>=1) on success, (0) on EOF, (-1) on error.
	*/
	
	#if defined(_WIN32)
		DWORD rsize = 0;
		BOOL status = FALSE;
	#endif
	
	#if !defined(_WIN32)
		size_t rsize = 0;
	#endif
	
	if (stream->mode != FSTREAM_READ && stream->mode != FSTREAM_TRUNCATE) {
		return FSTREAM_ERROR;
	}
	
	#if defined(_WIN32)
		if (size > (size_t) MAXDWORD) {
			return FSTREAM_ERROR;
		}
		
		status = ReadFile(stream->stream, buffer, (DWORD) size, &rsize, NULL);
		
		if (!status) {
			return FSTREAM_ERROR;
		}
		
		if (rsize > 0) {
			return (ssize_t) rsize;
		}
		
		return FSTREAM_EOF;
	#else
		if (stream->mode == FSTREAM_TRUNCATE) {
			if (fseeko(stream->stream, 0, SEEK_CUR) != 0) {
				return FSTREAM_ERROR;
			}
		}
		
		rsize = fread(buffer, sizeof(*buffer), size, stream->stream);
		
		if (rsize == 0) {
			if (ferror(stream->stream) != 0) {
				return FSTREAM_ERROR;
			}
			
			return FSTREAM_EOF;
		}
		
		return (ssize_t) rsize;
	#endif
	
}

int fstream_write(fstream_t* const stream, const char* const buffer, const size_t size) {
	/*
	Writes a block of data.
	
	Returns (0) on success, (-1) on error.
	*/
	
	#if defined(_WIN32)
		DWORD wsize = 0;
		BOOL status = FALSE;
		LARGE_INTEGER end = {0};
	#endif
	
	#if !defined(_WIN32)
		size_t wsize = 0;
	#endif
	
	if (stream->mode == FSTREAM_READ) {
		return FSTREAM_ERROR;
	}
	
	#if defined(_WIN32)
		if (size > (size_t) MAXDWORD) {
			return FSTREAM_ERROR;
		}
		
		if (stream->mode == FSTREAM_APPEND) {
			if (SetFilePointerEx(stream->stream, end, NULL, FILE_END) == FALSE) {
				return FSTREAM_ERROR;
			}
		}
		
		status = WriteFile(stream->stream, buffer, (DWORD) size, &wsize, NULL);
		
		if (status == 0 || wsize != (DWORD) size) {
			return FSTREAM_ERROR;
		}
	#else
		if (stream->mode == FSTREAM_TRUNCATE) {
			if (fseeko(stream->stream, 0, SEEK_CUR) != 0) {
				return FSTREAM_ERROR;
			}
		}
		
		wsize = fwrite(buffer, sizeof(*buffer), size, stream->stream);
		
		if (wsize != size) {
			return FSTREAM_ERROR;
		}
	#endif
	
	return FSTREAM_SUCCESS;
	
}

int fstream_seek(fstream_t* const stream, const int64_t offset, const fstream_seek_t method) {
	/*
	Sets the current file position.
	
	Returns (0) on success, (-1) on error.
	*/
	
	#if defined(_WIN32)
		DWORD whence = 0;
		
		LARGE_INTEGER value = {0};
		value.QuadPart = offset;
		
		switch (method) {
			case FSTREAM_SEEK_BEGIN:
				whence = FILE_BEGIN;
				break;
			case FSTREAM_SEEK_CURRENT:
				whence = FILE_CURRENT;
				break;
			case FSTREAM_SEEK_END:
				whence = FILE_END;
				break;
			default:
				return FSTREAM_ERROR;
		}
		
		if (SetFilePointerEx(stream->stream, value, NULL, whence) == FALSE) {
			return FSTREAM_ERROR;
		}
	#else
		int whence = 0;
		
		switch (method) {
			case FSTREAM_SEEK_BEGIN:
				whence = SEEK_SET;
				break;
			case FSTREAM_SEEK_CURRENT:
				whence = SEEK_CUR;
				break;
			case FSTREAM_SEEK_END:
				whence = SEEK_END;
				break;
			default:
				return FSTREAM_ERROR;
		}
		
		if (fseeko(stream->stream, offset, whence) != 0) {
			return FSTREAM_ERROR;
		}
	#endif
	
	return FSTREAM_SUCCESS;
	
}

int64_t fstream_tell(fstream_t* const stream) {
	/*
	Returns the current file offset.
	
	Returns (>=0) on success, (-1) on error.
	*/
	
	int64_t value = 0;
	
	#if defined(_WIN32)
		LARGE_INTEGER offset = {0};
		offset.QuadPart = 0;
		
		if (SetFilePointerEx(stream->stream, offset, &offset, FILE_CURRENT) == FALSE) {
			return FSTREAM_ERROR;
		}
		
		value = offset.QuadPart;
	#else
		value = ftello(stream->stream);
		
		if (value == -1) {
			return FSTREAM_ERROR;
		}
	#endif
	
	return value;
	
}

int fstream_getfd(fstream_t* const stream) {
	/*
	Returns the current file descriptor.
	
	Returns (>=0) on success, (-1) on error.
	*/
	
	#if defined(_WIN32)
		const int value = stream->fd;
	#else
		const int value = fileno(stream->stream);
	#endif
	
	return (int) value;
	
}

int64_t fstream_size(fstream_t* const stream) {
	/*
	Returns the current file size.
	
	Returns (>=0) on success, (-1) on error.
	*/
	
	const int64_t pos = fstream_tell(stream);
	
	int64_t file_size = 0;
	
	int status = 0;
	
	if (pos == FSTREAM_ERROR) {
		return FSTREAM_ERROR;
	}
	
	status = fstream_seek(stream, 0, FSTREAM_SEEK_END);
	
	if (status == FSTREAM_ERROR) {
		return FSTREAM_ERROR;
	}
	
	file_size = fstream_tell(stream);
	
	if (file_size == FSTREAM_ERROR) {
		fstream_seek(stream, pos, FSTREAM_SEEK_BEGIN);
		return FSTREAM_ERROR;
	}
	
	status = fstream_seek(stream, pos, FSTREAM_SEEK_BEGIN);
	
	if (status == FSTREAM_ERROR) {
		return FSTREAM_ERROR;
	}
	
	return file_size;
	
}

int fstream_truncate(fstream_t* const stream, const int64_t offset) {
	/*
	Truncates the file.
	
	Returns (0) on success, (-1) on error.
	*/
	
	int status = 0;
	int64_t file_size = 0;
	
	#if defined(_WIN32)
		char zeros[1024];
		int64_t diff = 0;
		size_t write = 0;
	#else
		int fd = 0;
	#endif
	
	if (offset < 0) {
		return FSTREAM_ERROR;
	}
	
	file_size = fstream_size(stream);
	
	if (file_size == FSTREAM_ERROR) {
		return FSTREAM_ERROR;
	}
	
	if (offset == file_size) {
		return FSTREAM_SUCCESS;
	}
	
	#if defined(_WIN32)
		if (offset < file_size) {
			status = fstream_seek(stream, offset, FSTREAM_SEEK_BEGIN);
			
			if (status != FSTREAM_SUCCESS) {
				return FSTREAM_ERROR;
			}
			
			if (!SetEndOfFile(stream->stream)) {
				return FSTREAM_ERROR;
			}
		} else {
			memset(zeros, 0, sizeof(zeros));
			
			status = fstream_seek(stream, 0, FSTREAM_SEEK_END);
			
			if (status != FSTREAM_SUCCESS) {
				return FSTREAM_ERROR;
			}
			
			diff = offset - file_size;
			
			while (diff != 0) {
				write = (diff > (int64_t) sizeof(zeros)) ? sizeof(zeros) : (size_t) diff;
				
				status = fstream_write(stream, zeros, write);
				
				if (status != FSTREAM_SUCCESS) {
					return FSTREAM_ERROR;
				}
				
				diff -= (int64_t) write;
			}
		}
	#else
		fd = fileno(stream->stream);
		
		if (fd == -1) {
			return FSTREAM_ERROR;
		}
		
		if (ftruncate(fd, offset) == -1) {
			return FSTREAM_ERROR;
		}
		
		status = fstream_seek(stream, offset, FSTREAM_SEEK_BEGIN);
		
		if (status != FSTREAM_SUCCESS) {
			return FSTREAM_ERROR;
		}
	#endif
	
	return FSTREAM_SUCCESS;
	
}

int fstream_close(fstream_t* const stream) {
	/*
	Closes the stream.
	
	Returns (0) on success, (-1) on error.
	*/
	
	int status = FSTREAM_SUCCESS;
	
	if (stream == NULL) {
		return FSTREAM_SUCCESS;
	}
	
	#if defined(_WIN32)
		if (stream->stream != 0) {
			if (_close(stream->fd) != 0) {
				status = FSTREAM_ERROR;
				
				if (stream->stream != INVALID_HANDLE_VALUE) {
					CloseHandle(stream->stream);
				}
			}
			
			stream->stream = 0;
			stream->fd = -1;
		}
	#else
		if (stream->stream != NULL) {
			if (fclose(stream->stream) != 0) {
				status = FSTREAM_ERROR;
			}
			
			stream->stream = NULL;
		}
	#endif
	
	free(stream);
	
	return status;
	
}
