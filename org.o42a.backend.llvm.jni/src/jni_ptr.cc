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
#include "jni_ptr.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/DataLayout.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Value.h"
#include "llvm/Support/FileSystem.h"

using namespace llvm;


#define MAKE_BUILDER \
		IRBuilder<> builder(from_ptr<BasicBlock>(blockPtr));\
		if (instrPtr) builder.SetInsertPoint(\
			static_cast<Instruction*>(from_ptr<Value>(instrPtr)))

jlong Java_org_o42a_backend_llvm_code_LLVMCodeBackend_codeToAny(
		JNIEnv *,
		jclass,
		jlong blockPtr) {
	return to_ptr<Value>(BlockAddress::get(from_ptr<BasicBlock>(blockPtr)));
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_field(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jint field) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreateConstInBoundsGEP2_32(
			pointer,
			0,
			field,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

static const AtomicOrdering LOAD_ORDERINGS[] = {
	NotAtomic,
	Monotonic,
	Acquire,
	SequentiallyConsistent,
};

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_load(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jint atomicity) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);

	if (!atomicity) {
		return to_instr_ptr(
				builder.GetInsertBlock(),
				builder.CreateLoad(
						pointer,
						StringRef(from_ptr<char>(id), idLen)));
	}

	o42ac::BackendModule *module = static_cast<o42ac::BackendModule *>(
			builder.GetInsertBlock()->getParent()->getParent());
	const DataLayout *dataLayout = module->getTargetDataLayout();
	Type *storeType = pointer->getType()->getContainedType(0);
	LoadInst *result;

	// Atomic operations support only integers.
	if (!storeType->isPointerTy()) {
		result = builder.CreateLoad(
				pointer,
				StringRef(from_ptr<char>(id), idLen));
	} else {

		IntegerType *intType = IntegerType::get(
				module->getContext(),
				dataLayout->getTypeSizeInBits(storeType));

		result = builder.CreateLoad(
				builder.CreatePointerCast(pointer, intType->getPointerTo()));
	}

	// Guarantee the data loaded one piece.
	result->setAtomic(LOAD_ORDERINGS[atomicity]);
	// Atomic operations require alignment.
	result->setAlignment(dataLayout->getTypeStoreSize(storeType));

	if (!storeType->isPointerTy()) {
		return to_instr_ptr(builder.GetInsertBlock(), result);
	}

	return to_instr_ptr(
			builder.GetInsertBlock(),
			builder.CreateIntToPtr(
					result,
					storeType,
					StringRef(from_ptr<char>(id), idLen)));
}

static const AtomicOrdering STORE_ORDERINGS[] = {
	NotAtomic,
	Monotonic,
	Release,
	SequentiallyConsistent,
};

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_store(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong pointerPtr,
		jlong valuePtr,
		jint atomicity) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *value = from_ptr<Value>(valuePtr);

	if (!atomicity) {
		return to_instr_ptr(
				builder.GetInsertBlock(),
				builder.CreateStore(value, pointer));
	}

	o42ac::BackendModule *module = static_cast<o42ac::BackendModule *>(
			builder.GetInsertBlock()->getParent()->getParent());
	const DataLayout *dataLayout = module->getTargetDataLayout();
	Type *storeType = pointer->getType()->getContainedType(0);
	Value *ptr;
	Value *val;

	// Atomic operations support only integers.
	if (!storeType->isPointerTy()) {
		ptr = pointer;
		val = value;
	} else {

		IntegerType *intType = IntegerType::get(
				module->getContext(),
				dataLayout->getTypeSizeInBits(storeType));

		ptr = builder.CreatePointerCast(pointer, intType->getPointerTo());
		val = builder.CreatePtrToInt(value, intType);
	}

	StoreInst *result = builder.CreateStore(val, ptr);

	// Guarantee the data stored one piece.
	result->setAtomic(STORE_ORDERINGS[atomicity]);
	// Atomic operations require alignment.
	result->setAlignment(dataLayout->getTypeStoreSize(storeType));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_testAndSet(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong expectedPtr,
		jlong valuePtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *expected = from_ptr<Value>(expectedPtr);
	Value *value = from_ptr<Value>(valuePtr);

	o42ac::BackendModule *module = static_cast<o42ac::BackendModule *>(
			builder.GetInsertBlock()->getParent()->getParent());
	const DataLayout *dataLayout = module->getTargetDataLayout();
	Type *storeType = pointer->getType()->getContainedType(0);

	// Atomic operations support only integers.
	if (!storeType->isPointerTy()) {

		AtomicCmpXchgInst *result = builder.CreateAtomicCmpXchg(
				pointer,
				expected,
				value,
				Monotonic,
				Monotonic);

		result->setName(StringRef(from_ptr<char>(id), idLen));

		return to_instr_ptr(builder.GetInsertBlock(), result);
	}

	IntegerType *intType = IntegerType::get(
			module->getContext(),
			dataLayout->getTypeSizeInBits(storeType));
	AtomicCmpXchgInst *result = builder.CreateAtomicCmpXchg(
			builder.CreatePointerCast(pointer, intType->getPointerTo()),
			builder.CreatePtrToInt(expected, intType),
			builder.CreatePtrToInt(value, intType),
			Monotonic,
			Monotonic);

	return to_instr_ptr(
			builder.GetInsertBlock(),
			builder.CreateIntToPtr(
					result,
					storeType,
					StringRef(from_ptr<char>(id), idLen)));
}

static const AtomicRMWInst::BinOp RMW_KINDS[] = {
	AtomicRMWInst::Xchg,
	AtomicRMWInst::Add,
	AtomicRMWInst::Sub,
	AtomicRMWInst::Or,
	AtomicRMWInst::And,
	AtomicRMWInst::Xor,
	AtomicRMWInst::Nand
};

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_atomicRMW(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jint rmwKind,
		jlong operandPtr) {

	MAKE_BUILDER;
	Value* pointer = from_ptr < Value > (pointerPtr);
	Value* operand = from_ptr < Value > (operandPtr);
	AtomicRMWInst::BinOp op = RMW_KINDS[rmwKind];
	AtomicRMWInst* result =
			builder.CreateAtomicRMW(op, pointer, operand, Monotonic);

	result->setName(StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);

}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toAny(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt8PtrTy(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt8PtrTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toInt(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jbyte intBits) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			IntegerType::get(builder.getContext(), intBits)->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toFp32(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getFloatTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toFp64(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getDoubleTy()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toRelPtr(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			builder.getInt32Ty()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_castStructTo(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong typePtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(typePtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_toStructRec(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong typePtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(typePtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo()->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_castFuncTo(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong funcTypePtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Type *type = from_ptr<Type>(funcTypePtr);
	Value *result = builder.CreatePointerCast(
			pointer,
			type->getPointerTo(),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_isNull(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *result = builder.CreateICmpEQ(
			pointer,
			Constant::getNullValue(pointer->getType()),
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_offset(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jlong indexPtr) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *index = from_ptr<Value>(indexPtr);
	Value *result = builder.CreateGEP(
			pointer,
			index,
			StringRef(from_ptr<char>(id), idLen));

	return to_instr_ptr(builder.GetInsertBlock(), result);
}

jlong Java_org_o42a_backend_llvm_code_op_RelLLOp_offsetBy(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong fromPtr,
		jlong byPtr) {

	MAKE_BUILDER;
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

	return to_instr_ptr(builder.GetInsertBlock(), result);
}
