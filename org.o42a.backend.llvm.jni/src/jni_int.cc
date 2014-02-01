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
#include "jni_int.h"

#include "o42ac/llvm/util.h"

#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Value.h"

using namespace llvm;


#define MAKE_BUILDER \
		IRBuilder<> builder(from_ptr<BasicBlock>(blockPtr));\
		if (instrPtr) builder.SetInsertPoint(\
			static_cast<Instruction*>(from_ptr<Value>(instrPtr)))

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_shl(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr,
		jlong numBitsPtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	Value *result = builder.CreateShl(
			value,
			numBits,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lshr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr,
		jlong numBitsPtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	Value *result = builder.CreateLShr(
			value,
			numBits,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ashr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr,
		jlong numBitsPtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *numBits = from_ptr<Value>(numBitsPtr);
	Value *result = builder.CreateAShr(
			value,
			numBits,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_and(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateAnd(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_or(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateOr(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_xor(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateXor(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_neg(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *result = builder.CreateNeg(
			value,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_add(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateAdd(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_sub(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateSub(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_mul(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateMul(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_div(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateSDiv(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_rem(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateSRem(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong JNICALL Java_org_o42a_backend_llvm_code_op_IntLLOp_eq(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpEQ(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ne(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpNE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_gt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpSGT(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_ge(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpSGE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpSLT(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_le(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong op1ptr,
		jlong op2ptr) {

	MAKE_BUILDER;
	Value *op1 = from_ptr<Value>(op1ptr);
	Value *op2 = from_ptr<Value>(op2ptr);
	Value *result = builder.CreateICmpSLE(
			op1,
			op2,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_int2int(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr,
		jbyte intBits) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *result = builder.CreateIntCast(
			value,
			IntegerType::get(builder.getContext(), intBits),
			true,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_intToFp32(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *result = builder.CreateSIToFP(
			value,
			builder.getFloatTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_intToFp64(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *result = builder.CreateSIToFP(
			value,
			builder.getDoubleTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_IntLLOp_lowestBit(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong valuePtr) {

	MAKE_BUILDER;
	Value *value = from_ptr<Value>(valuePtr);
	Value *result = builder.CreateIntCast(
			value,
			builder.getInt1Ty(),
			false,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}
