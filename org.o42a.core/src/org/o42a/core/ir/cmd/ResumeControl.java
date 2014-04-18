/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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


final class ResumeControl extends Control {

	private final MainControl main;
	private final Control parent;
	private final Allocator allocator;

	ResumeControl(Control parent, String index) {
		this.main = parent.main();
		this.parent = parent;
		this.allocator = parent.code().allocator(index + "_resume");
		this.allocator.debug("Resumed @" + this.allocator.getId());
	}

	@Override
	public final LocalsCode locals() {

		final BracesControl braces = braces();

		if (braces != null) {
			return braces.locals();
		}

		return main().locals();
	}

	@Override
	public final Block code() {
		return this.allocator;
	}

	@Override
	public final CodePos exit() {
		return this.parent.exit();
	}

	@Override
	public CodePos falseDir() {
		return this.parent.falseDir();
	}

	@Override
	public void end() {
		if (this.allocator.exists()) {
			this.allocator.go(this.parent.code().tail());
		}
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
		return this.parent.braces();
	}

	@Override
	final CodePos returnDir() {
		return main().returnDir();
	}

}
