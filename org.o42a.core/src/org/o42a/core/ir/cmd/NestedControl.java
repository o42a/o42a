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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;


abstract class NestedControl extends Control {

	private final MainControl main;
	private final Control parent;
	private final Block code;
	private final BracesControl braces;
	private Code allocation;
	private final CodePos exit;
	private final CodePos falseDir;

	NestedControl(Control parent, Block code, CodePos exit, CodePos falseDir) {
		this.main = parent.main();
		this.parent = parent;
		this.code = code;
		this.falseDir = falseDir;
		this.exit = exit;
		this.braces = parent.braces();
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
		return this.code;
	}

	@Override
	public final Code allocation() {
		if (this.allocation != null) {
			return this.allocation;
		}
		return this.allocation = this.parent.allocation();
	}

	@Override
	public final CodePos exit() {
		return this.exit;
	}

	@Override
	public final CodePos falseDir() {
		return this.falseDir;
	}

	@Override
	public void end() {
	}

	@Override
	final MainControl main() {
		return this.main;
	}

	@Override
	final BracesControl braces() {
		return this.braces;
	}

	@Override
	final CodePos returnDir() {
		return this.parent.returnDir();
	}

	static final class ParenthesesControl extends NestedControl {

		ParenthesesControl(Control parent, Block code, CodePos next) {
			super(parent, code, next, parent.falseDir());
		}

	}

	static final class InterrogationControl extends NestedControl {

		InterrogationControl(Control parent, CodePos next) {
			super(parent, parent.code(), next, next);
		}

	}

	static final class AltControl extends NestedControl {

		AltControl(Control parent, Block code, CodePos falseDir) {
			super(parent, code, parent.exit(), falseDir);
		}

	}

}
