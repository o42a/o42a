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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.util.Chain;


public abstract class CCodePart<C extends Code> extends Chain<OpRecord> {

	private final CCode<?> code;
	private final CodeId id;
	private OpRecord lastRevealed;
	private boolean revealing;
	private boolean hasRecords;

	public CCodePart(CCode<?> code, CodeId id) {
		this.code = code;
		this.id = id;
	}

	public final CodeId getId() {
		return this.id;
	}

	public final CCode<?> code() {
		return this.code;
	}

	public abstract C underlying();

	public void revealUpTo(OpRecord last) {
		assert !this.revealing :
			"Already revealing " + this;

		final C underlying = underlying();

		this.revealing = true;
		try {

			OpRecord record;
			if (this.lastRevealed == null) {
				record = getFirst();
			} else {
				record = this.lastRevealed.getNext();
				if (record == null) {
					return;// All revealed
				}
			}

			for (;;) {
				record.reveal(underlying);
				if (record == last) {
					break;
				}
				record = record.getNext();
			}

			this.lastRevealed = record;
		} finally {
			this.revealing = false;
		}
	}

	@Override
	protected OpRecord next(OpRecord item) {
		return item.getNext();
	}

	protected final boolean hasRecords() {
		if (this.hasRecords) {
			return true;
		}
		if (isEmpty()) {
			return false;
		}
		for (OpRecord record : this) {
			if (!record.isNoOp()) {
				return this.hasRecords = true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return getClass().getSimpleName();
		}
		return this.id.toString();
	}

	@Override
	protected void setNext(OpRecord prev, OpRecord next) {
		prev.setNext(next);
	}

	protected final void revealRecords() {
		revealUpTo(getLast());
	}

}
