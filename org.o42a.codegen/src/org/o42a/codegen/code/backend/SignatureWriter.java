/*
    Compiler Code Generator
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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.data.Type;


public interface SignatureWriter<F extends Func> {

	void returnVoid();

	void returnInt32();

	void returnInt64();

	void returnFp64();

	void returnBool();

	void returnAny();

	void returnData();

	void returnPtr(Type<?> type);

	void addInt32();

	void addInt64();

	void addFp64();

	void addBool();

	void addRelPtr();

	void addAny();

	void addData();

	void addPtr(Type<?> type);

	void addFuncPtr(Signature<?> signature);

	SignatureAllocation<F> done();

}
