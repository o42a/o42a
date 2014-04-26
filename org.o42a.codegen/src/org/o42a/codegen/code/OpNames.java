/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import static org.o42a.codegen.code.op.RecOp.DEREF_ID;
import static org.o42a.codegen.code.op.RelOp.OFFSET_ID;

import org.o42a.codegen.code.op.*;
import org.o42a.util.string.ID;


public class OpNames {

	public static final ID INDEX_ID = ID.id("idx");

	private final Code code;
	private int opSeq;
	private int blockSeq;

	public OpNames(Code code) {
		this.code = code;
	}

	public final Code code() {
		return this.code;
	}

	public ID nestedId(ID name) {
		if (name != null) {
			return this.code.getId().setLocal(name);
		}
		return code().getId().setLocal(ID.id().anonymous(nextBlock()));
	}

	public final ID opId(ID id) {
		if (id != null) {
			return id;
		}
		return code().getId().setLocal(ID.id().anonymous(nextOp()));
	}

	public final ID castId(ID id, ID type, Op op) {
		if (id != null) {
			return id;
		}
		return op.getId().type(type);
	}

	public final ID unaryId(ID id, ID operator, Op op) {
		if (id != null) {
			return id;
		}
		return operator.detail(op.getId());
	}

	public final ID binaryId(ID id, ID operator, Op left, Op right) {
		if (id != null) {
			return id;
		}
		return left.getId().detail(operator).detail(right.getId());
	}

	public final ID binaryId(
			ID id,
			ID operator,
			Op left,
			long right) {
		if (id != null) {
			return id;
		}
		return left.getId().detail(operator).detail(Long.toString(right));
	}

	public final ID derefId(ID id, PtrOp<?> ptr) {
		if (id != null) {
			return id;
		}
		return ptr.getId().detail(DEREF_ID);
	}

	public final ID offsetId(ID id, PtrOp<?> from, RelOp offset) {
		return binaryId(id, OFFSET_ID, from, offset);
	}

	public final ID indexId(ID id, PtrOp<?> from, IntOp<?> index) {
		return binaryId(id, INDEX_ID, from, index);
	}

	@Override
	public String toString() {
		return "OpNames[" + this.code + ']';
	}

	protected final int nextOp() {
		return ++this.opSeq;
	}

	protected final int nextBlock() {
		return ++this.blockSeq;
	}

	static final class FunctionOpNames extends OpNames {

		FunctionOpNames(Function<?> function) {
			super(function);
		}

		@Override
		public ID nestedId(ID name) {
			if (name != null) {
				return name;
			}
			return ID.id().anonymous(nextBlock());
		}

	}

	static final class InsetOpNames extends OpNames {

		InsetOpNames(Code code) {
			super(code);
		}

		@Override
		public ID nestedId(ID name) {

			final ID local = code().getId().getLocal();
			final ID localName;

			if (name == null) {
				localName = local.anonymous(nextBlock());
			} else {
				localName = local.sub(name);
			}

			return code().getEnclosing().getOpNames().nestedId(localName);
		}

	}

}
