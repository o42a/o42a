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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.util.string.ID;


public class CondBlock extends CodeBlock {

	private final BoolOp condition;
	private final ID falseName;
	private Block otherwise;

	CondBlock(Block enclosing, BoolOp condition, ID trueName, ID falseName) {
		super(enclosing, trueName);
		this.condition = condition;
		this.falseName = falseName;
	}

	public final BoolOp getCondition() {
		return this.condition;
	}

	public final Block otherwise() {
		if (this.otherwise == null) {
			initBlocks();
		}
		return this.otherwise;
	}

	@Override
	public BlockWriter writer() {
		if (this.writer == null) {
			initBlocks();
		}
		return this.writer;
	}

	@Override
	public String toString() {
		return this.condition.toString()
				+ " ? " + getId() + " : " + this.falseName;
	}

	private final Block enclosing() {
		return (Block) getEnclosing();
	}

	private void initBlocks() {
		this.writer = enclosing().writer().block(this);
		this.otherwise = enclosing().addBlock(this.falseName);
		enclosing().writer().go(
				this.condition,
				unwrapPos(head()),
				unwrapPos(this.otherwise.head()));
	}

}
