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
import org.o42a.util.collect.Chain;
import org.o42a.util.string.ID;


public abstract class CCodePart<C extends Code> {

	private final CCode<?> code;
	private final ID id;
	private final Chain<OpRecord> records =
			new Chain<>(OpRecord::getNext, OpRecord::setNext);
	private OpRecord lastRevealed;
	private boolean revealing;
	private boolean hasRecords;

	public CCodePart(CCode<?> code, ID id) {
		this.code = code;
		this.id = id;
	}

	public final ID getId() {
		return this.id;
	}

	public final CCode<?> code() {
		return this.code;
	}

	public final boolean isEmpty() {
		return this.records.isEmpty();
	}

	public abstract C underlying();

	public boolean revealUpTo(OpRecord last) {
		if (this.revealing && this.lastRevealed == last) {
			return false;
		}
		assert last.part() == this :
			last + " belongs to " + last.part() + ", but " + this + " expected";
		assert !this.revealing :
			"Already revealing " + this;
		this.revealing = true;
		try {

			OpRecord record;

			if (this.lastRevealed == null) {
				record = this.records.getFirst();
			} else {
				record = this.lastRevealed.getNext();
				if (record == null) {
					return true;// All revealed
				}
			}

			for (;;) {
				this.lastRevealed = record;
				record.reveal();
				if (record == last) {
					return true;
				}
				record = record.getNext();
			}
		} finally {
			this.revealing = false;
		}
	}

	protected final boolean hasRecords() {
		if (this.hasRecords) {
			return true;
		}
		if (isEmpty()) {
			return false;
		}
		for (OpRecord record : this.records) {
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

	protected final void prepareRecords() {
		for (OpRecord record : this.records) {
			record.prepare();
		}
	}

	protected void revealRecords() {
		if (!isEmpty()) {
			revealUpTo(this.records.getLast());
		}
	}

	final void add(OpRecord op) {
		this.records.add(op);
	}

}
