/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010,2011 Ruslan Lopatin

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

#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/Type.h"
#include "llvm/Support/CommandLine.h"

using namespace llvm;


void Java_org_o42a_backend_llvm_data_LLVMModule_parseArgs(
		JNIEnv *env,
		jclass cls,
		jobjectArray commandLine) {

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
		jclass cls) {

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

jlong Java_org_o42a_backend_llvm_data_LLVMModule_createModule(
		JNIEnv *env,
		jclass cls,
		jstring id) {

	jStringRef moduleId(env, id);
	o42ac::BackendModule *module =
			o42ac::BackendModule::createBackend(moduleId);

	return to_ptr(module);
}

jboolean Java_org_o42a_backend_llvm_data_LLVMModule_write(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	o42ac::BackendModule &module = *from_ptr<o42ac::BackendModule>(modulePtr);

	return module.writeCode() ? JNI_TRUE : JNI_FALSE;
}

void Java_org_o42a_backend_llvm_data_LLVMModule_destroyModule(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {
	from_ptr<o42ac::BackendModule>(modulePtr)->destroyBackend();
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_voidType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getVoidTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_int32type(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getInt32Ty(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_int64type(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getInt64Ty(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_fp64type(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getDoubleTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_boolType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getInt1Ty(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_anyType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Type::getInt8PtrTy(module->getContext()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMModule_pointerTo(
		JNIEnv *env,
		jclass cls,
		jlong typePtr) {

	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	return to_ptr(type->get()->getPointerTo());
}
