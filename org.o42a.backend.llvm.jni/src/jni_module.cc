/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010-2013 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "jni_module.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"
#include "llvm/Support/CommandLine.h"

#include <unistd.h>
#include <sys/ioctl.h>

using namespace llvm;


jint Java_org_o42a_backend_llvm_data_LLVMModule_stderrColumns(
		JNIEnv *,
		jclass) {
#ifdef TIOCGWINSZ

	struct winsize wsize;

	if (ioctl(STDERR_FILENO, TIOCGWINSZ, (char *) &wsize)) {
		return 0;
	}

	errs() << wsize.ws_col << "\n";

	return wsize.ws_col;
#else /* TIOCGWINSZ */
	return 0;
#endif /* TIOCGWINSZ */
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_bufferPtr(
		JNIEnv *env,
		jclass,
		jobject buffer) {
	return to_ptr<void>(env->GetDirectBufferAddress(buffer));
}

void Java_org_o42a_backend_llvm_data_LLVMModule_parseArgs(
		JNIEnv *env,
		jclass,
		jobjectArray commandLine) {
	o42ac::BackendModule::initializeTargets();

	jObjectArray<jbyteArray> args(env, commandLine);
	const jsize argc = args.length();
	jbyteArray byteArrays[argc];
	jbyte *argv[argc];

	for (int i = 0; i < argc; ++i) {

		jbyteArray byteArray = args[i];

		byteArrays[i] = byteArray;
		argv[i] = env->GetByteArrayElements(byteArray, NULL);
	}

	cl::ParseCommandLineOptions(
			argc,
			(char**) argv,
			"o42a Compiler");

	for (int i = 0; i < argc; ++i) {
		env->ReleaseByteArrayElements(byteArrays[i], argv[i], JNI_ABORT);
	}
}

jbyteArray Java_org_o42a_backend_llvm_data_LLVMModule_inputFilename(
		JNIEnv *env,
		jclass) {

	const std::string *filename = o42ac::BackendModule::getInputFilename();

	if (!filename) {
		return NULL;
	}

	const size_t len = filename->length();
	jbyteArray array = env->NewByteArray(len);
	jbyte *items = env->GetByteArrayElements(array, NULL);

	for (size_t i = 0; i < len; ++i) {
		items[i] = filename->at(i);
	}

	env->ReleaseByteArrayElements(array, items, JNI_COMMIT);

	return array;
}

jbyteArray Java_org_o42a_backend_llvm_data_LLVMModule_inputEncoding(
		JNIEnv *env,
		jclass) {

	const std::string *encoding = o42ac::BackendModule::getInputEncoding();

	if (!encoding) {
		return NULL;
	}

	const size_t len = encoding->length();
	jbyteArray array = env->NewByteArray(len);
	jbyte *items = env->GetByteArrayElements(array, NULL);

	for (size_t i = 0; i < len; ++i) {
		items[i] = encoding->at(i);
	}

	env->ReleaseByteArrayElements(array, items, JNI_COMMIT);

	return array;
}

jint Java_org_o42a_backend_llvm_data_LLVMModule_debugEnabled(
		JNIEnv *,
		jclass) {
	return o42ac::BackendModule::debugEnabled();
}

jint Java_org_o42a_backend_llvm_data_LLVMModule_usesAnalysed(
		JNIEnv *,
		jclass) {
	return o42ac::BackendModule::usesAnalysed();
}

jint Java_org_o42a_backend_llvm_data_LLVMModule_normalizationEnabled(
		JNIEnv *,
		jclass) {
	return o42ac::BackendModule::normalizationEnabled();
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_createModule(
		JNIEnv *,
		jclass,
		jlong id,
		jint idLen) {

	StringRef moduleId(from_ptr<char>(id), idLen);
	o42ac::BackendModule *module =
			o42ac::BackendModule::createBackend(moduleId);

	return to_ptr<o42ac::BackendModule>(module);
}

jboolean Java_org_o42a_backend_llvm_data_LLVMModule_write(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule &module = *from_ptr<o42ac::BackendModule>(modulePtr);

	return module.writeCode() ? JNI_TRUE : JNI_FALSE;
}

void Java_org_o42a_backend_llvm_data_LLVMModule_destroyModule(
		JNIEnv *,
		jclass,
		jlong modulePtr) {
	from_ptr<o42ac::BackendModule>(modulePtr)->destroyBackend();
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_voidType(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getVoidTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_intType(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jbyte numBits) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(IntegerType::get(module->getContext(), numBits));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_fp32type(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getFloatTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_fp64type(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getDoubleTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_boolType(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getInt1Ty(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_relPtrType(
		JNIEnv *,
		jclass,
		jlong modulePtr)  {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getInt32Ty(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_anyType(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Type>(Type::getInt8PtrTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_pointerTo(
		JNIEnv *,
		jclass,
		jlong typePtr) {

	Type *type = from_ptr<Type>(typePtr);

	return to_ptr<Type>(type->getPointerTo());
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_pointerToFunc(
		JNIEnv *,
		jclass,
		jlong funcTypePtr) {

	FunctionType *type = from_ptr<FunctionType>(funcTypePtr);

	return to_ptr<Type>(type->getPointerTo());
}
