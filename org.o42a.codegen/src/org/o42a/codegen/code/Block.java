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

	private static final ID ENTER_TO_ID = ID.id().detail("__enter_to__");

	private final CodePtr ptr;
	private final CodeAssets initialAssets = new CodeAssets(this, "initial");
	private CodeAssets assets = this.initialAssets;

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
		return this.assets;
	}

	public final Allocator allocator(String name) {
		return allocator(ID.id(name));
	}

	public final Allocator allocator(ID name) {
		assert assertIncomplete();
		return new AllocatorCode(this, name, false);
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

	public CodePos[] go(CodeOp pos, CodePos[] targets) {
		assert assertIncomplete();

		final CodePos[] heads = new CodePos[targets.length];
		final CodePos[] unwrapped = new CodePos[targets.length];

		for (int i = 0; i < targets.length; ++i) {

			final CodePos head = reallocateDownTo(targets[i]);

			heads[i] = head;
			addAssetsTo(head);
			unwrapped[i] = unwrapPos(head);
		}

		writer().go(pos, unwrapped);
		removeAllAssets();

		return heads;
	}

	public void returnVoid() {
		returnVoid(true);
	}

	@Override
	public abstract BlockWriter writer();

	@Override
	protected Allocator debugAllocator(ID name) {
		assert assertIncomplete();
		return new AllocatorCode(this, name, true);
	}

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
					"go to " + pos,
					target.assets(),
					assets()));
		}
	}

	@Override
	protected void updateAssets(CodeAssets assets) {
		assert !getFunction().isDone() :
			"Can not update assets of already built function";
		this.assets = assets;
	}

	@Override
	protected void removeAllAssets() {
		super.removeAllAssets();
	}

	@Override
	protected void disposeBy(Allocator allocator) {
		allocator.dispose(this);
	}

	void returnVoid(boolean dispose) {
		assert assertIncomplete();
		writer().returnVoid(dispose);
		complete();
	}

	CodePos reallocateDownTo(CodePos pos) {
		if (getGenerator().isProxied()) {
			return pos;
		}

		final Allocator from = getClosestAllocator();
		final Allocator to = pos.code().getClosestAllocator();

		if (from == to) {
			return pos;
		}
		if (!to.ptr().is(pos)) {
			throw new UnsupportedOperationException(
					"Can jump only to allocator's head");
		}

		final Block realloc = addBlock(ENTER_TO_ID.detail(pos.code().getId()));

		allocate(realloc, from, pos, to);
		realloc.writer().go(unwrapPos(pos));
		realloc.addAssetsTo(pos);

		return realloc.head();
	}

	private static boolean isHead(CodePos pos) {
		if (pos == null) {
			return false;
		}
		return pos.code().ptr().is(pos);
	}

	private void allocate(
			Block realloc,
			Allocator from,
			CodePos target,
			Allocator allocator) {

		final Allocator enclosing = allocator.getEnclosingAllocator();

		if (enclosing != from) {
			allocate(realloc, from, target, enclosing);
		}

		allocator.allocate(realloc, target);
	}

}
