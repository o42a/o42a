/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_code_LLVMSignatureWriter */

#ifndef _Included_org_o42a_backend_llvm_code_LLVMSignatureWriter
#define _Included_org_o42a_backend_llvm_code_LLVMSignatureWriter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLVMSignatureWriter
 * Method:    createSignature
 * Signature: (JLjava/lang/String;J[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMSignatureWriter_createSignature
  (JNIEnv *, jclass, jlong, jstring, jlong, jlongArray);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_code_LLVMFunction */

#ifndef _Included_org_o42a_backend_llvm_code_LLVMFunction
#define _Included_org_o42a_backend_llvm_code_LLVMFunction
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLVMFunction
 * Method:    externFunction
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMFunction_externFunction
  (JNIEnv *, jclass, jlong, jstring, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMFunction
 * Method:    createFunction
 * Signature: (JLjava/lang/String;JZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMFunction_createFunction
  (JNIEnv *, jclass, jlong, jstring, jlong, jboolean);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMFunction
 * Method:    arg
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMFunction_arg
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMFunction
 * Method:    validate
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_o42a_backend_llvm_code_LLVMFunction_validate
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_code_op_LLVMFunc */

#ifndef _Included_org_o42a_backend_llvm_code_op_LLVMFunc
#define _Included_org_o42a_backend_llvm_code_op_LLVMFunc
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_op_LLVMFunc
 * Method:    call
 * Signature: (JLjava/lang/String;J[J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_op_LLVMFunc_call
  (JNIEnv *, jclass, jlong, jstring, jlong, jlongArray);

#ifdef __cplusplus
}
#endif
#endif
