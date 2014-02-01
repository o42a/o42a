/*
    Compiler Code Generator
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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.FuncAllocation;


public interface FuncWriter<F extends Func<F>> extends BlockWriter {

	FuncAllocation<F> getAllocation();

	Int8op int8arg(Code code, Arg<Int8op> arg);

	Int16op int16arg(Code code, Arg<Int16op> arg);

	Int32op int32arg(Code code, Arg<Int32op> arg);

	Int64op int64arg(Code code, Arg<Int64op> arg);

	Fp32op fp32arg(Code code, Arg<Fp32op> arg);

	Fp64op fp64arg(Code code, Arg<Fp64op> arg);

	BoolOp boolArg(Code code, Arg<BoolOp> arg);

	RelOp relPtrArg(Code code, Arg<RelOp> arg);

	AnyOp ptrArg(Code code, Arg<AnyOp> arg);

	DataOp dataArg(Code code, Arg<DataOp> arg);

	<S extends StructOp<S>> S ptrArg(Code code, Arg<S> arg, Type<S> type);

	<FF extends Func<FF>> FF funcPtrArg(
			Code code,
			Arg<FF> arg,
			Signature<FF> signature);

	void done();

}
