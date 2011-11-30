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
#include "jni_ptr.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/BasicBlock.h"
#include "llvm/Function.h"
#include "llvm/Module.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"


jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_field(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jint field) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreateConstInBoundsGEP2_32(
			pointer,
			0,
			field,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_load(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreateLoad(
			pointer,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

void Java_org_o42a_backend_llvm_code_op_PtrLLOp_store(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong pointerPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *value = from_ptr<Value>(valuePtr);

	builder.CreateStore(value, pointer);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toAny(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt8PtrTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt8PtrTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toInt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jbyte intBits) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			IntegerType::get(block->getContext(), intBits)->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toFp32(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getFloatTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toFp64(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getDoubleTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toRelPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt32Ty()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_castStructTo(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong typePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(typePtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_castFuncTo(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong funcTypePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(funcTypePtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_isNull(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreateICmpEQ(
			pointer,
			Constant::getNullValue(pointer->getType()),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_offset(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong indexPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *index = from_ptr<Value>(indexPtr);
	Value *result = builder.CreateGEP(
			pointer,
			index,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_RelLLOp_offsetBy(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong fromPtr,
		jlong byPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *from = from_ptr<Value>(fromPtr);
	Value *by = from_ptr<Value>(byPtr);
	Type *int64ty = builder.getInt64Ty();
	Value *origin = builder.CreatePtrToInt(from, int64ty);
	Value *offset = builder.CreateIntCast(by, int64ty, true);
	Value *position = builder.CreateAdd(origin, offset);
	Value *result = builder.CreateIntToPtr(
			position,
			builder.getInt8PtrTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}
