/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_code_LLVMCode */

#ifndef _Included_org_o42a_backend_llvm_code_LLVMCode
#define _Included_org_o42a_backend_llvm_code_LLVMCode
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    createBlock
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_createBlock
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    go
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_go
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    choose
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_choose
  (JNIEnv *, jclass, jlong, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    int32
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_int32
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    int64
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_int64
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    fp64
 * Signature: (JD)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_fp64
  (JNIEnv *, jclass, jlong, jdouble);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    bool
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_bool
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    nullPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_nullPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    nullStructPtr
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_nullStructPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    nullFuncPtr
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_nullFuncPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    allocatePtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_allocatePtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    allocateStruct
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_allocateStruct
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    phi
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_phi
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    phi2
 * Signature: (JJJJJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_phi2
  (JNIEnv *, jclass, jlong, jlong, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    returnVoid
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_returnVoid
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_code_LLVMCode
 * Method:    returnValue
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_returnValue
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
