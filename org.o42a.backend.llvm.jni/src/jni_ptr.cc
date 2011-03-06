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


jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_field(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr,
		jint field) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(
			block,
			"field #" << field << " of " << *pointer << "\n");

	Value *result = builder.CreateConstInBoundsGEP2_32(pointer, 0, field);

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_load(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "load: " << *pointer << "\n");

	Value *result = builder.CreateLoad(pointer);

	ODUMP(result);

	return to_ptr(result);
}

void Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_store(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *value = from_ptr<Value>(valuePtr);

	OCODE(block, "store: " << *pointer << " = " << *value << "\n");

	Value *result = builder.CreateStore(value, pointer);

	ODUMP(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toAny(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toAnyPtr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(pointer, builder.getInt8PtrTy());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toPtr(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toAnyPtr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt8PtrTy()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toInt32(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toInt32ptr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt32Ty()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toInt64(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toInt64ptr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt64Ty()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toFp64(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toFp64ptr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getDoubleTy()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_toRelPtr(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "toRelPtr: " << *pointer << "\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt32Ty()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_castStructTo(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr,
		jlong typePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	OCODE(block, "cast (" << *pointer << ") to (" << *type->get() << ")\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			type->get()->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_castFuncTo(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr,
		jlong funcTypePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(funcTypePtr);

	OCODE(block, "cast (" << *pointer << ") to (" << *type << ")\n");

	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo());

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMPtrOp_isNull(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong pointerPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *pointer = from_ptr<Value>(pointerPtr);

	OCODE(block, "isNull: " << *pointer << "\n");

	Value *result = builder.CreateICmpEQ(
			pointer,
			Constant::getNullValue(pointer->getType()));

	ODUMP(result);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMRelOp_offsetBy(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong fromPtr,
		jlong byPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	IRBuilder<> builder(block);
	Value *from = from_ptr<Value>(fromPtr);
	Value *by = from_ptr<Value>(byPtr);

	OCODE(block, "offset from (" << *from << ") by (" << *by << ")\n");

	const Type *int64ty = builder.getInt64Ty();
	Value *origin = builder.CreatePtrToInt(from, int64ty);
	Value *offset = builder.CreateIntCast(by, int64ty, true);
	Value *position = builder.CreateAdd(origin, offset);
	Value *result = builder.CreateIntToPtr(position, builder.getInt8PtrTy());

	ODUMP(result);

	return to_ptr(result);
}
