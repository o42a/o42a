/*
    Compiler Core
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
package org.o42a.core.ir.local;

import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;


abstract class NestedControl extends Control {

	private final MainControl main;
	private final Control parent;
	private final Code code;
	private final BracesControl braces;
	private AllocationCode allocation;
	private final CodePos exit;

	NestedControl(Control parent, Code code, CodePos exit) {
		super(parent);
		this.main = parent.main();
		this.exit = exit;
		this.braces = parent.braces();
		this.parent = parent;
		this.code = code;
	}

	@Override
	public final Code code() {
		return this.code;
	}

	@Override
	public final AllocationCode allocation() {
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

	@Override
	final CodePos exitDir(BracesControl braces) {
		return this.parent.exitDir(braces);
	}

	@Override
	final CodePos repeatDir(BracesControl braces) {
		return this.parent.repeatDir(braces);
	}

	static final class ParenthesesControl extends NestedControl {

		ParenthesesControl(Control parent, Code code, CodePos next) {
			super(parent, code, next);
		}

	}

	static final class IssueControl extends NestedControl {

		IssueControl(Control parent, CodePos next) {
			super(parent, parent.code(), next);
		}

	}

	static final class AltControl extends NestedControl {

		AltControl(Control parent, Code code, CodePos next) {
			super(parent, code, next);
		}

	}

}
