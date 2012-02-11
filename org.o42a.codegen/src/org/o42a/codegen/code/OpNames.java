/*
    Compiler Code Generator
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.*;


public class OpNames {

	private final Code code;
	private int opSeq;
	private int blockSeq;

	public OpNames(Code code) {
		this.code = code;
	}

	public final Code code() {
		return this.code;
	}

	public CodeId nestedId(CodeId name) {
		if (name != null) {
			return this.code.getId().setLocal(name);
		}
		return code().getId().setLocal(code().id().anonymous(nextBlock()));
	}

	public final CodeId opId(CodeId id) {
		if (id != null) {
			return id;
		}
		return code().getId().setLocal(code().id().anonymous(nextOp()));
	}

	public final CodeId castId(CodeId id, String type, Op op) {
		if (id != null) {
			return id;
		}
		return op.getId().type(code().id(type));
	}

	public final CodeId castId(CodeId id, CodeId type, Op op) {
		if (id != null) {
			return id;
		}
		return op.getId().type(type);
	}

	public final CodeId unaryId(CodeId id, String operator, Op op) {
		if (id != null) {
			return id;
		}
		return code().id(operator).detail(op.getId());
	}

	public final CodeId binaryId(
			CodeId id,
			String operator,
			Op left,
			Op right) {
		if (id != null) {
			return id;
		}
		return left.getId().detail(operator).detail(right.getId());
	}

	public final CodeId binaryId(
			CodeId id,
			String operator,
			Op left,
			long right) {
		if (id != null) {
			return id;
		}
		return left.getId().detail(operator).detail(Long.toString(right));
	}

	public final CodeId derefId(CodeId id, PtrOp<?> ptr) {
		return unaryId(id, "deref", ptr);
	}

	public final CodeId offsetId(CodeId id, PtrOp<?> from, RelOp offset) {
		return binaryId(id, "off", from, offset);
	}

	public final CodeId indexId(CodeId id, PtrOp<?> from, IntOp<?> index) {
		return binaryId(id, "idx", from, index);
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
		public CodeId nestedId(CodeId name) {
			if (name != null) {
				return name;
			}
			return code().getGenerator().id().anonymous(nextBlock());
		}

	}

	static final class InsetOpNames extends OpNames {

		InsetOpNames(Code code) {
			super(code);
		}

		@Override
		public CodeId nestedId(CodeId name) {

			final CodeId local = code().getId().getLocal();
			final CodeId codeName;

			if (name == null) {
				codeName = local.anonymous(nextBlock());
			} else {
				codeName = local.sub(name);
			}

			return super.nestedId(codeName);
		}

	}

}
