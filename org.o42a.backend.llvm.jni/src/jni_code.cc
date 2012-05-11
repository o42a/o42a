/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010-2012 Ruslan Lopatin

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
#include "llvm/Target/TargetData.h"

using namespace llvm;


#define MAKE_BUILDER \
		IRBuilder<> builder(from_ptr<BasicBlock>(blockPtr));\
		if (instrPtr) builder.SetInsertPoint(\
			static_cast<Instruction*>(from_ptr<Value>(instrPtr)))

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

	return to_ptr<BasicBlock>(block);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_stackSave(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr) {

	MAKE_BUILDER;
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(
					builder.GetInsertBlock()->getParent()->getParent());
	Value *stackState = builder.CreateCall(module->getStackSaveFunc(), "stack");

	return to_instr_ptr(stackState);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_stackRestore(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong stackPtr) {

	MAKE_BUILDER;
	o42ac::BackendModule *module =
			static_cast<o42ac::BackendModule*>(
					builder.GetInsertBlock()->getParent()->getParent());
	Value *stackState = from_ptr<Value>(stackPtr);

	return to_ptr<Value>(builder.CreateCall(
			module->getStackRestoreFunc(),
			stackState));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_go(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong targetPtr) {

	MAKE_BUILDER;
	BasicBlock *target = from_ptr<BasicBlock>(targetPtr);

	return to_instr_ptr(builder.CreateBr(target));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_choose(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong conditionPtr,
		jlong truePtr,
		jlong falsePtr) {

	MAKE_BUILDER;
	Value *condition = from_ptr<Value>(conditionPtr);
	BasicBlock *trueBlock = from_ptr<BasicBlock>(truePtr);
	BasicBlock *falseBlock = from_ptr<BasicBlock>(falsePtr);

	return to_instr_ptr(builder.CreateCondBr(condition, trueBlock, falseBlock));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int8(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jbyte value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt8Ty(module->getContext()),
			value,
			true);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int16(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jshort value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt16Ty(module->getContext()),
			value,
			true);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jint value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt32Ty(module->getContext()),
			value,
			true);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_int64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt64Ty(module->getContext()),
			value,
			true);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_fp32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jfloat value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantFP::get(
			Type::getFloatTy(module->getContext()),
			value);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_fp64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jdouble value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantFP::get(
			Type::getDoubleTy(module->getContext()),
			value);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_bool(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jboolean value) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result = ConstantInt::get(
			Type::getInt1Ty(module->getContext()),
			value);

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_nullPtr(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	Constant *result =
			Constant::getNullValue(Type::getInt8PtrTy(module->getContext()));

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_nullStructPtr(
		JNIEnv *,
		jclass,
		jlong typePtr) {

	Type *type = from_ptr<Type>(typePtr);
	Constant *result = Constant::getNullValue(type->getPointerTo());

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_nullFuncPtr(
		JNIEnv *,
		jclass,
		jlong funcTypePtr) {

	Type *type = from_ptr<Type>(funcTypePtr);
	Constant *result = Constant::getNullValue(type->getPointerTo());

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocatePtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen) {

	MAKE_BUILDER;
	o42ac::BackendModule *module = static_cast<o42ac::BackendModule*>(
			builder.GetInsertBlock()->getParent()->getParent());
	AllocaInst *result = builder.CreateAlloca(
			builder.getInt8PtrTy(),
			0,
			StringRef(from_ptr<char>(id), idLen));

	result->setAlignment(module->getTargetData().getPointerPrefAlignment());

	return to_instr_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocateStructPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong typePtr) {

	MAKE_BUILDER;
	o42ac::BackendModule *module = static_cast<o42ac::BackendModule*>(
			builder.GetInsertBlock()->getParent()->getParent());
	Type *type = from_ptr<Type>(typePtr);
	AllocaInst *result = builder.CreateAlloca(
			type->getPointerTo(),
			0,
			StringRef(from_ptr<char>(id), idLen));

	result->setAlignment(module->getTargetData().getPointerPrefAlignment());

	return to_instr_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_allocateStruct(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong typePtr) {

	MAKE_BUILDER;
	o42ac::BackendModule *module = static_cast<o42ac::BackendModule*>(
			builder.GetInsertBlock()->getParent()->getParent());
	Type *type = from_ptr<Type>(typePtr);
	AllocaInst *result = builder.CreateAlloca(
			type,
			0,
			StringRef(from_ptr<char>(id), idLen));

	result->setAlignment(module->getTargetData().getPrefTypeAlignment(type));

	return to_instr_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_phi2(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong block1ptr,
		jlong value1ptr,
		jlong block2ptr,
		jlong value2ptr) {

	MAKE_BUILDER;
	Value *value1 = from_ptr<Value>(value1ptr);
	BasicBlock *block1 = from_ptr<BasicBlock>(block1ptr);
	Value *value2 = from_ptr<Value>(value2ptr);
	BasicBlock *block2 = from_ptr<BasicBlock>(block2ptr);
	PHINode *phi = builder.CreatePHI(
			value1->getType(),
			2,
			StringRef(from_ptr<char>(id), idLen));

	phi->addIncoming(value1, block1);
	phi->addIncoming(value2, block2);

	return to_instr_ptr(phi);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_acquireBarrier(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr) {
	MAKE_BUILDER;
	return to_instr_ptr(builder.CreateFence(Acquire));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_releaseBarrier(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr) {
	MAKE_BUILDER;
	return to_instr_ptr(builder.CreateFence(Release));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_fullBarrier(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr) {
	MAKE_BUILDER;
	return to_instr_ptr(builder.CreateFence(SequentiallyConsistent));
}

jlong Java_org_o42a_backend_llvm_code_LLCode_select(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong conditionPtr,
		jlong truePtr,
		jlong falsePtr) {

	MAKE_BUILDER;
	Value *condition = from_ptr<Value>(conditionPtr);
	Value *value1 = from_ptr<Value>(truePtr);
	Value *value2 = from_ptr<Value>(falsePtr);
	Value *result = builder.CreateSelect(
			condition,
			value1,
			value2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLCode_returnVoid(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr) {

	MAKE_BUILDER;

	return to_instr_ptr(builder.CreateRetVoid());
}

jlong Java_org_o42a_backend_llvm_code_LLCode_returnValue(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong resultPtr) {

	MAKE_BUILDER;
	Value *result = from_ptr<Value>(resultPtr);

	return to_instr_ptr(builder.CreateRet(result));
}
