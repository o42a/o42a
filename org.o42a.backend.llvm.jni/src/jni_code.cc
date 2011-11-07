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
#include "jni_code.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/BasicBlock.h"
#include "llvm/Function.h"
#include "llvm/Module.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_LLCode_createBlock(
		JNIEnv *,
		jclass,
		jlong functionPtr,
		jlong idData,
		jint idLength) {

	Function *function = from_ptr<Function>(functionPtr);
	StringRef blockId(from_ptr<char>(idData), idLength);

	BasicBlock *block =
			BasicBlock::Create(function->getContext(), blockId, function);

	return to_ptr(block);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_stackSave(
		JNIEnv *,
		jclass,
		jlong blockPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(block->getParent()->getParent());
	IRBuilder<> builder(block);
	Value *stackState = builder.CreateCall(module->getStackSaveFunc(), "stack");

	return to_ptr(stackState);
}

void Java_org_o42a_backend_llvm_code_LLCode_stackRestore(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong stackPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(block->getParent()->getParent());
	Value *stackState = from_ptr<Value>(stackPtr);
	IRBuilder<> builder(block);

	builder.CreateCall(module->getStackRestoreFunc(), stackState);
}

void Java_org_o42a_backend_llvm_code_LLCode_go(
		JNIEnv *,
		jclass,
		jlong sourcePtr,
		jlong targetPtr) {

	BasicBlock *source = from_ptr<BasicBlock>(sourcePtr);
	BasicBlock *target = from_ptr<BasicBlock>(targetPtr);
	IRBuilder<> builder(source);

	builder.CreateBr(target);
}

void Java_org_o42a_backend_llvm_code_LLCode_choose(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong conditionPtr,
		jlong truePtr,
		jlong falsePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *condition = from_ptr<Value>(conditionPtr);
	BasicBlock *trueBlock = from_ptr<BasicBlock>(truePtr);
	BasicBlock *falseBlock = from_ptr<BasicBlock>(falsePtr);
	IRBuilder<> builder(block);

	builder.CreateCondBr(condition, trueBlock, falseBlock);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_blockAddress(
		JNIEnv *,
		jclass,
		jlong,
		jlong targetPtr) {
	return to_ptr(BlockAddress::get(from_ptr<BasicBlock>(targetPtr)));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_indirectbr(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jlong targetPtr,
		jlongArray targetBlockPtrs) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *target = from_ptr<Value>(targetPtr);
	jInt64Array targetBlocks(env, targetBlockPtrs);
	IRBuilder<> builder(block);
	size_t len = targetBlocks.length();
	IndirectBrInst *inst = builder.CreateIndirectBr(target, len);

	for (size_t i = 0; i < len; ++i) {
		inst->addDestination(from_ptr<BasicBlock>(targetBlocks[i]));
	}

	return to_ptr(inst);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int8(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jbyte value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt8Ty(module->getContext()),
			value,
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int16(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jshort value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt16Ty(module->getContext()),
			value,
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jint value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt32Ty(module->getContext()),
			value,
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt64Ty(module->getContext()),
			value,
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_fp32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jfloat value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantFP::get(
			Type::getFloatTy(module->getContext()),
			value);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_fp64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jdouble value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantFP::get(
			Type::getDoubleTy(module->getContext()),
			value);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_bool(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jboolean value) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt1Ty(module->getContext()),
			value);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_nullPtr(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *result =
			Constant::getNullValue(Type::getInt8PtrTy(module->getContext()));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_nullStructPtr(
		JNIEnv *,
		jclass,
		jlong typePtr) {

	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	Constant *result = Constant::getNullValue(type->get()->getPointerTo());

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_nullFuncPtr(
		JNIEnv *,
		jclass,
		jlong funcTypePtr) {

	Type *type = from_ptr<Type>(funcTypePtr);
	Constant *result = Constant::getNullValue(type->getPointerTo());

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocatePtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAlloca(
			builder.getInt8PtrTy(),
			0,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocateStructPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong typePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	Value *result = builder.CreateAlloca(
			type->get()->getPointerTo(),
			0,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocateStruct(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong typePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	Value *result = builder.CreateAlloca(
			type->get(),
			0,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_phi2(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong block1ptr,
		jlong value1ptr,
		jlong block2ptr,
		jlong value2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *value1 = from_ptr<Value>(value1ptr);
	BasicBlock *block1 = from_ptr<BasicBlock>(block1ptr);
	Value *value2 = from_ptr<Value>(value2ptr);
	BasicBlock *block2 = from_ptr<BasicBlock>(block2ptr);
	PHINode *phi = builder.CreatePHI(
			value1->getType(),
			StringRef(from_ptr<char>(id), idLen));

	phi->addIncoming(value1, block1);
	phi->addIncoming(value2, block2);

	return to_ptr(phi);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLCode_phiN(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlongArray blockAndValuePtrs) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	jInt64Array blocksAndValues(env, blockAndValuePtrs);
	size_t len = blocksAndValues.length();
	PHINode *phi = builder.CreatePHI(
			from_ptr<Value>(blocksAndValues[1])->getType(),
			StringRef(from_ptr<char>(id), idLen));

	for (size_t i = 0; i < len; i += 2) {
		phi->addIncoming(
				from_ptr<Value>(blocksAndValues[i + 1]),
				from_ptr<BasicBlock>(blocksAndValues[i]));
	}

	return to_ptr(phi);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_select(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong conditionPtr,
		jlong truePtr,
		jlong falsePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *condition = from_ptr<Value>(conditionPtr);
	Value *value1 = from_ptr<Value>(truePtr);
	Value *value2 = from_ptr<Value>(falsePtr);
	Value *result = builder.CreateSelect(
			condition,
			value1,
			value2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

void Java_org_o42a_backend_llvm_code_LLCode_returnVoid(
		JNIEnv *,
		jclass,
		jlong blockPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);

	builder.CreateRetVoid();
}

void Java_org_o42a_backend_llvm_code_LLCode_returnValue(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong resultPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *result = from_ptr<Value>(resultPtr);
	IRBuilder<> builder(block);

	builder.CreateRet(result);
}
