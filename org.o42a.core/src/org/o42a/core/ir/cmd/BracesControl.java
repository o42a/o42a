/*
    Compiler Core
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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


final class BracesControl extends Control {

	private static final ID BRACES_ID = ID.id("braces");

	private final MainControl main;
	private final Control parent;
	private final Name name;
	private final BraceLocals locals;
	private final CodePos done;
	private final Block enclosingBlock;
	private final Allocator allocator;

	BracesControl(
			Control parent,
			Block enclosingBlock,
			CodePos next,
			Name name) {
		this.main = parent.main();
		this.parent = parent;
		this.locals = new BraceLocals(parent.locals());
		this.enclosingBlock = enclosingBlock;
		this.allocator = enclosingBlock.allocator(BRACES_ID);
		this.allocator.put(LocalsCode.class, this.locals);
		this.name = name;
		this.done = next;
	}

	public final BracesControl getEnclosing() {
		return this.parent.braces();
	}

	public final Name getName() {
		return this.name;
	}

	@Override
	public final LocalsCode locals() {
		return this.locals;
	}

	@Override
	public final Block code() {
		return this.allocator;
	}

	@Override
	public final CodePos exit() {
		return this.done;
	}

	@Override
	public CodePos falseDir() {
		return this.parent.falseDir();
	}

	@Override
	public String toString() {
		if (this.allocator == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.allocator.getId() + ']';
	}

	@Override
	final MainControl main() {
		return this.main;
	}

	@Override
	final BracesControl braces() {
		return this;
	}

	@Override
	final CodePos returnDir() {
		return main().returnDir();
	}

	@Override
	Control done() {
		if (this.allocator.exists()) {
			this.allocator.go(this.enclosingBlock.tail());
		}
		return this.parent;
	}

}
