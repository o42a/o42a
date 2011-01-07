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
#include "jni_fp.h"

#include "o42a/llvm/util.h"

#include "llvm/BasicBlock.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_neg(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFNeg(value);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_add(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFAdd(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_sub(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFSub(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_mul(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFMul(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_div(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFDiv(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_rem(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFRem(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_eq(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOEQ(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_ne(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpONE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_gt(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOGT(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_ge(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOGE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_lt(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOLT(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_le(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOLE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_fp64toInt32(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFPToSI(value, builder.getInt32Ty());

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMFp64op_fp64toInt64(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFPToSI(value, builder.getInt64Ty());

	return to_ptr(result);
}
