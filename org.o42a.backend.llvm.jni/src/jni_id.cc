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
#include "jni_id.h"

#include "o42ac/llvm/debug.h"
#include "o42ac/llvm/util.h"

#include "llvm/Constants.h"
#include "llvm/DerivedTypes.h"
#include "llvm/Module.h"

using namespace llvm;


jlong Java_org_o42a_backend_llvm_id_LLVMId_typeExpression(
		JNIEnv *,
		jclass,
		jlong typePtr) {

	const PATypeHolder *type = from_ptr<PATypeHolder>(typePtr);

	return to_ptr(Constant::getNullValue(type->get()->getPointerTo()));
}

jlong Java_org_o42a_backend_llvm_id_LLVMId_expression(
		JNIEnv *env,
		jclass,
		jlong modulePtr,
		jlong globalPtr,
		jintArray indices) {

	Module *module = from_ptr<Module>(modulePtr);
	Constant *global = from_ptr<Constant>(globalPtr);

	jInt32Array indexArray(env, indices);
	const size_t len = indexArray.length();
	Constant *indexList[len + 1];
	const IntegerType *int32ty =
			IntegerType::getInt32Ty(module->getContext());

	indexList[0] = ConstantInt::get(int32ty, 0);
	for (int i = len - 1; i >= 0; --i) {
		indexList[len - i] = ConstantInt::get(int32ty, indexArray[i]);
		ODEBUG_WITH_TYPE("trace", llvm::errs() << " " << indexArray[i]);
	}

	Constant *result =
			ConstantExpr::getInBoundsGetElementPtr(global, indexList, len + 1);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_id_LLVMId_relativeExpression(
		JNIEnv *,
		jclass,
		jlong idPtr,
		jlong toPtr) {
	if (!idPtr || !toPtr) {
		return to_ptr(NULL);
	}

	Constant *id = from_ptr<Constant>(idPtr);
	Constant *to = from_ptr<Constant>(toPtr);
	const IntegerType *int32ty =
			IntegerType::getInt32Ty(id->getContext());
	const IntegerType *int64ty =
			IntegerType::getInt64Ty(id->getContext());

	Constant *result = ConstantExpr::getIntegerCast(
			ConstantExpr::getSub(
					ConstantExpr::getPtrToInt(id, int64ty),
					ConstantExpr::getPtrToInt(to, int64ty)),
			int32ty,
			true);

	return to_ptr(result);
}

jlong Java_org_o42a_backend_llvm_id_LLVMId_toAnyPtr(
		JNIEnv *,
		jclass,
		jlong pointerPtr) {

	Constant *pointer = from_ptr<Constant>(pointerPtr);
	Constant *result = ConstantExpr::getPointerCast(
			pointer,
			Type::getInt8PtrTy(pointer->getContext()));

	return to_ptr(result);
}
