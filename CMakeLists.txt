cmake_minimum_required(VERSION 3.19 FATAL_ERROR)

project(
	Nouzen
	VERSION 0.1
	DESCRIPTION "A command-line utility to download and install packages from APT repositories."
	HOMEPAGE_URL "https://github.com/AmanoTeam/Nouzen"
	LANGUAGES C
)

option(NOUZEN_LTO "Turn on compiler link-time optimizations" OFF)
option(NOUZEN_UNICODE "Enable Unicode support for Windows builds" ON)
option(NOUZEN_DBG "Build the main project with debugging symbols, even in release builds" OFF)
option(NOUZEN_ASAN "Build with AddressSanitizer for the nz-jni JNI library" OFF)
option(NOUZEN_BUILD_SHARED "Build the project as a shared library" OFF)
option(NOUZEN_SSL_VERIFY "Enable SSL certificate verification in the network library" ON)

set(CMAKE_POLICY_DEFAULT_CMP0048 NEW)
set(CMAKE_POLICY_DEFAULT_CMP0069 NEW)
set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)
set(CMAKE_PLATFORM_NO_VERSIONED_SONAME ON)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")

set(EXEC_RPATH "")
set(LIB_RPATH "")

if (APPLE)
	set(EXEC_RPATH "@loader_path/../lib")
	set(LIB_RPATH "@loader_path")
elseif (UNIX)
	set(EXEC_RPATH "$ORIGIN/../lib:$ORIGIN")
	set(LIB_RPATH "$ORIGIN")
endif()

set(
	WARNING_OPTIONS
	"-Wall"
	"-Wextra"
)

set(CURL_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/curl")
set(BEARSSL_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/bearssl")
set(NGHTTP2_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/nghttp2")
set(LIBARCHIVE_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/libarchive")
set(XZ_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/xz-utils")
set(ZLIB_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/zlib")
set(ZSTD_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/zstd")
set(BZIP2_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/bzip2")
set(WOLFSSL_SOURCE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/submodules/wolfssl")

set(
	INCLUDE_DIRECTORIES
	"${CMAKE_CURRENT_SOURCE_DIR}/src"
	"${CMAKE_CURRENT_BINARY_DIR}/submodules/nghttp2/lib/includes"
)

set(
	NOUZEN_SOURCE_FILES
	"${CMAKE_CURRENT_SOURCE_DIR}/src/argparse.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/ask.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/base_uri.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/biggestint.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/buffer.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/downloader.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/format.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/absrel.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/exists.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/getexec.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/mkdir.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/normpath.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/parentpath.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/realpath.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/rm.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/cp.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/stripsep.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/splitext.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/chdir.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/symlink.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/chmod.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/walkdir.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/fstream.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/fs/stat.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/term/screen.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/term/keyboard.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/wildcard_match.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/guess_file_format.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/guess_uri.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/hex.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/errors.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/logging.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/options.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/cpuinfo.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/envdir.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/find_exe.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/system.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/execv.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/posix_spawn.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/osdetect.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/os/rlimit.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/package.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/pprint.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/progress_callback.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/query.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/repository.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/sslcerts.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/strsplit.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/uncompress.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/urldecode.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/urlencode.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/wcurl.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/strsub.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/write_callback.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/elfutils/elfutils.c"
)

set(
	LIBQUERY_JNI_SOURCE_FILES
	"${CMAKE_CURRENT_SOURCE_DIR}/src/nz-jni.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/src/repository-jni.c"
)

set(
	TARGETS
	nz
	wolfssl
	libcurl_shared
	libzstd_shared
	liblzma
	zlib
	archive
	nghttp2
	bz2
)


set(
	CURL_LIBRARY_DEPENDENCIES
	wolfssl
	nghttp2
)

set(
	ARCHIVE_LIBRARY_DEPENDENCIES
	libzstd_shared
	liblzma
	zlib
	bz2
)

# curl
set(PICKY_COMPILER OFF)
set(BUILD_CURL_EXE OFF)
set(HTTP_ONLY ON)
set(ENABLE_THREADED_RESOLVER ON)
set(CURL_USE_WOLFSSL ON)
set(CURL_USE_OPENSSL OFF)
set(CURL_USE_LIBPSL OFF)
set(CURL_DISABLE_ALTSVC ON)
set(CURL_DISABLE_CRYPTO_AUTH ON)
set(CURL_DISABLE_COOKIES OFF)
set(CURL_DISABLE_HSTS ON)
set(CURL_DISABLE_HTTP_AUTH ON)
set(CURL_DISABLE_NETRC ON)
set(CURL_DISABLE_WEBSOCKETS ON)
set(USE_LIBIDN2 OFF)
set(ENABLE_UNIX_SOCKETS OFF)
set(CURL_USE_LIBSSH2 OFF)
set(CURL_CA_BUNDLE "none")
set(CURL_CA_PATH "none")
set(CURL_WERROR OFF)
set(CURL_DISABLE_DOH OFF)
set(USE_NGHTTP2 ON)
set(BUILD_LIBCURL_DOCS OFF)
set(BUILD_MISC_DOCS OFF)
set(ENABLE_CURL_MANUAL OFF)
set(BUILD_TESTING OFF)
set(BUILD_EXAMPLES OFF)
set(CURL_ENABLE_EXPORT_TARGET OFF)
set(HAVE_WOLFSSL_GET_PEER_CERTIFICATE ON)
set(HAVE_WOLFSSL_USEALPN ON)
set(HAVE_WOLFSSL_BIO_NEW ON)
set(HAVE_WOLFSSL_BIO_SET_SHUTDOWN ON)

if (WIN32 AND NOUZEN_UNICODE)
	set(ENABLE_UNICODE ON)
endif()

# wolfssl
set(WOLFSSL_CURL ON)
set(WOLFSSL_OPENSSLALL ON)
set(WOLFSSL_SESSIONCERTS ON)

# nghttp2
set(ENABLE_LIB_ONLY ON)
set(WITH_LIBXML2 OFF)
set(WITH_JEMALLOC OFF)
set(ENABLE_FAILMALLOC OFF)
set(ENABLE_DOC OFF)

# xz-utils
set(XZ_NLS OFF)

if (CMAKE_SYSTEM_NAME STREQUAL "OpenBSD")
	set(XZ_ASM_I386 OFF)
endif()

# zstd
set(BUILD_SHARED_LIBS ON)
set(ZSTD_BUILD_PROGRAMS OFF)
set(ZSTD_BUILD_TESTS OFF)
set(ZSTD_BUILD_STATIC OFF)

# libarchive
set(ENABLE_TEST OFF)
set(HAVE_LIBZSTD ON)
set(HAVE_LZMA_STREAM_ENCODER_MT OFF)
set(LIBLZMA_HAS_AUTO_DECODER ON)
set(LIBLZMA_HAS_EASY_ENCODER ON)
set(LIBLZMA_HAS_LZMA_PRESET ON)
set(USE_BZIP2_DLL ON)
set(ENABLE_OPENSSL OFF)
set(LIBMD_FOUND OFF)

# bzip2
set(ENABLE_LIB_ONLY ON)
set(ENABLE_SHARED_LIB ON)
set(ENABLE_STATIC_LIB OFF)
set(ENABLE_TESTS OFF)

execute_process(
	COMMAND git submodule update --init --depth=1
	WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
	COMMAND_ERROR_IS_FATAL ANY
)

file(READ "${CURL_SOURCE_DIRECTORY}/CMakeLists.txt" FILE_CONTENTS) 
string(REPLACE "DISABLE_FILE ON" "DISABLE_FILE OFF" FILE_CONTENTS "${FILE_CONTENTS}")
string(REPLACE "list(APPEND CMAKE_REQUIRED_LIBRARIES ZLIB::ZLIB)" "" FILE_CONTENTS "${FILE_CONTENTS}")
file(WRITE "${CURL_SOURCE_DIRECTORY}/CMakeLists.txt" "${FILE_CONTENTS}") 

file(READ "${BZIP2_SOURCE_DIRECTORY}/CMakeLists.txt" FILE_CONTENTS) 
string(REPLACE "Python3_FOUND" "0" FILE_CONTENTS "${FILE_CONTENTS}") 
file(WRITE "${BZIP2_SOURCE_DIRECTORY}/CMakeLists.txt" "${FILE_CONTENTS}") 

file(READ "${ZLIB_SOURCE_DIRECTORY}/CMakeLists.txt" FILE_CONTENTS)
string(REPLACE "(UNIX)" "(1)" FILE_CONTENTS "${FILE_CONTENTS}")
string(REPLACE "(NOT APPLE)" "(0)" FILE_CONTENTS "${FILE_CONTENTS}")
file(WRITE "${ZLIB_SOURCE_DIRECTORY}/CMakeLists.txt" "${FILE_CONTENTS}")

file(READ "${LIBARCHIVE_SOURCE_DIRECTORY}/CMakeLists.txt" FILE_CONTENTS)
string(REPLACE "Haiku" "none" FILE_CONTENTS "${FILE_CONTENTS}")
file(WRITE "${LIBARCHIVE_SOURCE_DIRECTORY}/CMakeLists.txt" "${FILE_CONTENTS}")

file(READ "${NGHTTP2_SOURCE_DIRECTORY}/lib/CMakeLists.txt" FILE_CONTENTS)
string(REPLACE "add_library(nghttp2 ALIAS \${LIB_SELECTED})" "" FILE_CONTENTS "${FILE_CONTENTS}")
file(WRITE "${NGHTTP2_SOURCE_DIRECTORY}/lib/CMakeLists.txt" "${FILE_CONTENTS}")

include_directories(${INCLUDE_DIRECTORIES})

set(
	NGHTTP2_INCLUDE_DIR
	"${NGHTTP2_SOURCE_DIRECTORY}/lib/includes"
	"${CMAKE_CURRENT_BINARY_DIR}/submodules/nghttp2/lib/includes"
	CACHE PATH ""
)

set(NGHTTP2_LIBRARY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}nghttp2${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

set(WOLFSSL_INCLUDE_DIR "${WOLFSSL_SOURCE_DIRECTORY}" CACHE PATH "" FORCE)
set(WOLFSSL_LIBRARY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}wolfssl${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

set(BZIP2_INCLUDE_DIR "${BZIP2_SOURCE_DIRECTORY}" CACHE PATH "")
set(BZIP2_LIBRARIES "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}bz2${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

set(ZLIB_INCLUDE_DIR "${ZLIB_SOURCE_DIRECTORY}" CACHE PATH "")
set(ZLIB_LIBRARY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}z${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

set(ZSTD_INCLUDE_DIR "${ZSTD_SOURCE_DIRECTORY}/lib" CACHE PATH "")
set(ZSTD_LIBRARY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}zstd${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

set(LIBLZMA_INCLUDE_DIR "${XZ_SOURCE_DIRECTORY}/src/liblzma/api" CACHE PATH "")
set(LIBLZMA_LIBRARY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/${CMAKE_SHARED_LIBRARY_PREFIX}lzma${CMAKE_SHARED_LIBRARY_SUFFIX}" CACHE FILEPATH "")

if (CMAKE_C_COMPILER_ID STREQUAL "GNU")
	add_compile_options($<$<CONFIG:Release>:-fipa-pta>)
endif()

if (ANDROID)
	set(HAVE_FSEEKO 0)
endif()

add_subdirectory(
	"${ZSTD_SOURCE_DIRECTORY}/build/cmake"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${BZIP2_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${ZLIB_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${CURL_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

target_include_directories(
	libcurl_shared
	PRIVATE
	"${CMAKE_CURRENT_BINARY_DIR}/submodules/wolfssl"
)

add_subdirectory(
	"${XZ_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${LIBARCHIVE_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${NGHTTP2_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

add_subdirectory(
	"${WOLFSSL_SOURCE_DIRECTORY}"
	EXCLUDE_FROM_ALL
)

target_compile_definitions(
	libzstd_shared
	PRIVATE
	ZDICT_QSORT=ZDICT_QSORT_MIN
)

set_target_properties(
	archive
	PROPERTIES
	C_STANDARD 99
	C_STANDARD_REQUIRED ON
)

set_target_properties(
	liblzma
	PROPERTIES
	C_STANDARD 99
	C_STANDARD_REQUIRED ON
)

if (CMAKE_SYSTEM_NAME STREQUAL "OpenBSD")
	target_compile_options(
		liblzma
		PRIVATE
		-include sys/types.h
	)
endif()

if (NOT WIN32)
	target_compile_definitions(
		nghttp2
		PRIVATE
		HAVE_ARPA_INET_H HAVE_NETINET_IN_H
	)
endif()

foreach(target ${CURL_LIBRARY_DEPENDENCIES})
	add_custom_command(
		OUTPUT ${target}
		COMMAND ${CMAKE_COMMAND} --build ./ --target ${target}
	)
	
	add_custom_target(
		"ensure_${target}" ALL DEPENDS ${target}
	)
	
	add_dependencies(
		libcurl_shared
		"ensure_${target}"
	)
endforeach()

foreach(target ${ARCHIVE_LIBRARY_DEPENDENCIES})
	add_custom_command(
		OUTPUT ${target}
		COMMAND ${CMAKE_COMMAND} --build ./ --target ${target}
	)
	
	add_custom_target(
		"ensure_${target}" ALL DEPENDS ${target}
	)
	
	add_dependencies(
		archive
		"ensure_${target}"
	)
endforeach()

if (NOUZEN_BUILD_SHARED)
	add_library(
		nz
		SHARED
		${NOUZEN_SOURCE_FILES}
	)
	
	add_library(
		nz-jni
		SHARED
		${LIBQUERY_JNI_SOURCE_FILES}
	)

	target_link_libraries(nz-jni PRIVATE nz)

	set_target_properties(nz-jni PROPERTIES BUILD_RPATH "$ORIGIN")

	if (NOUZEN_ASAN)
		target_compile_options(nz-jni PRIVATE -fsanitize=address)
		target_link_options(nz-jni PRIVATE -fsanitize=address)
	endif()
else()
	add_executable(
		nz
		${NOUZEN_SOURCE_FILES}
	)
	
	target_sources(
		nz
		PRIVATE
		"${CMAKE_CURRENT_SOURCE_DIR}/src/main.c"
	)
endif()

target_compile_options(
	nz
	PRIVATE
	${WARNING_OPTIONS}
)

if (NOUZEN_DBG)
	target_compile_options(
		nz
		PRIVATE
		-g
	)
endif()

target_compile_definitions(
	nz
	PRIVATE
	CURL_SKIP_INCLUDE_MPRINTF
)

if (UNIX)
	target_compile_definitions(nz PRIVATE _GNU_SOURCE)
	target_compile_definitions(nz PRIVATE _FILE_OFFSET_BITS=64)
endif()

if (NOT NOUZEN_SSL_VERIFY)
	target_compile_definitions(
		nz
		PRIVATE
		WCURL_DISABLE_SSL_VERIFY
	)
endif()

set_target_properties(
	nz
	PROPERTIES
	BUILD_RPATH "${EXEC_RPATH}"
	INSTALL_RPATH "${EXEC_RPATH}"
)

set_target_properties(
	libcurl_shared archive
	PROPERTIES
	BUILD_RPATH "${LIB_RPATH}"
	INSTALL_RPATH "${LIB_RPATH}"
)

# Enable Unicode on Windows builds
if (WIN32 AND NOUZEN_UNICODE)
	target_sources(
		nz
		PRIVATE
		"src/wio.c"
	)
	
	target_compile_definitions(
		nz
		PRIVATE
		UNICODE _UNICODE
	)
	
	target_link_options(
		nz
		BEFORE
		PRIVATE
		-municode
	)
endif()

if (NOUZEN_LTO)
	set(NOUZEN_HAS_LTO OFF)
	
	include(CheckIPOSupported)
	check_ipo_supported(RESULT NOUZEN_HAS_LTO LANGUAGES C)
	
	if (NOUZEN_HAS_LTO)
		set_target_properties(
			${TARGETS}
			PROPERTIES
			INTERPROCEDURAL_OPTIMIZATION TRUE
		)
	endif()
endif()

target_link_libraries(
	nz
	PRIVATE
	libcurl_shared
	archive
)

install(
	TARGETS ${TARGETS}
	RUNTIME DESTINATION bin
	LIBRARY DESTINATION lib NAMELINK_SKIP
)

install(
	FILES "${CMAKE_CURRENT_SOURCE_DIR}/submodules/cabundle/pem/cert.pem"
	DESTINATION etc/tls
	RENAME cert.pem
)

install(
	FILES "${CMAKE_CURRENT_SOURCE_DIR}/options.conf"
	DESTINATION etc/nouzen
)

install(
	PROGRAMS "${CMAKE_CURRENT_SOURCE_DIR}/tools/apt.sh"
	TYPE BIN
	RENAME apt
)

install(
	PROGRAMS "${CMAKE_CURRENT_SOURCE_DIR}/tools/apt.sh"
	TYPE BIN
	RENAME apt-get
)

