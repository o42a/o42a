/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010 Ruslan Lopatin

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
#include <jni.h>
#include "llvm/ADT/StringRef.h"

#ifndef O42A_UTIL_H
#define O42A_UTIL_H

using namespace llvm;

template<typename T> inline T* from_ptr(const jlong ptr) {
	return reinterpret_cast<T*>(ptr);
}

inline jlong to_ptr(const void* object) {
	return reinterpret_cast<jlong>(object);
}

class jStringRef : public StringRef {

	JNIEnv *const env;
	jstring const string;

public:

	jStringRef(JNIEnv *_env, jstring _string) :
		StringRef(
				_env->GetStringUTFChars(_string, NULL),
				_env->GetStringUTFLength(_string)),
		env(_env),
		string(_string) {
	}

	inline ~jStringRef() {
		this->env->ReleaseStringUTFChars(this->string, data());
	}

};

template<typename X, typename I> class jArrayBase {
protected:

	JNIEnv *const env;
	X const array;
	I *itemList;

public:

	jArrayBase(JNIEnv *_env, X _array) : env(_env), array(_array) {}

	inline size_t length() {
		return this->env->GetArrayLength(this->array);
	}

	inline I *items() const {
		return itemList;
	}

	inline I operator[] (size_t index) {
		return itemList[index];
	}

};

template<typename X, typename I> class jArray : jArrayBase<X, I> {
public:

	jArray(JNIEnv *_env, X _array) :
		jArrayBase<X, I>::jArrayBase(_env, _array) {}

};

template<typename I> class jObjectArrayBase {
protected:

	JNIEnv *const env;
	jobjectArray const array;

public:

	jObjectArrayBase(JNIEnv *_env, jobjectArray _array) :
		env(_env),
		array(_array) {
	}

	inline size_t length() {
		return this->env->GetArrayLength(this->array);
	}

	inline I operator[] (size_t index) {
		return static_cast<I>(
				this->env->GetObjectArrayElement(this->array, index));
	}

};

template<typename I> class jObjectArray : public jObjectArrayBase<I> {
public:

	jObjectArray(JNIEnv *_env, jobjectArray _array) :
		jObjectArrayBase<I>::jObjectArrayBase(_env, _array) {
	}

};

typedef jObjectArray<jstring> jStringArray;


template<>
class jArray<jbyteArray, jbyte> : public jArrayBase<jbyteArray, jbyte> {
public:

	jArray(JNIEnv *_env, jbyteArray _array) :
		jArrayBase<jbyteArray, jbyte>::jArrayBase(_env, _array) {
		this->itemList = _env->GetByteArrayElements(_array, NULL);
	}

	~jArray() {
		this->env->ReleaseByteArrayElements(
				this->array,
				this->itemList,
				JNI_ABORT);
	}

};

template<>
class jArray<jintArray, jint> : public jArrayBase<jintArray, jint> {
public:

	jArray(JNIEnv *_env, jintArray _array) :
		jArrayBase<jintArray, jint>::jArrayBase(_env, _array) {
		this->itemList = _env->GetIntArrayElements(_array, NULL);
	}

	~jArray() {
		this->env->ReleaseIntArrayElements(
				this->array,
				this->itemList,
				JNI_ABORT);
	}

};

template<>
class jArray<jlongArray, jlong> : public jArrayBase<jlongArray, jlong> {
public:

	jArray(JNIEnv *_env, jlongArray _array) :
		jArrayBase<jlongArray, jlong>::jArrayBase(_env, _array) {
		this->itemList = _env->GetLongArrayElements(_array, NULL);
	}

	~jArray() {
		this->env->ReleaseLongArrayElements(
				this->array,
				this->itemList,
				JNI_ABORT);
	}

};

typedef jArray<jintArray, jint> jInt32Array;
typedef jArray<jlongArray, jlong> jInt64Array;

#endif
