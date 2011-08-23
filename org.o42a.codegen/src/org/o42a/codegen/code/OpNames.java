/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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

	public CodeId opId(CodeId id) {
		if (id != null) {
			return id;
		}
		return this.code.getId().setLocal(
				this.code.getGenerator().id().anonymous(nextOp()));
	}

	public CodeId nestedId(CodeId name) {
		if (name != null) {
			return this.code.getId().setLocal(name);
		}
		return this.code.getId().setLocal(
				this.code.getGenerator().id().anonymous(nextBlock()));
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

}
