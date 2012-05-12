/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_o42a_backend_llvm_data_SystemTypeInfo */

#ifndef _Included_org_o42a_backend_llvm_data_SystemTypeInfo
#define _Included_org_o42a_backend_llvm_data_SystemTypeInfo
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_data_SystemTypeInfo
 * Method:    pthreadLayout
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadLayout
  (JNIEnv *, jclass);

/*
 * Class:     org_o42a_backend_llvm_data_SystemTypeInfo
 * Method:    pthreadMutexLayout
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadMutexLayout
  (JNIEnv *, jclass);

/*
 * Class:     org_o42a_backend_llvm_data_SystemTypeInfo
 * Method:    pthreadCondLayout
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadCondLayout
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_data_SystemTypeInfo_Registry */

#ifndef _Included_org_o42a_backend_llvm_data_SystemTypeInfo_Registry
#define _Included_org_o42a_backend_llvm_data_SystemTypeInfo_Registry
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_data_LLVMDataAllocator */

#ifndef _Included_org_o42a_backend_llvm_data_LLVMDataAllocator
#define _Included_org_o42a_backend_llvm_data_LLVMDataAllocator
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    binaryConstant
 * Signature: (JJI[BIIZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_binaryConstant
  (JNIEnv *, jclass, jlong, jlong, jint, jbyteArray, jint, jint, jboolean);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    createType
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createType
  (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    createTypeData
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createTypeData
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateStruct
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStruct
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateGlobal
 * Signature: (JJIJZZ)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateGlobal
  (JNIEnv *, jclass, jlong, jlong, jint, jlong, jboolean, jboolean);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    refineType
 * Signature: (JJZ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_refineType
  (JNIEnv *, jclass, jlong, jlong, jboolean);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateInt
 * Signature: (JJI)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateInt
  (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateFp32
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp32
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateFp64
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp64
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateFuncPtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFuncPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocatePtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocatePtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    allocateStructPtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStructPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    intLayout
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_intLayout
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    fp32layout
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp32layout
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    fp64layout
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp64layout
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    ptrLayout
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_ptrLayout
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    relPtrLayout
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_relPtrLayout
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataAllocator
 * Method:    structLayout
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_org_o42a_backend_llvm_data_LLVMDataAllocator_structLayout
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_data_LLVMDataWriter */

#ifndef _Included_org_o42a_backend_llvm_data_LLVMDataWriter
#define _Included_org_o42a_backend_llvm_data_LLVMDataWriter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    nullPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    nullStructPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullStructPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    nullFuncPtr
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullFuncPtr
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    createStruct
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_createStruct
  (JNIEnv *, jclass, jint);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeInt
 * Signature: (JJJI)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeInt
  (JNIEnv *, jclass, jlong, jlong, jlong, jint);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writePtrAsInt64
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writePtrAsInt64
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeFp32
 * Signature: (JJF)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFp32
  (JNIEnv *, jclass, jlong, jlong, jfloat);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeFp64
 * Signature: (JJD)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFp64
  (JNIEnv *, jclass, jlong, jlong, jdouble);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeFuncPtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFuncPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeDataPtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeDataPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeRelPtr
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeRelPtr
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeStruct
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeStruct
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeSystemStruct
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeSystemStruct
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     org_o42a_backend_llvm_data_LLVMDataWriter
 * Method:    writeGlobal
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeGlobal
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class org_o42a_backend_llvm_data_LLVMDataWriter_LLVMData */

#ifndef _Included_org_o42a_backend_llvm_data_LLVMDataWriter_LLVMData
#define _Included_org_o42a_backend_llvm_data_LLVMDataWriter_LLVMData
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
}
#endif
#endif
