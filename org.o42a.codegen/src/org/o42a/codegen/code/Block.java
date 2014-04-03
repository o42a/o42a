/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.CodeOp;
import org.o42a.codegen.debug.DebugBlockBase;
import org.o42a.util.string.ID;


public abstract class Block extends DebugBlockBase {

	private final CodePtr ptr;
	private final CodeAssets initialAssets = new CodeAssets(this);
	private CodeAssets currentAssets = this.initialAssets;

	Block(Code enclosing, ID name) {
		super(enclosing, name);
		this.ptr = new CodePtr(this);
	}

	Block(Generator generator, ID id) {
		super(generator, id);
		this.ptr = new CodePtr(this);
	}

	@Override
	public final Block getBlock() {
		return this;
	}

	public final CodePtr ptr() {
		return this.ptr;
	}

	public final CodePos head() {
		return ptr().head();
	}

	public final CodePos tail() {
		assert assertIncomplete();
		return writer().tail();
	}

	@Override
	public final CodeAssets assets() {
		return this.currentAssets;
	}

	public final Allocator allocator(String name) {
		return allocator(ID.id(name));
	}

	public final Allocator allocator(ID name) {
		assert assertIncomplete();
		return new AllocatorCode(this, name);
	}

	public final void go(CodePos pos) {
		assert assertIncomplete();
		addAssetsTo(pos);
		if (!getGenerator().isProxied()) {
			disposeUpTo(pos);
		}
		writer().go(unwrapPos(pos));
		removeAllAssets();
	}

	public final void go(CodeOp pos, CodePos[] targets) {
		assert assertIncomplete();

		final CodePos[] unwrapped = new CodePos[targets.length];

		for (int i = 0; i < targets.length; ++i) {

			final CodePos target = targets[i];

			addAssetsTo(target);
			unwrapped[i] = unwrapPos(target);
		}

		writer().go(pos, unwrapped);
		removeAllAssets();
	}

	public void returnVoid() {
		assert assertIncomplete();
		writer().returnVoid();
		complete();
	}

	@Override
	public abstract BlockWriter writer();

	@Override
	protected CodePos unwrapPos(CodePos pos) {
		if (pos == null || pos.getClass() != CodePtr.class) {
			return pos;
		}
		return ((CodePtr) pos).pos();
	}

	@Override
	protected final CondBlock choose(
			BoolOp condition,
			ID trueName,
			ID falseName) {
		assert assertIncomplete();
		return new CondBlock(this, condition, trueName, falseName);
	}

	@Override
	protected void addAssetsTo(CodePos pos) {

		final Block target = pos.code();

		if (isHead(pos)) {
			target.initialAssets.addSource(assets());
		} else {
			target.updateAssets(new CodeAssets(
					this,
					target.assets(),
					assets()));
		}
	}

	@Override
	protected void updateAssets(CodeAssets assets) {
		this.currentAssets = assets;
	}

	@Override
	protected void removeAllAssets() {
		super.removeAllAssets();
	}

	@Override
	protected void disposeBy(Allocator allocator) {
		allocator.dispose(this);
	}

	private static boolean isHead(CodePos pos) {
		if (pos == null) {
			return false;
		}
		return pos.code().ptr().is(pos);
	}

}
