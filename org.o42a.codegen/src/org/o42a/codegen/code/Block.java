/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.OpBlockBase;


public abstract class Block extends OpBlockBase {

	private final Head head = new Head(this);

	Block(Code enclosing, CodeId name) {
		super(enclosing, name);
	}

	Block(Generator generator, CodeId id) {
		super(generator, id);
	}

	@Override
	public final Block getBlock() {
		return this;
	}

	public final CodePos head() {
		if (created()) {
			return writer().head();
		}
		return this.head;
	}

	public final CodePos tail() {
		assert assertIncomplete();
		return writer().tail();
	}

	public final Allocator allocator(String name) {
		assert assertIncomplete();
		return new AllocatorCode(this, id(name));
	}

	public final Allocator allocator(CodeId name) {
		assert assertIncomplete();
		return new AllocatorCode(this, name);
	}

	public final void go(CodePos pos) {
		assert assertIncomplete();
		disposeUpTo(pos.code().getAllocator());
		writer().go(unwrapPos(pos));
	}

	public void returnVoid() {
		assert assertIncomplete();
		writer().returnVoid();
		complete();
	}

	@Override
	public abstract BlockWriter writer();

	@Override
	protected final CondBlock choose(
			BoolOp condition,
			CodeId trueName,
			CodeId falseName) {
		assert assertIncomplete();
		return new CondBlock(this, condition, trueName, falseName);
	}

}
