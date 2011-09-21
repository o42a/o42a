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
#include "llvm/Module.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_shl(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr,
		jlong numBitsPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateShl(value, numBits, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lshr(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr,
		jlong numBitsPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateLShr(value, numBits, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ashr(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr,
		jlong numBitsPtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAShr(value, numBits, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_and(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAnd(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_or(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateOr(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_xor(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateXor(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_neg(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateNeg(value, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_add(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateAdd(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_sub(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSub(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_mul(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateMul(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_div(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSDiv(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_rem(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSRem(op1, op2, name);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_op_IntLLOp_eq(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpEQ(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ne(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpNE(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_gt(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSGT(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ge(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSGE(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lt(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSLT(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_le(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateICmpSLE(op1, op2, name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_int2int(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr,
		jbyte intBits) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateIntCast(
			value,
			IntegerType::get(block->getContext(), intBits),
			true,
			name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_intToFp32(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSIToFP(value, builder.getFloatTy(), name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_intToFp64(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateSIToFP(value, builder.getDoubleTy(), name);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lowestBit(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result =
			builder.CreateIntCast(value, builder.getInt1Ty(), false, name);

	return to_ptr(result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_op_IntLLOp_atomicBinary(
		JNIEnv *env,
		jclass,
		jlong blockPtr,
		jstring id,
		jstring op,
		jlong targetPtr,
		jlong operandPtr,
		jint bits) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	jStringRef name(env, id);
	jStringRef funcName(env, op);
	Value *target = from_ptr<Value>(targetPtr);
	Value *operand = from_ptr<Value>(operandPtr);
	IRBuilder<> builder(block);

	const Type* intType = IntegerType::get(block->getContext(), bits);
	Constant *const func = block->getParent()->getParent()->getOrInsertFunction(
			funcName,
			intType,
			intType->getPointerTo(),
			intType,
			NULL);
	Value *result = builder.CreateCall2(func, target, operand, name);

	return to_ptr(result);
}
