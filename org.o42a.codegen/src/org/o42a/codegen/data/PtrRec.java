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
package org.o42a.codegen.data;

import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.util.string.ID;


public abstract class PtrRec<P extends AllocPtrOp<P>, T extends Ptr<?>>
		extends Rec<P, T> {

	PtrRec(SubData<?> enclosing, ID id) {
		super(enclosing, id);
	}

	@Override
	public PtrRec<P, T> setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public PtrRec<P, T> setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public PtrRec<P, T> setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	public abstract void setNull();

}
