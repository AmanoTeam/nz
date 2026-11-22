#include <stdlib.h>
#include <string.h>

#include <curl/curl.h>

#include "fs/exists.h"
#include "fs/getexec.h"
#include "sslcerts.h"
#include "fs/sep.h"

static const char* const SSL_CERTIFICATE_LOCATIONS[] = {
#if defined(__APPLE__)
	"/etc/ssl/cert.pem",
	"/System/Library/OpenSSL/certs/cert.pem"
#elif defined(__HAIKU__)
	"/system/data/ssl/CARootCertificates.pem"
#elif defined(__ANDROID__)
	"/data/data/com.termux/files/usr/etc/tls/cert.pem"
#elif defined(__linux__)
	"/etc/pki/tls/certs/ca-bundle.crt",
	"/etc/ssl/ca-bundle.pem",
	"/etc/ssl/certs/ca-certificates.crt",
	"/usr/share/ssl/certs/ca-bundle.crt"
#elif defined(__OpenBSD__) || defined(__FreeBSD__) || defined(__NetBSD__) || defined(__DragonFly__)
	"/etc/ssl/cert.pem",
	"/etc/ssl/certs/ca-certificates.crt",
	"/etc/openssl/certs/ca-certificates.crt",
	"/usr/local/share/certs/ca-root-nss.crt",
#else
	"/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem",
	"/etc/pki/tls/certs/ca-bundle.crt",
	"/etc/ssl/ca-bundle.pem",
	"/etc/ssl/cert.pem",
	"/etc/ssl/certs/ca-certificates.crt",
	"/usr/local/share/certs/ca-root-nss.crt",
	"/usr/share/ssl/certs/ca-bundle.crt"
#endif
};

static const char BUILTIN_CA_CERT_LOCATION[] = 
	PATHSEP_M
	"etc"
	PATHSEP_M
	"tls"
	PATHSEP_M
	"cert.pem";

static char* cacert = NULL;

int sslcerts_load_certificates(CURL* const curl) {
	
	int err = SSLCERTS_SUCCESS;
	
	CURLcode code = CURLE_OK;
	
	size_t index = 0;
	
	const char* location = NULL;
	
	char* app_directory = NULL;
	char* name = NULL;
	
	code = curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 1L);
	
	if (code != CURLE_OK) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	code = curl_easy_setopt(curl, CURLOPT_CAINFO, cacert);
	
	if (code != CURLE_OK) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	if (cacert != NULL) {
		return err;
	}
	
	for (index = 0; index < sizeof(SSL_CERTIFICATE_LOCATIONS) / sizeof(*SSL_CERTIFICATE_LOCATIONS); index++) {
		location = SSL_CERTIFICATE_LOCATIONS[index];
		
		if (file_exists(location) != 1) {
			continue;
		}
		
		cacert = strdup(location);
		
		if (cacert == NULL) {
			err = SSLCERTS_ERROR;
			goto end;
		}
		
		goto end;
	}
	
	app_directory = get_app_directory();
	
	if (app_directory == NULL) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	name = malloc(strlen(app_directory) + strlen(BUILTIN_CA_CERT_LOCATION) + 1);
	
	if (name == NULL) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	strcpy(name, app_directory);
	strcat(name, BUILTIN_CA_CERT_LOCATION);
	
	if (file_exists(name) != 1) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	cacert = strdup(name);
	
	if (cacert == NULL) {
		err = SSLCERTS_ERROR;
		goto end;
	}
	
	end:;
	
	free(app_directory);
	free(name);
	
	code = curl_easy_setopt(curl, CURLOPT_CAINFO, cacert);
	
	if (code != CURLE_OK) {
		err = SSLCERTS_ERROR;
	}
	
	return err;
	
}

void sslcerts_unload_certificates(void) {
	free(cacert);
	cacert = NULL;
}
