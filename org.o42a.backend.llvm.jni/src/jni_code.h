/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_code_LLCode */

#ifndef _Included_org_o42a_backend_llvm_code_LLCode
#define _Included_org_o42a_backend_llvm_code_LLCode
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    createBlock
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_createBlock
  (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    stackSave
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_stackSave
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    stackRestore
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLCode_stackRestore
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    go
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLCode_go
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    choose
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLCode_choose
  (JNIEnv *, jclass, jlong, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    blockAddress
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_blockAddress
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    indirectbr
 * Signature: (JJ[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_indirectbr
  (JNIEnv *, jclass, jlong, jlong, jlongArray);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    int8
 * Signature: (JB)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_int8
  (JNIEnv *, jclass, jlong, jbyte);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    int16
 * Signature: (JS)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_int16
  (JNIEnv *, jclass, jlong, jshort);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    int32
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_int32
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    int64
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_int64
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    fp32
 * Signature: (JF)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_fp32
  (JNIEnv *, jclass, jlong, jfloat);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    fp64
 * Signature: (JD)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_fp64
  (JNIEnv *, jclass, jlong, jdouble);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    bool
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_bool
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    nullPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_nullPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    nullStructPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_nullStructPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    nullFuncPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_nullFuncPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    allocatePtr
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_allocatePtr
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    allocateStructPtr
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_allocateStructPtr
  (JNIEnv *, jclass, jlong, jstring, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    allocateStruct
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_allocateStruct
  (JNIEnv *, jclass, jlong, jstring, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    phi2
 * Signature: (JLjava/lang/String;JJJJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_phi2
  (JNIEnv *, jclass, jlong, jstring, jlong, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    phiN
 * Signature: (JLjava/lang/String;[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_phiN
  (JNIEnv *, jclass, jlong, jstring, jlongArray);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    select
 * Signature: (JLjava/lang/String;JJJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_select
  (JNIEnv *, jclass, jlong, jstring, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    returnVoid
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLCode_returnVoid
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLCode
 * Method:    returnValue
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLCode_returnValue
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
