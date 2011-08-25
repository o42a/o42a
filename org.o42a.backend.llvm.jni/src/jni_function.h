/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_code_LLSignatureWriter */

#ifndef _Included_org_o42a_backend_llvm_code_LLSignatureWriter
#define _Included_org_o42a_backend_llvm_code_LLSignatureWriter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLSignatureWriter
 * Method:    createSignature
 * Signature: (JLjava/lang/String;J[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLSignatureWriter_createSignature
  (JNIEnv *, jclass, jlong, jstring, jlong, jlongArray);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_code_LLFunction */

#ifndef _Included_org_o42a_backend_llvm_code_LLFunction
#define _Included_org_o42a_backend_llvm_code_LLFunction
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLFunction
 * Method:    externFunction
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLFunction_externFunction
  (JNIEnv *, jclass, jlong, jstring, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLFunction
 * Method:    createFunction
 * Signature: (JLjava/lang/String;JZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLFunction_createFunction
  (JNIEnv *, jclass, jlong, jstring, jlong, jboolean);

/*
 * Class:     org_o42a_backend_llvm_code_LLFunction
 * Method:    arg
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLFunction_arg
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_code_LLFunction
 * Method:    validate
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_o42a_backend_llvm_code_LLFunction_validate
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_code_op_LLFunc */

#ifndef _Included_org_o42a_backend_llvm_code_op_LLFunc
#define _Included_org_o42a_backend_llvm_code_op_LLFunc
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_op_LLFunc
 * Method:    call
 * Signature: (JLjava/lang/String;J[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_op_LLFunc_call
  (JNIEnv *, jclass, jlong, jstring, jlong, jlongArray);

#ifdef __cplusplus
}
#endif
#endif
