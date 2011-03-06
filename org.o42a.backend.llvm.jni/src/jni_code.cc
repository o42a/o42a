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


jlong Java_org_o42a_backend_llvm_code_LLVMCode_createBlock(
		JNIEnv *env,
		jclass cls,
		jlong functionPtr,
		jstring id) {

	Function *function = from_ptr<Function>(functionPtr);
	jStringRef blockId(env, id);

	OTRACE("createBlock: " << blockId << " (" << function->getName() << ")\n");

	BasicBlock *block =
			BasicBlock::Create(function->getContext(), blockId, function);

	return to_ptr(block);
}

void Java_org_o42a_backend_llvm_code_LLVMCode_go(
		JNIEnv *env,
		jclass cls,
		jlong sourcePtr,
		jlong targetPtr) {

	BasicBlock *source = from_ptr<BasicBlock>(sourcePtr);
	BasicBlock *target = from_ptr<BasicBlock>(targetPtr);
	IRBuilder<> builder(source);

	OCODE(source, "go: " << target->getName() << "\n");

	Value *result = builder.CreateBr(target);

	ODUMP(result);
}

void Java_org_o42a_backend_llvm_code_LLVMCode_choose(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong conditionPtr,
		jlong truePtr,
		jlong falsePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *condition = from_ptr<Value>(conditionPtr);
	BasicBlock *trueBlock = from_ptr<BasicBlock>(truePtr);
	BasicBlock *falseBlock = from_ptr<BasicBlock>(falsePtr);
	IRBuilder<> builder(block);

	OCODE(block, "choose: (" << *condition
			<< ") ? " << trueBlock->getName()
			<< " : " << falseBlock->getName() << "\n");

	Value *result = builder.CreateCondBr(condition, trueBlock, falseBlock);

	ODUMP(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_int32(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jint value) {

	Module *module = from_ptr<Module>(modulePtr);

	OTRACE("int32: " << value << "\n");

	Constant *result = ConstantInt::get(
			Type::getInt32Ty(module->getContext()),
			value,
			true);

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_int64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong value) {

	Module *module = from_ptr<Module>(modulePtr);

	OTRACE("int64: " << value << "\n");

	Constant *result = ConstantInt::get(
			Type::getInt64Ty(module->getContext()),
			value,
			true);

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_fp64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jdouble value) {

	Module *module = from_ptr<Module>(modulePtr);

	OTRACE("fp64: " << value << "\n");

	Constant *result = ConstantFP::get(
			Type::getInt64Ty(module->getContext()),
			value);

	ODUMP(result);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_bool(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jboolean value) {

	Module *module = from_ptr<Module>(modulePtr);

	OTRACE("bool: " << (value ? "true" : "false") << "\n");

	Constant *result = ConstantInt::get(
			Type::getInt1Ty(module->getContext()),
			value);

	ODUMP(result);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_nullPtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	Module *module = from_ptr<Module>(modulePtr);

	OTRACE("nullPtr\n");

	Constant *result =
			Constant::getNullValue(Type::getInt8PtrTy(module->getContext()));

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_nullStructPtr(
		JNIEnv *env,
		jclass cls,
		jlong typePtr) {

	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	OTRACE("nullStructPtr: " << *type->get() << "\n");

	Constant *result = Constant::getNullValue(type->get()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_nullFuncPtr(
		JNIEnv *env,
		jclass cls,
		jlong funcTypePtr) {

	Type *type = from_ptr<Type>(funcTypePtr);

	OTRACE("nullFuncPtr: " << *type << "\n");

	Constant *result = Constant::getNullValue(type->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_allocatePtr(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);

	OCODE(block, "allocatePtr\n");

	Value *result = builder.CreateAlloca(builder.getInt8PtrTy());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_allocateStruct(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong typePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	OCODE(block, "allocateStruct: "
			<< block->getParent()->getParent()->getTypeName(type->get())
			<< "\n");

	Value *result = builder.CreateAlloca(type->get());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_LLVMCode_phi(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong block1ptr,
		jlong value1ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *value1 = from_ptr<Value>(value1ptr);
	BasicBlock *block1 = from_ptr<BasicBlock>(block1ptr);

	OCODE(block, "phi: " << block1->getName() << "(" << *value1 << ")\n");

	PHINode *phi = builder.CreatePHI(value1->getType());

	phi->addIncoming(value1, block1);

	ODUMP(phi);

	return to_ptr(phi);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_LLVMCode_phi2(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
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

	OCODE(
			block,
			"phi2: " << block1->getName() << "(" << *value1 << "), "
			<< block2->getName() << "(" << *value2 << ")\n");

	PHINode *phi = builder.CreatePHI(value1->getType());

	phi->addIncoming(value1, block1);
	phi->addIncoming(value2, block2);

	ODUMP(phi);

	return to_ptr(phi);
}

void Java_org_o42a_backend_llvm_code_LLVMCode_returnVoid(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);

	OCODE(block, "returnVoid\n");

	Value *result = builder.CreateRetVoid();

	ODUMP(result);
}

void Java_org_o42a_backend_llvm_code_LLVMCode_returnValue(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong resultPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *result = from_ptr<Value>(resultPtr);
	IRBuilder<> builder(block);

	OCODE(block, "returnValue: " << *result << "\n");

	Value *res = builder.CreateRet(result);

	ODUMP(res);
}
