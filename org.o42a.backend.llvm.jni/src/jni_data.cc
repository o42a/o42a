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
		jint end) {

	Module *const module = from_ptr<Module>(modulePtr);
	jStringRef name(env, id);
	jArray<jbyteArray, jbyte> array(env, data);
	const size_t length = end - start;

	OTRACE("binaryConstant: " << name << "\n");

	const Type *const itemType = Type::getInt8Ty(module->getContext());
	const ArrayType *const type = ArrayType::get(itemType, length);
	GlobalVariable *const global =
			cast<GlobalVariable>(module->getOrInsertGlobal(name, type));

	global->setConstant(true);
	global->setLinkage(GlobalValue::InternalLinkage);

	Constant *values[length];

	for (int i = start; i < end; ++i) {
		values[i] = ConstantInt::get(itemType, array[i]);
	}

	global->setInitializer(ConstantArray::get(type, values, length));

	ODUMP(global);

	Constant *result = ConstantExpr::getPointerCast(
			global,
			Type::getInt8PtrTy(module->getContext()));

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_createTypeData(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	std::vector<const Type*> *result = new std::vector<const Type*>();

	OTRACE("createTypeData: " << result << "\n");

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
	const Type *type = from_ptr<Type>(typePtr);

	enclosing->push_back(type);

	OTRACE("allocateStruct (" << module->getTypeName(type)
			<< "): " << *type << "\n");

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
	Type *type = from_ptr<Type>(typePtr);
	jStringRef name(env, id);
	GlobalVariable *global =
			cast<GlobalVariable>(module->getOrInsertGlobal(name, type));

	global->setConstant(constant);
	global->setLinkage(
			exported
			? GlobalValue::ExternalLinkage : GlobalValue::InternalLinkage);

	OTRACE("allocateGlobal: " << name << "\n");
	ODUMP(global);

	return to_ptr(global);
}

jlong Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateType(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jstring id,
		jlong typeDataPtr,
		jboolean packed) {

	Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *typeData =
			from_ptr<std::vector<const Type*> >(typeDataPtr);
	StructType *type = StructType::get(module->getContext(), *typeData, packed);

	if (id) {

		jStringRef name(env, id);

		module->addTypeName(name, type);
	}

	OTRACE("allocateType (" << module->getTypeName(type) << "): "
			<< *type << "\n");

	return to_ptr(type);
}


void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateInt32(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getInt32Ty(module->getContext()));
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateInt64(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);

	enclosing->push_back(Type::getInt64Ty(module->getContext()));
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

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateCodePtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong enclosingPtr,
		jlong funcTypePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
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
		jlong modulePtr,
		jlong enclosingPtr,
		jlong typePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *enclosing =
			from_ptr<std::vector<const Type*> >(enclosingPtr);
	Type *type = from_ptr<Type>(typePtr);

	enclosing->push_back(type->getPointerTo());
}

void Java_org_o42a_backend_llvm_data_LLVMDataAllocator_allocateRelPtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong typePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	std::vector<const Type*> *typeData =
			from_ptr<std::vector<const Type*> >(typePtr);

	typeData->push_back(Type::getInt32Ty(module->getContext()));
}

static inline jint typeLayout(
		const o42ac::BackendModule *module,
		const Type *type) {

	const TargetData &targetData = module->getTargetData();

	return targetData.getTypeStoreSize(type)
			| (targetData.getPreferredTypeAlignmentShift(type) << 29);
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_int32layout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt32Ty(module->getContext()));
}

jint Java_org_o42a_backend_llvm_data_LLVMDataAllocator_int64layout(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr) {

	const o42ac::BackendModule *module =
			from_ptr<o42ac::BackendModule>(modulePtr);

	return typeLayout(module, Type::getInt64Ty(module->getContext()));
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
	const Type *type = from_ptr<Type>(typePtr);

	return typeLayout(module, type);
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
		jlong modulePtr,
		jlong typePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	const Type *type = from_ptr<Type>(typePtr);

	return to_ptr(Constant::getNullValue(type->getPointerTo()));
}

jlong JNICALL Java_org_o42a_backend_llvm_data_LLVMDataWriter_nullFuncPtr(
		JNIEnv *env,
		jclass cls,
		jlong modulePtr,
		jlong typePtr) {

	const Module *module = from_ptr<Module>(modulePtr);
	const Type *type = from_ptr<Type>(typePtr);

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

	OTRACE("writeInt32: " << *result << "\n");

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

	OTRACE("writeInt64: " << *result << "\n");

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

	OTRACE("writePtrAsInt64: " << *result << "\n");

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

	OTRACE("writeFp64: " << *result << "\n");

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

	OTRACE("writeNativePtr: " << *result << "\n");

	data->push_back(result);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeCodePtr(
		JNIEnv *env,
		jclass cls,
		jlong structPtr,
		jlong codePtr) {

	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(structPtr);
	Function *function = from_ptr<Function>(codePtr);

	OTRACE("writeCodePtr: " << function->getName() << "\n");

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

	OTRACE("writeDataPtr: " << *constant << "\n");

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

	OTRACE("writeRelPtr: " << *constant << "\n");

	data->push_back(constant);
}

void Java_org_o42a_backend_llvm_data_LLVMDataWriter_writeStruct(
		JNIEnv *env,
		jclass cls,
		jlong enclosingPtr,
		jlong typePtr,
		jlong dataPtr) {

	StructType *type = from_ptr<StructType>(typePtr);
	std::vector<Constant*> *enclosing =
			from_ptr<std::vector<Constant*> >(enclosingPtr);
	std::vector<Constant*> *data =
			from_ptr<std::vector<Constant*> >(dataPtr);

	ODEBUG_WITH_TYPE(
			"trace",
			llvm::errs() << "writeStruct:\n";
			for (size_t i = 0, s = data->size(); i < s; ++i) {
				Constant *c = data->at(i);
				llvm::errs() << "     ";
				llvm::errs() << c->getName() << ": " << *c->getType();
				llvm::errs() << "\n";
			}
			llvm::errs() << "**** " << *type << "\n";
	);

	Constant *constant = ConstantStruct::get(type, *data);

	ODUMP(constant);

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

	OTRACE("writeGlobal:" << *global << "\n");

	Constant *initializer = ConstantStruct::get(
			cast<const StructType>(global->getType()->getElementType()),
			*data);

	global->setInitializer(initializer);
}
