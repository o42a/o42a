/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010-2014 Ruslan Lopatin

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

#include "llvm/Analysis/Verifier.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Module.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_LLSignatureWriter_createSignature(
		JNIEnv *env,
		jclass,
		jlong returnTypePtr,
		jlongArray paramPtrs) {

	Type *returnType = from_ptr<Type>(returnTypePtr);
	jInt64Array paramArray(env, paramPtrs);
	const size_t numParams = paramArray.length();
	Type* params[numParams];

	for (size_t i = 0; i < numParams; ++i) {
		params[i] = from_ptr<Type>(paramArray[i]);
	}

	FunctionType *result = FunctionType::get(
			returnType,
			ArrayRef<Type *>(params, numParams),
			false);

	return to_ptr<FunctionType>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLFunction_externFunction(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen,
		jlong typePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	FunctionType *type = from_ptr<FunctionType>(typePtr);
	Constant *function = module->getOrInsertFunction(
			StringRef(from_ptr<char>(id), idLen),
			type);

	return to_ptr<Value>(function);
}

jlong Java_org_o42a_backend_llvm_code_LLFunction_createFunction(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen,
		jlong funcTypePtr,
		jboolean exported) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	FunctionType *type = from_ptr<FunctionType>(funcTypePtr);
	GlobalValue::LinkageTypes linkageType =
			exported
			? GlobalValue::ExternalLinkage : GlobalValue::PrivateLinkage;
	Function *function = Function::Create(
			type,
			linkageType,
			StringRef(from_ptr<char>(id), idLen),
			module);

	function->setDoesNotThrow();

	return to_ptr<Value>(function);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLFunction_arg(
		JNIEnv *,
		jclass,
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

	return to_ptr<Value>(value);
}

jboolean Java_org_o42a_backend_llvm_code_LLFunction_validate(
		JNIEnv *,
		jclass,
		jlong functionPtr) {

	Function *function = from_ptr<Function>(functionPtr);
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(function->getParent());

	return module->validateFunction(function) ? JNI_TRUE : JNI_FALSE;
}

jlong Java_org_o42a_backend_llvm_code_op_LLFunc_call(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong functionPtr,
		jlongArray argPtrs) {

	IRBuilder<> builder(from_ptr<BasicBlock>(blockPtr));
	if (instrPtr) {
		builder.SetInsertPoint(static_cast<Instruction*>(
				from_ptr<Value>(instrPtr)));
	}
	Value *callee = from_ptr<Value>(functionPtr);
	jArray<jlongArray, jlong> argArray(env, argPtrs);
	const size_t numArgs = argArray.length();
	Value* args[numArgs];

	for (size_t i = 0; i < numArgs; ++i) {
		args[i] = from_ptr<Value>(argArray[i]);
	}

	if (!id) {
		return to_instr_ptr(
				builder.GetInsertBlock(),
				builder.CreateCall(callee, ArrayRef<Value*>(args, numArgs)));
	}

	return to_instr_ptr(builder.GetInsertBlock(),
			builder.CreateCall(
					callee,
					ArrayRef<Value*>(args, numArgs),
					StringRef(from_ptr<char>(id), idLen)));
}
