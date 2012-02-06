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
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;
import org.o42a.util.ArrayUtil;


public final class AllocationCode extends Code {

	private final AllocationWriter writer;
	private final boolean disposable;
	private Code exits[];

	AllocationCode(Code enclosing, CodeId name, boolean disposable) {
		super(enclosing, name != null ? name : enclosing.id().detail("alloc"));
		this.disposable = disposable;
		this.writer = enclosing.writer().allocation(this);
	}

	public final boolean isDisposable() {
		return this.disposable;
	}

	@Override
	public boolean created() {
		return writer().created();
	}

	@Override
	public final boolean exists() {
		return writer().exists();
	}

	public final AnyRecOp allocatePtr(CodeId id) {
		assert assertIncomplete();
		return writer().allocatePtr(opId(id));
	}

	public final AnyRecOp allocateNull(CodeId id) {

		final AnyRecOp result = allocatePtr(id);

		result.store(this, nullPtr());

		return result;
	}

	public <S extends StructOp<S>> S allocate(CodeId id, Type<S> type) {
		return allocate(this, id, type);
	}

	public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			CodeId id,
			Type<S> type) {
		assert assertIncomplete();

		final Code code = this;
		final StructRecOp<S> result = writer().allocatePtr(
				code.opId(id),
				dataAllocation(type.data(code.getGenerator())));

		result.allocated(code, null);

		return result;
	}

	public final void addExit(Code exit) {
		if (this.exits == null) {
			this.exits = new Code[] {exit};
		} else {
			this.exits = ArrayUtil.append(this.exits, exit);
		}
	}

	public final Block addExitBlock(String name) {

		final Block exit = addBlock(name);

		addExit(exit);

		return exit;
	}

	public final Block addExitBlock(CodeId name) {

		final Block exit = addBlock(name);

		addExit(exit);

		return exit;
	}

	@Override
	public void done() {
		if (isComplete()) {
			return;
		}
		if (getGenerator().isProxied()) {
			super.done();
			return;
		}

		if (isDisposable()) {
			if (this.exits != null) {
				for (Code exit : this.exits) {
					if (exit.exists()) {
						writer().dispose(exit.writer());
					}
				}
			}
		}

		super.done();
	}

	@Override
	public final AllocationWriter writer() {
		return this.writer;
	}

}
