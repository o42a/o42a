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
#include "jni_data.h"

#include <pthread.h>

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/IR/Constants.h"
#include "llvm/IR/DataLayout.h"
#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/Module.h"

#include "o42a/memory/gc.h"

using namespace llvm;


extern o42a_layout_t o42a_layout(uint8_t, size_t);

jint Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadLayout(
		JNIEnv *,
		jclass) {
	return O42A_LAYOUT(pthread_t);
}

jint Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadMutexLayout(
		JNIEnv *,
		jclass) {
	return O42A_LAYOUT(pthread_mutex_t);
}

jint Java_org_o42a_backend_llvm_data_SystemTypeInfo_pthreadCondLayout(
		JNIEnv *,
		jclass) {
	return O42A_LAYOUT(pthread_cond_t);
}

jint Java_org_o42a_backend_llvm_data_SystemTypeInfo_gcBlockPadding(
		JNIEnv *,
		jclass) {
	return sizeof(struct _o42a_gc_block) - sizeof(o42a_gc_block_t);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_binaryConstant(
		JNIEnv *env,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen,
		jbyteArray data,
		jint start,
		jint end,
		jboolean isConstant) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	jByteArray array(env, data);
	size_t length = end - start;
	IntegerType *const itemType = Type::getInt8Ty(module->getContext());
	ArrayType *type = ArrayType::get(itemType, length);
	GlobalVariable *const global =
			cast<GlobalVariable>(module->getOrInsertGlobal(
					StringRef(from_ptr<char>(id), idLen),
					type));

	global->setConstant(isConstant);
	global->setLinkage(GlobalValue::PrivateLinkage);

	Constant *values[length];

	for (int i = start; i < end; ++i) {
		values[i - start] = ConstantInt::get(itemType, array[i]);
	}

	global->setInitializer(ConstantArray::get(
			type,
			ArrayRef<Constant*>(values, length)));

	Constant *result = ConstantExpr::getPointerCast(
			global,
			Type::getInt8PtrTy(module->getContext()));

	return to_ptr<Value>(result);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createType(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	StructType *type = StructType::create(
			module->getContext(),
			StringRef(from_ptr<char>(id), idLen));

	return to_ptr<StructType>(type);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createTypeData(
		JNIEnv *,
		jclass,
		jlong) {

	std::vector<Type*> *result = new std::vector<Type*>();

	return to_ptr<std::vector<Type*> >(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStruct(
		JNIEnv *,
		jclass,
		jlong,
		jlong enclosingPtr,
		jlong typePtr) {

	std::vector<Type*> *enclosing = from_ptr<std::vector<Type*> >(enclosingPtr);
	Type *type = from_ptr<Type>(typePtr);

	enclosing->push_back(type);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateGlobal(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen,
		jlong typePtr,
		jboolean constant,
		jboolean exported) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	Type *type = from_ptr<Type>(typePtr);
	GlobalVariable *global = cast<GlobalVariable>(module->getOrInsertGlobal(
			StringRef(from_ptr<char>(id), idLen),
			type));

	global->setConstant(constant);
	global->setLinkage(
			exported
			? GlobalValue::ExternalLinkage : GlobalValue::PrivateLinkage);

	return to_ptr<Value>(global);
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_refineType(
		JNIEnv *,
		jclass,
		jlong typePtr,
		jlong typeDataPtr,
		jboolean packed) {

	StructType *type = from_ptr<StructType>(typePtr);
	std::vector<Type*> *typeData = from_ptr<std::vector<Type*> >(typeDataPtr);

	type->setBody(*typeData, packed);

	delete typeData;
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateInt(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong enclosingPtr,
		jint numBits) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(IntegerType::get(module->getContext(), numBits));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong enclosingPtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getFloatTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong enclosingPtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getDoubleTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp128(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong enclosingPtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getFP128Ty(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFuncPtr(
		JNIEnv *,
		jclass,
		jlong enclosingPtr,
		jlong funcTypePtr) {

	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);
	FunctionType *type = from_ptr<FunctionType>(funcTypePtr);

	enclosing->push_back(type->getPointerTo());
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocatePtr(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong enclosingPtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<Type*> *enclosing = from_ptr<std::vector<Type*> >(enclosingPtr);

	enclosing->push_back(Type::getInt8PtrTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStructPtr(
		JNIEnv *,
		jclass,
		jlong enclosingPtr,
		jlong typePtr) {

	std::vector<Type*> *enclosing =
			from_ptr<std::vector<Type*> >(enclosingPtr);
	Type *type = from_ptr<Type>(typePtr);

	enclosing->push_back(type->getPointerTo());
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_externStruct(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong id,
		jint idLen,
		jlong typePtr,
		jboolean constant) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	Type *type = from_ptr<Type>(typePtr);
	GlobalVariable *global = cast<GlobalVariable>(module->getOrInsertGlobal(
			StringRef(from_ptr<char>(id), idLen),
			type));

	global->setConstant(constant);
	global->setLinkage(GlobalValue::ExternalLinkage);

	return to_ptr<Value>(global);
}

static inline jint typeLayout(
		const o42ac::BackendModule *module,
		Type *type) {

	const DataLayout *dataLayout = module->getTargetDataLayout();

	return o42a_layout(
			dataLayout->getABITypeAlignment(type),
			dataLayout->getTypeStoreSize(type));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_intLayout(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jint intBits) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, IntegerType::get(module->getContext(), intBits));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp32layout(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getFloatTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp64layout(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getDoubleTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp128layout(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getFP128Ty(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_ptrLayout(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt8PtrTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_relPtrLayout(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt32Ty(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_structLayout(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong typePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	Type *type = from_ptr<Type>(typePtr);

	return typeLayout(module, type);
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_dumpStructLayout(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong typePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	const DataLayout *dataLayout = module->getTargetDataLayout();
	StructType *type = from_ptr<StructType>(typePtr);
	const StructLayout *layout = dataLayout->getStructLayout(type);
	const unsigned numElements = type->getNumElements();

	for (unsigned i = 0; i < numElements; ++i) {
		errs() << layout->getElementOffset(i) << ": " << *type->getElementType(i) << "\n";
	}
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullPtr(
		JNIEnv *,
		jclass,
		jlong modulePtr) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return to_ptr<Value>(Constant::getNullValue(
			Type::getInt8PtrTy(module->getContext())));
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullStructPtr(
		JNIEnv *,
		jclass,
		jlong typePtr) {

	Type *type = from_ptr<Type>(typePtr);

	return to_ptr<Value>(Constant::getNullValue(type->getPointerTo()));
}

jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullFuncPtr(
		JNIEnv *,
		jclass,
		jlong funcTypePtr) {

	Type *type = from_ptr<Type>(funcTypePtr);

	return to_ptr<Value>(Constant::getNullValue(type->getPointerTo()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_createStruct(
		JNIEnv *,
		jclass,
		jint size) {

	std::vector<Constant*> *result = new std::vector<Constant*>();

	result->reserve(size);

	return to_ptr<std::vector<Constant*> >(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeInt(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong structPtr,
		jlong value,
		jint numBits) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	IntegerType* type = IntegerType::get(module->getContext(), numBits);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	ConstantInt *result = ConstantInt::getSigned(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writePtrAsInt64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong structPtr,
		jlong value) {

	Constant *ptr = from_ptr<Constant>(value);
	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<Constant*> *data =
				from_ptr<std::vector<Constant*> >(structPtr);
	IntegerType *int64type = Type::getInt64Ty(module->getContext());
	uint64_t ptrSize =
			module->getTargetDataLayout()->getTypeSizeInBits(ptr->getType());

	// Bit-cast pointer to integer.
	Constant *result = ConstantExpr::getPtrToInt(
			ptr,
			IntegerType::get(module->getContext(), ptrSize));
	uint64_t sizeDiff = 64 - ptrSize;

	if (sizeDiff) {
	    // Extend to 64-bit integer if necessary.
		result = ConstantExpr::getIntegerCast(result, int64type, false);
		if (module->getTargetDataLayout()->isBigEndian()) {
			// If the target is big-endian, move the pointer data so that
			// it become available at int64 address.
			result = ConstantExpr::getExactLShr(
					result,
					ConstantInt::get(int64type, sizeDiff));
		}
	}

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFp32(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong structPtr,
		jfloat value) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	Type* type = Type::getFloatTy(module->getContext());
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantFP::get(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFp64(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong structPtr,
		jdouble value) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	Type* type = Type::getDoubleTy(module->getContext());
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantFP::get(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeNativePtr(
		JNIEnv *,
		jclass,
		jlong modulePtr,
		jlong structPtr,
		jlong value) {

	o42ac::BackendModule *const module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	std::vector<Constant*> *data =
				from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantExpr::getPointerCast(
			from_ptr<Constant>(value),
			Type::getInt8PtrTy(module->getContext()));

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFuncPtr(
		JNIEnv *,
		jclass,
		jlong structPtr,
		jlong funcPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Function *function = from_ptr<Function>(funcPtr);

	data->push_back(function);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeDataPtr(
		JNIEnv *,
		jclass,
		jlong structPtr,
		jlong dataPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *constant = from_ptr<Constant>(dataPtr);

	data->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeRelPtr(
		JNIEnv *,
		jclass,
		jlong structPtr,
		jlong relPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *constant = from_ptr<Constant>(relPtr);

	data->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeStruct(
		JNIEnv *,
		jclass,
		jlong enclosingPtr,
		jlong typePtr,
		jlong dataPtr) {

	StructType *type = from_ptr<StructType>(typePtr);
	std::vector<Constant*> *enclosing =
			from_ptr<std::vector<Constant*> >(enclosingPtr);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(dataPtr);
	Constant *constant = ConstantStruct::get(type, *data);

    delete data;
    enclosing->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeSystemStruct(
		JNIEnv *,
		jclass,
		jlong enclosingPtr,
		jlong typePtr) {

	StructType *type = from_ptr<StructType>(typePtr);
	std::vector<Constant*> *enclosing =
			from_ptr<std::vector<Constant*> >(enclosingPtr);

	enclosing->push_back(Constant::getNullValue(type));
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeGlobal(
		JNIEnv *,
		jclass,
		jlong globalPtr,
		jlong dataPtr) {

	GlobalVariable *global = from_ptr<GlobalVariable>(globalPtr);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(dataPtr);
	Constant *initializer = ConstantStruct::get(
			cast<StructType>(global->getType()->getElementType()),
			*data);

	global->setInitializer(initializer);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeAlignmentGap(
		JNIEnv *,
		jclass,
		jlong typePtr,
		jlong structPtr,
		jint index) {

	StructType *type = from_ptr<StructType>(typePtr);
	std::vector<Constant *> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *constant =
			ConstantExpr::getNullValue(type->getElementType(index));

	data->push_back(constant);
}
