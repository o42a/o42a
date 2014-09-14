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

import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public class CondBlock extends Block {

	private final BoolOp condition;
	private final ID falseName;
	private final Init<CondBlocks> blocks = init(this::initBlocks);

	CondBlock(Block enclosing, BoolOp condition, ID trueName, ID falseName) {
		super(enclosing, trueName);
		this.condition = condition;
		this.falseName = falseName;
	}

	public final BoolOp getCondition() {
		return this.condition;
	}

	public final Block otherwise() {
		return this.blocks.get().otherwise;
	}

	@Override
	public final Allocator getAllocator() {
		return getEnclosing().getAllocator();
	}

	@Override
	public boolean created() {
		return this.blocks.isInitialized()
				&& this.blocks.get().writer.created();
	}

	@Override
	public final boolean exists() {
		return this.blocks.isInitialized()
				&& this.blocks.get().writer.exists();
	}

	@Override
	public BlockWriter writer() {
		return this.blocks.get().writer;
	}

	@Override
	public String toString() {
		if (this.condition == null) {
			return super.toString();
		}
		return this.condition.toString()
				+ " ? " + getId() + " : " + this.falseName;
	}

	private final Block enclosing() {
		return (Block) getEnclosing();
	}

	private CondBlocks initBlocks() {

		final BlockWriter writer = enclosing().writer().block(this);
		final Block otherwise = enclosing().addBlock(this.falseName);
		final CondBlocks blocks = new CondBlocks(writer, otherwise);

		this.blocks.set(blocks);

		final Block enclosing = enclosing();
		final CodePos truePos = unwrapPos(head());
		final CodePos falsePos = unwrapPos(otherwise.head());

		enclosing.addAssetsTo(truePos);
		enclosing.addAssetsTo(falsePos);
		enclosing.writer().go(this.condition, truePos, falsePos);
		enclosing.removeAllAssets();

		return blocks;
	}

	private static final class CondBlocks {

		private final BlockWriter writer;
		private final Block otherwise;

		CondBlocks(BlockWriter writer, Block otherwise) {
			this.writer = writer;
			this.otherwise = otherwise;
		}

	}

}
