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
#include "jni_function.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/Function.h"
#include "llvm/Module.h"
#include "llvm/Analysis/Verifier.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_LLSignatureWriter_createSignature(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring name,
		jlong returnTypePtr,
		jlongArray paramPtrs) {

	Module *module = from_ptr<Module>(modulePtr);
	jStringRef signatureName(env, name);
	const Type *returnType = from_ptr<const Type>(returnTypePtr);
	jArray<jlongArray, jlong> paramArray(env, paramPtrs);
	const size_t numParams = paramArray.length();
	std::vector<const Type*> params(numParams);

	for (size_t i = 0; i < numParams; ++i) {
		params[i] = from_ptr<const Type>(paramArray[i]);
	}

	FunctionType *result = FunctionType::get(returnType, params, false);

	module->addTypeName(signatureName, result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLFunction_externFunction(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring name,
		jlong typePtr) {

	Module *module = from_ptr<Module>(modulePtr);
	jStringRef funcName(env, name);
	FunctionType *type = from_ptr<FunctionType>(typePtr);
	Constant *function = module->getOrInsertFunction(funcName, type);

	return to_ptr(function);
}

jlong Java_org_o42a_backend_llvm_code_LLFunction_createFunction(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring name,
		jlong funcTypePtr,
		jboolean exported) {

	Module *module = from_ptr<Module>(modulePtr);
	jStringRef funcName(env, name);
	FunctionType *type = from_ptr<FunctionType>(funcTypePtr);
	GlobalValue::LinkageTypes linkageType =
			exported
			? GlobalValue::ExternalLinkage : GlobalValue::PrivateLinkage;
	Function *function = Function::Create(
			type,
			linkageType,
			Twine(funcName.data()),
			module);

	function->setDoesNotThrow(true);

	return to_ptr(function);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLFunction_arg(
		JNIEnv *env,
		jclass cls,
		jlong functionPtr,
		jint index) {

	Function *function = from_ptr<Function>(functionPtr);
	Value *value;

	if (!index) {
		value = &*function->arg_begin();
	} else {

		Function::arg_iterator args = function->arg_begin();

		for (int i = 0; i < index; ++i) {
			++args;
		}

		value = &*args;
	}

	return to_ptr(value);
}

jboolean Java_org_o42a_backend_llvm_code_LLFunction_validate(
		JNIEnv *env,
		jclass cls,
		jlong functionPtr) {

	Function *function = from_ptr<Function>(functionPtr);
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(function->getParent());

	return module->validateFunction(function) ? JNI_TRUE : JNI_FALSE;
}

jlong Java_org_o42a_backend_llvm_code_op_LLFunc_call(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jstring id,
		jlong functionPtr,
		jlongArray argPtrs) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *callee = from_ptr<Value>(functionPtr);
	jArray<jlongArray, jlong> argArray(env, argPtrs);
	const size_t numArgs = argArray.length();
	std::vector<Value*> args(numArgs);

	for (size_t i = 0; i < numArgs; ++i) {
		args[i] = from_ptr<Value>(argArray[i]);
	}

	Value *result;

	if (id) {
		jStringRef name(env, id);
		result = builder.CreateCall(callee, args.begin(), args.end(), name);
	} else {
		result = builder.CreateCall(callee, args.begin(), args.end());
	}

	return to_ptr(result);
}
