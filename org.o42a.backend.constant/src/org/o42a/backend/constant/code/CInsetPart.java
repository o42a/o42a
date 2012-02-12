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
package org.o42a.backend.constant.code;

import org.o42a.codegen.code.Code;


final class CInsetPart<C extends Code>
		extends CCodePart<C>
		implements OpRecord {

	private C underlying;
	private OpRecord next;

	CInsetPart(CInset<C> inset) {
		super(inset, inset.getId());
	}

	@Override
	public boolean isEmptyOp() {
		return !hasOps();
	}

	@Override
	public final C underlying() {
		if (this.underlying != null) {
			return this.underlying;
		}

		inset().getEnclosingPart().revealUpTo(this);
		assert this.underlying != null :
			"Inset \"" + this + "\" not revealed yet";

		return this.underlying;
	}

	@SuppressWarnings("unchecked")
	public final CInset<C> inset() {
		return (CInset<C>) code();
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
	public final void reveal(Code underlying) {
		if (isEmpty()) {
			return;
		}
		assert this.underlying == null :
			"Insset \"" + this + "\" already revealed";
		this.underlying = inset().createUnderlying(underlying);
		revealRecords();
	}

}
