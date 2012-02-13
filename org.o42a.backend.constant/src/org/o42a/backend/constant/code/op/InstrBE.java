/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.OpRecord;
import org.o42a.codegen.code.Code;


public abstract class InstrBE implements OpRecord {

	private OpRecord next;
	private final CCodePart<?> part;

	public InstrBE(CCode<?> code) {
		this.part = code.op(this);
	}

	@Override
	public boolean isNoOp() {
		return false;
	}

	public final CCodePart<?> part() {
		return this.part;
	}

	@Override
	public final OpRecord getNext() {
		return this.next;
	}

	@Override
	public final void setNext(OpRecord next) {
		this.next = next;
	}

	@Override
	public void reveal(Code underlying) {
		emit();
	}

	protected abstract void emit();

}
