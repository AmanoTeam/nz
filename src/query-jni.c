#include <jni.h>
#include "query.h"

JNIEXPORT void JNICALL queryFree(
    JNIEnv* env,
    jobject obj,
    jlong query
) {

    hquery_t* c_query = (hquery_t*) query;
    query_free(c_query);

}


JNIEXPORT jint JNICALL queryAddString(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key,
    jstring value
) {

    hquery_t* c_query = (hquery_t*) query;
    
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    const char* c_value = (*env)->GetStringUTFChars(env, value, NULL);
    
    const int status = query_add_string(c_query, c_key, c_value);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    (*env)->ReleaseStringUTFChars(env, value, c_value);
    
    return (jint) status;

}


JNIEXPORT jint JNICALL queryAddInt(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key,
    jlong value
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    const int status = query_add_int(c_query, c_key, (bigint_t)value);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jint) status;

}


JNIEXPORT jint JNICALL queryAddUint(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key,
    jlong value
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    const int status = query_add_uint(c_query, c_key, (biguint_t)value);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jint) status;

}


JNIEXPORT jint JNICALL queryAddFloat(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key,
    jdouble value
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    const int status = query_add_float(c_query, c_key, (bigfloat_t)value);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jint) status;

}


JNIEXPORT jlong JNICALL queryGetItem(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jlong index
) {

    hquery_t* c_query = (hquery_t*) query;
    hquery_param_t* param = query_get_item(c_query, (size_t)index);
    return (jlong) param;

}


JNIEXPORT jstring JNICALL queryGetString(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    char* result = query_get_string(c_query, c_key);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    
    if (result == NULL) {
        return NULL;
    }
    
    jstring j_result = (*env)->NewStringUTF(env, result);
    return j_result;

}


JNIEXPORT jstring JNICALL paramGetString(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    const char* result = param_get_string(c_param);
    return result ? (*env)->NewStringUTF(env, result) : NULL;

}


JNIEXPORT jlong JNICALL queryGetInt(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    bigint_t result = query_get_int(c_query, c_key);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jlong) result;

}


JNIEXPORT jlong JNICALL paramGetInt(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    return (jlong) param_get_int(c_param);

}


JNIEXPORT jlong JNICALL queryGetUint(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    biguint_t result = query_get_uint(c_query, c_key);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jlong) result;

}


JNIEXPORT jlong JNICALL paramGetUint(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    return (jlong) param_get_uint(c_param);

}


JNIEXPORT jdouble JNICALL queryGetFloat(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    bigfloat_t result = query_get_float(c_query, c_key);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jdouble) result;

}


JNIEXPORT jdouble JNICALL paramGetFloat(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    return (jdouble) param_get_float(c_param);

}


JNIEXPORT jint JNICALL queryGetBool(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring key
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_key = (*env)->GetStringUTFChars(env, key, NULL);
    
    const int result = query_get_bool(c_query, c_key);
    
    (*env)->ReleaseStringUTFChars(env, key, c_key);
    return (jint) result;

}


JNIEXPORT jint JNICALL paramGetBool(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    return (jint) param_get_bool(c_param);

}


JNIEXPORT jlong JNICALL queryInit(
    JNIEnv* env,
    jobject obj,
    jchar sep,
    jstring subsep
) {

    hquery_t* c_query = calloc(1, sizeof(hquery_t));
    const char* c_subsep = NULL;
    
    if (subsep != NULL) {
        c_subsep = (*env)->GetStringUTFChars(env, subsep, NULL);
    }
    
    query_init(c_query, (char)sep, c_subsep);
    
    if (c_subsep != NULL) {
        (*env)->ReleaseStringUTFChars(env, subsep, c_subsep);
    }
	
	return (jlong) c_query;
	
}


JNIEXPORT jint JNICALL queryLoadString(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring string
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_string = (*env)->GetStringUTFChars(env, string, NULL);
    
    const int status = query_load_string(c_query, c_string);
    
    (*env)->ReleaseStringUTFChars(env, string, c_string);
    return (jint) status;

}


JNIEXPORT jint JNICALL queryLoadFile(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring filename
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_filename = (*env)->GetStringUTFChars(env, filename, NULL);
    
    const int status = query_load_file(c_query, c_filename);
    
    (*env)->ReleaseStringUTFChars(env, filename, c_filename);
    return (jint) status;

}


JNIEXPORT jint JNICALL queryLoadEnviron(
    JNIEnv* env,
    jobject obj,
    jlong query
) {

    hquery_t* c_query = (hquery_t*) query;
    return (jint) query_load_environ(c_query);

}


JNIEXPORT void JNICALL paramFree(
    JNIEnv* env,
    jobject obj,
    jlong param
) {

    hquery_param_t* c_param = (hquery_param_t*) param;
    param_free(c_param);

}


JNIEXPORT jint JNICALL queryDumpString(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jbyteArray destination
) {

    hquery_t* c_query = (hquery_t*) query;

    if (destination == NULL) {
        return (jint) query_dump_string(c_query, NULL);
    }

    size_t size = query_dump_string(c_query, NULL);

    if (size < 2) {
        return 0;
    }

    char* buffer = malloc(size);

    if (buffer == NULL) {
        return -1;
    }

    query_dump_string(c_query, buffer);

    jsize len = (jsize)(size - 1);
    (*env)->SetByteArrayRegion(env, destination, 0, len, (jbyte*)buffer);

    free(buffer);
    return len;

}


JNIEXPORT jint JNICALL queryDumpFile(
    JNIEnv* env,
    jobject obj,
    jlong query,
    jstring filename
) {

    hquery_t* c_query = (hquery_t*) query;
    const char* c_filename = (*env)->GetStringUTFChars(env, filename, NULL);
    
    const int status = query_dump_file(c_query, c_filename);
    
    (*env)->ReleaseStringUTFChars(env, filename, c_filename);
    return (jint) status;

}


static JNINativeMethod JNI_NATIVES_METHODS[] = {
    {"queryFree",        "(J)V",                                  (void*)queryFree},
    {"queryAddString",   "(JLjava/lang/String;Ljava/lang/String;)I", (void*)queryAddString},
    {"queryAddInt",      "(JLjava/lang/String;J)I",               (void*)queryAddInt},
    {"queryAddUint",     "(JLjava/lang/String;J)I",               (void*)queryAddUint},
    {"queryAddFloat",    "(JLjava/lang/String;D)I",               (void*)queryAddFloat},
    {"queryGetItem",     "(JJ)J",                                 (void*)queryGetItem},
    {"queryGetString",   "(JLjava/lang/String;)Ljava/lang/String;", (void*)queryGetString},
    {"paramGetString",   "(J)Ljava/lang/String;",                 (void*)paramGetString},
    {"queryGetInt",      "(JLjava/lang/String;)J",                (void*)queryGetInt},
    {"paramGetInt",      "(J)J",                                  (void*)paramGetInt},
    {"queryGetUint",     "(JLjava/lang/String;)J",                (void*)queryGetUint},
    {"paramGetUint",     "(J)J",                                  (void*)paramGetUint},
    {"queryGetFloat",    "(JLjava/lang/String;)D",                (void*)queryGetFloat},
    {"paramGetFloat",    "(J)D",                                  (void*)paramGetFloat},
    {"queryGetBool",     "(JLjava/lang/String;)I",                (void*)queryGetBool},
    {"paramGetBool",     "(J)I",                                  (void*)paramGetBool},
    {"queryInit",        "(CLjava/lang/String;)J",                (void*)queryInit},
    {"queryLoadString",  "(JLjava/lang/String;)I",                (void*)queryLoadString},
    {"queryLoadFile",    "(JLjava/lang/String;)I",                (void*)queryLoadFile},
    {"queryLoadEnviron", "(J)I",                                  (void*)queryLoadEnviron},
    {"paramFree",        "(J)V",                                  (void*)paramFree},
    {"queryDumpString",  "(J[B)I",                                (void*)queryDumpString},
    {"queryDumpFile",    "(JLjava/lang/String;)I",                (void*)queryDumpFile}
};


jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	
	JNIEnv* env = NULL;
	jclass class = NULL;
	
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR;
	}
	
	class = (*env)->FindClass(env, "com/amanoteam/nz/library/LibQuery");
	
    if (class == NULL) {
        return JNI_ERR;
    }
    
	if ((*env)->RegisterNatives(env, class, JNI_NATIVES_METHODS, sizeof(JNI_NATIVES_METHODS) / sizeof(*JNI_NATIVES_METHODS)) < 0) {
        return JNI_ERR;
    }
	
	return JNI_VERSION_1_6;
	
}