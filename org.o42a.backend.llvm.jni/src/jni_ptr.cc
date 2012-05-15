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
#include "jni_ptr.h"

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

	return to_instr_ptr(result);
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_load(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong id,
		jint idLen,
		jlong pointerPtr,
		jboolean atomic) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);

	if (!atomic) {
		return to_instr_ptr(builder.CreateLoad(
				pointer,
				StringRef(from_ptr<char>(id), idLen)));
	}

	o42ac::BackendModule *module = static_cast<o42ac::BackendModule *>(
			builder.GetInsertBlock()->getParent()->getParent());
	const TargetData &targetData = module->getTargetData();
	Type *storeType = pointer->getType()->getContainedType(0);
	LoadInst *result;

	// Atomic operations support only integers.
	if (storeType->isIntegerTy()) {
		result = builder.CreateLoad(
				pointer,
				StringRef(from_ptr<char>(id), idLen));
	} else {

		IntegerType *intType = IntegerType::get(
				module->getContext(),
				targetData.getTypeSizeInBits(storeType));

		result = builder.CreateLoad(
				builder.CreateBitCast(pointer, intType->getPointerTo()));
	}

	// Guarantee the data loaded one piece.
	result->setAtomic(Monotonic);
	// Atomic operations require alignment.
	result->setAlignment(targetData.getABITypeAlignment(storeType));

	if (storeType->isIntegerTy()) {
		return to_instr_ptr(result);
	}

	return to_instr_ptr(builder.CreateBitCast(
			result,
			storeType,
			StringRef(from_ptr<char>(id), idLen)));
}

jlong Java_org_o42a_backend_llvm_code_op_PtrLLOp_store(
		JNIEnv *,
		jclass,
		jlong blockPtr,
		jlong instrPtr,
		jlong pointerPtr,
		jlong valuePtr,
		jboolean atomic) {

	MAKE_BUILDER;
	Value *pointer = from_ptr<Value>(pointerPtr);
	Value *value = from_ptr<Value>(valuePtr);

	if (!atomic) {
		return to_instr_ptr(builder.CreateStore(value, pointer));
	}

	o42ac::BackendModule *module = static_cast<o42ac::BackendModule *>(
			builder.GetInsertBlock()->getParent()->getParent());
	const TargetData &targetData = module->getTargetData();
	Type *storeType = pointer->getType()->getContainedType(0);
	Value *ptr;
	Value *val;

	// Atomic operations support only integers.
	if (storeType->isIntegerTy()) {
		ptr = pointer;
		val = value;
	} else {

		IntegerType *intType = IntegerType::get(
				module->getContext(),
				targetData.getTypeSizeInBits(storeType));

		ptr = builder.CreateBitCast(pointer, intType->getPointerTo());
		val = builder.CreateBitCast(value, intType);
	}

	StoreInst *result = builder.CreateStore(val, ptr);

	// Guarantee the data stored one piece.
	result->setAtomic(Monotonic);
	// Atomic operations require alignment.
	result->setAlignment(targetData.getABITypeAlignment(storeType));

	return to_instr_ptr(result);
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
	const TargetData &targetData = module->getTargetData();
	Type *storeType = pointer->getType()->getContainedType(0);

	// Atomic operations support only integers.
	if (storeType->isIntegerTy()) {

		AtomicCmpXchgInst *result = builder.CreateAtomicCmpXchg(
				pointer,
				expected,
				value,
				Monotonic);

		result->setName(StringRef(from_ptr<char>(id), idLen));

		return to_instr_ptr(result);
	}

	IntegerType *intType = IntegerType::get(
			module->getContext(),
			targetData.getTypeSizeInBits(storeType));
	AtomicCmpXchgInst *result = builder.CreateAtomicCmpXchg(
			builder.CreateBitCast(pointer, intType->getPointerTo()),
			builder.CreateBitCast(expected, intType->getPointerTo()),
			builder.CreateBitCast(value, intType),
			Monotonic);

	return to_instr_ptr(builder.CreateBitCast(
			result,
			storeType,
			StringRef(from_ptr<char>(id), idLen)));
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
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

	return to_instr_ptr(result);
}
