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
#include "jni_fp.h"

#include "o42ac/llvm/util.h"

#include "llvm/BasicBlock.h"
#include "llvm/Value.h"
#include "llvm/Support/IRBuilder.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_neg(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFNeg(
			value,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_add(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFAdd(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_sub(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFSub(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_mul(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFMul(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_div(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFDiv(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_rem(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFRem(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_eq(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOEQ(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_ne(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpONE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_gt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOGT(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_ge(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOGE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_lt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOLT(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_le(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFCmpOLE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_fp2int(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong valuePtr,
		jbyte intBits) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFPToSI(
			value,
			IntegerType::get(block->getContext(), intBits),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_fp2fp32(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFPCast(
			value,
			builder.getFloatTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_FpLLOp_fp2fp64(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	BasicBlock *block = from_ptr<BasicBlock>(blockPtr);
	Value *value = from_ptr<Value>(valuePtr);
	IRBuilder<> builder(block);
	Value *result = builder.CreateFPCast(
			value,
			builder.getDoubleTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr(result);
}
