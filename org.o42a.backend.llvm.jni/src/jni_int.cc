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
#include "jni_int.h"

#include "o42ac/llvm/util.h"

#include "llvm/BasicBlock.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_lshr(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr,
		jlong numBitsPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateLShr(value, numBits);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_ashr(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr,
		jlong numBitsPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAShr(value, numBits);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_and(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAnd(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_or(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateOr(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_xor(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateXor(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_neg(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateNeg(value);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_add(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAdd(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_sub(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSub(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_mul(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateMul(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_div(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSDiv(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_rem(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSRem(op1, op2);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_op_LLVMIntOp_eq(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpEQ(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_ne(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpNE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_gt(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSGT(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_ge(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSGE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_lt(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSLT(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_le(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSLE(op1, op2);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_int2int(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr,
		jbyte intBits) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateIntCast(
			value,
			IntegerType::get(block->getContext(), intBits),
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_intToFp32(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSIToFP(value, builder.getFloatTy());

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_intToFp64(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSIToFP(value, builder.getDoubleTy());

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_LLVMIntOp_lowestBit(
		JNIEnv *env,
		jclass cls,
		jlong blockPtr,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateIntCast(value, builder.getInt1Ty(), false);

	return to_ptr(result);
}
