/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.signature;

import org.o42a.codegen.code.Arg;
import org.o42a.codegen.code.SignatureBuilder;


enum CArgType {

	VOID() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnVoid();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			throw new UnsupportedOperationException();
		}

	},

	INT8() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnInt8();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addInt8(arg.getName());
		}

	},

	INT16() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnInt16();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addInt16(arg.getName());
		}

	},

	INT32() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnInt32();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addInt32(arg.getName());
		}

	},

	INT64() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnInt64();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addInt64(arg.getName());
		}

	},

	FP32() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnFp32();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addFp32(arg.getName());
		}

	},

	FP64() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnFp64();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addFp64(arg.getName());
		}

	},

	BOOL() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnBool();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addBool(arg.getName());
		}

	},

	REL() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addRelPtr(arg.getName());
		}

	},

	ANY() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnAny();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addPtr(arg.getName());
		}

	},

	DATA() {

		@Override
		public void setReturn(SignatureBuilder builder) {
			builder.returnData();
		}

		@Override
		public Arg<?> addArg(SignatureBuilder builder, CArg<?> arg) {
			return builder.addData(arg.getName());
		}

	};

	public abstract void setReturn(SignatureBuilder builder);

	public abstract Arg<?> addArg(SignatureBuilder builder, CArg<?> arg);

}
