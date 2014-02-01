/*
    Constant Handler Compiler Back-end
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
	public CCodePart<?> part() {
		return inset().getEnclosingPart();
	}

	@Override
	public final boolean isNoOp() {
		return !hasRecords();
	}

	@Override
	public final C underlying() {
		if (this.underlying != null) {
			return this.underlying;
		}

		inset().getEnclosingPart().revealUpTo(this);
		this.underlying = inset().createUnderlying(
				inset().getEnclosingPart().underlying());

		assert this.underlying != null :
			"Can not construct an underlying inset for " + this;

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
	public void prepare() {
		prepareRecords();
	}

	@Override
	public final void reveal() {
		revealRecords();
	}

	@Override
	public boolean revealUpTo(OpRecord last) {
		part().revealUpTo(this);
		return super.revealUpTo(last);
	}

}
