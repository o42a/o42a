/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_data_LLVMModule */

#ifndef _Included_org_o42a_backend_llvm_data_LLVMModule
#define _Included_org_o42a_backend_llvm_data_LLVMModule
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    parseArgs
 * Signature: ([[B)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_parseArgs
  (JNIEnv *, jclass, jobjectArray);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    inputFilename
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_inputFilename
  (JNIEnv *, jclass);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    debugEnabled
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_debugEnabled
  (JNIEnv *, jclass);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    createModule
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_createModule
  (JNIEnv *, jclass, jstring);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    write
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_write
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    destroyModule
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_destroyModule
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    voidType
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_voidType
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    int32type
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_int32type
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    int64type
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_int64type
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    fp64type
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_fp64type
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    boolType
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_boolType
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    relPtrType
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_relPtrType
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    anyType
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_anyType
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    pointerTo
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_pointerTo
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMModule
 * Method:    pointerToFunc
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMModule_pointerToFunc
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
