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
#include "jni_data.h"

#include "o42ac/llvm/BackendModule.h"
#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/Constants.h"
#include "llvm/DerivedTypes.h"
#include "llvm/Module.h"
#include "llvm/Target/TargetData.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_binaryConstant(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring id,
		jbyteArray data,
		jint start,
		jint end,
		jboolean isConstant) {

	Module *const module = from_ptr<Module>(modulePtr);
	jStringRef name(env, id);
	jByteArray array(env, data);
	size_t length = end - start;
	const Type *const itemType = Type::getInt8Ty(module->getContext());
	const ArrayType *const type = ArrayType::get(itemType, length);
	GlobalVariable *const global =
			cast<GlobalVariable>(module->getOrInsertGlobal(name, type));

	global->setConstant(isConstant);
	global->setLinkage(GlobalValue::PrivateLinkage);

	Constant *values[length];

	for (int i = start; i < end; ++i) {
		values[i - start] = ConstantInt::get(itemType, array[i]);
	}

	global->setInitializer(ConstantArray::get(type, values, length));

	Constant *result = ConstantExpr::getPointerCast(
			global,
			Type::getInt8PtrTy(module->getContext()));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	o42ac::BackendModule *module = from_ptr<o42ac::BackendModule>(modulePtr);
	PATypeHolder *type = module->newOpaqueType();

	return to_ptr(type);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createTypeData(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	std::vector<const Type*> *result = new std::vector<const Type*>();

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStruct(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr,
		jlong typePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);
	const PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	enclosing->push_back(type->get());

	return to_ptr(type);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateGlobal(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring id,
		jlong typePtr,
		jboolean constant,
		jboolean exported) {

	Module *module = from_ptr<Module>(modulePtr);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	jStringRef name(env, id);
	GlobalVariable *global =
			cast<GlobalVariable>(module->getOrInsertGlobal(name, type->get()));

	global->setConstant(constant);
	global->setLinkage(
			exported
			? GlobalValue::ExternalLinkage : GlobalValue::PrivateLinkage);

	return to_ptr(global);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_refineType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring id,
		jlong typePtr,
		jlong typeDataPtr,
		jboolean packed) {

	Module *module = from_ptr<Module>(modulePtr);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	std::vector<const Type*> *typeData =
			from_ptr<std::vector<const Type*> >(typeDataPtr);
	StructType *newType =
			StructType::get(module->getContext(), *typeData, packed);

	delete typeData;

	cast<OpaqueType>(type->get())->refineAbstractTypeTo(newType);
	newType = cast<StructType>(type->get());
	if (id) {

		jStringRef name(env, id);

		module->addTypeName(name, newType);
	}

	return to_ptr(newType);
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateInt(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr,
		jbyte intBits) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(IntegerType::get(module->getContext(), intBits));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp32(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getFloatTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFp64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getDoubleTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateFuncPtr(
		JNIEnv *env,
		jclass cls,
		jlong enclosingPtr,
		jlong funcTypePtr) {

	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);
	FunctionType *type = from_ptr<FunctionType>(funcTypePtr);

	enclosing->push_back(type->getPointerTo());
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocatePtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getInt8PtrTy(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateStructPtr(
		JNIEnv *env,
		jclass cls,
		jlong enclosingPtr,
		jlong typePtr) {

	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);
	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	enclosing->push_back(type->get()->getPointerTo());
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateRelPtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *typeData =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	typeData->push_back(Type::getInt32Ty(module->getContext()));
}

static inline jint typeLayout(
		const o42ac::BackendModule *module,
		const Type *type) {

	const TargetData &targetData = module->getTargetData();

	return targetData.getTypeStoreSize(type)
			| (targetData.getPreferredTypeAlignmentShift(type) << 29);
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_intLayout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jbyte intBits) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, IntegerType::get(module->getContext(), intBits));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp32layout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getFloatTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_fp64layout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getDoubleTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_ptrLayout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt8PtrTy(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_relPtrLayout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt32Ty(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_structLayout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong typePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);
	const PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	return typeLayout(module, type->get());
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullPtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const Module *module = from_ptr<Module>(modulePtr);

	return to_ptr(Constant::getNullValue(
			Type::getInt8PtrTy(module->getContext())));
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullStructPtr(
		JNIEnv *env,
		jclass cls,
		jlong typePtr) {

	const PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	return to_ptr(Constant::getNullValue(type->get()->getPointerTo()));
}

jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullFuncPtr(
		JNIEnv *env,
		jclass cls,
		jlong funcTypePtr) {

	const Type *type = from_ptr<Type>(funcTypePtr);

	return to_ptr(Constant::getNullValue(type->getPointerTo()));
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataWriter_createStruct(
		JNIEnv *env,
		jclass cls,
		jint size) {

	std::vector<Constant*> *result = new std::vector<Constant*>();

	result->reserve(size);

	return to_ptr(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeInt32(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong structPtr,
		jint value) {

	const Module *module = from_ptr<Module>(modulePtr);
	const IntegerType *type = IntegerType::getInt32Ty(module->getContext());
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	ConstantInt *result = ConstantInt::getSigned(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeInt64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong structPtr,
		jlong value) {

	const Module *module = from_ptr<Module>(modulePtr);
	const IntegerType* type = Type::getInt64Ty(module->getContext());
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	ConstantInt *result = ConstantInt::getSigned(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writePtrAsInt64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong structPtr,
		jlong value) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<Constant*> *data =
				from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantExpr::getPtrToInt(
			from_ptr<Constant>(value),
			Type::getInt64Ty(module->getContext()));

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFp64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong structPtr,
		jdouble value) {

	const Module *module = from_ptr<Module>(modulePtr);
	const Type* type = Type::getDoubleTy(module->getContext());
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantFP::get(type, value);

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeNativePtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong structPtr,
		jlong value) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<Constant*> *data =
				from_ptr<std::vector<Constant*> >(structPtr);
	Constant *result = ConstantExpr::getPointerCast(
			from_ptr<Constant>(value),
			Type::getInt8PtrTy(module->getContext()));

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeFuncPtr(
		JNIEnv *env,
		jclass cls,
		jlong structPtr,
		jlong funcPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Function *function = from_ptr<Function>(funcPtr);

	data->push_back(function);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeDataPtr(
		JNIEnv *env,
		jclass cls,
		jlong structPtr,
		jlong dataPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *constant = from_ptr<Constant>(dataPtr);

	data->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeRelPtr(
		JNIEnv *env,
		jclass cls,
		jlong structPtr,
		jlong relPtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Constant *constant = from_ptr<Constant>(relPtr);

	data->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeStruct(
		JNIEnv *env,
		jclass cls,
		jlong enclosingPtr,
		jlong typePtr,
		jlong dataPtr) {

	PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);
	std::vector<Constant*> *enclosing =
			from_ptr<std::vector<Constant*> >(enclosingPtr);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(dataPtr);
	Constant *constant =
			ConstantStruct::get(cast<StructType>(type->get()), *data);

    enclosing->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeGlobal(
		JNIEnv *env,
		jclass cls,
		jlong globalPtr,
		jlong dataPtr) {

	GlobalVariable *global = from_ptr<GlobalVariable>(globalPtr);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(dataPtr);
	Constant *initializer = ConstantStruct::get(
			cast<const StructType>(global->getType()->getElementType()),
			*data);

	global->setInitializer(initializer);
}
