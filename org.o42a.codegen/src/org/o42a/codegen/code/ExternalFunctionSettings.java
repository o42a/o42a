/*
    Compiler Code Generator
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.data.DataType;


public final class ExternalFunctionSettings
		extends AbstractFunctionSettings<ExternalFunctionSettings> {

	ExternalFunctionSettings(Functions functions) {
		super(functions);
		this.flags = EXPORTED;
	}

	public final ExternalFunctionSettings set(FunctionAttributes properties) {
		this.flags = properties.getFunctionFlags() | EXPORTED;
		return this;
	}

	public <F extends Fn<F>> FuncPtr<F> link(
			String name,
			Signature<F> signature) {
		assert (hasSideEffects()
				|| signature.returns(getGenerator()).getDataType()
				!= DataType.VOID) :
			"Function " + name + " returns void, but declares no side effects";
		return functions().externalFunction(name, signature, this);
	}

}
