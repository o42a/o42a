/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.source.LocationInfo;


public abstract class Control {

	Control() {
	}

	Control(Control parent) {
	}

	public final Generator getGenerator() {
		return getBuilder().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return main().builder();
	}

	public final HostOp host() {
		return getBuilder().host();
	}

	public final ObjectOp owner() {
		return getBuilder().owner();
	}

	public final ValOp result() {
		return main().mainResult();
	}

	public abstract AllocationCode allocation();

	public abstract Block code();

	public abstract CodePos exit();

	public abstract CodePos falseDir();

	public final void returnValue(ValOp value) {
		returnValue(code(), value);
	}

	public final void returnValue(Block code, ValOp value) {
		main().storeResult(code, value);
		code.go(returnDir());
	}

	public final void exitBraces() {
		exitBraces(null, null);
	}

	public final void exitBraces(LocationInfo location, String name) {

		final BracesControl braces = enclosingBraces(name);

		if (braces != null) {
			code().go(exitDir(braces));
		} else {
			unresolvedBlock(location, name);
		}
	}

	public final void repeat(LocationInfo location, String name) {

		final BracesControl braces = enclosingBraces(name);

		if (braces != null) {
			code().go(repeatDir(braces));
		} else {
			unresolvedBlock(location, name);
		}
	}

	public String name(String name) {
		if (name != null) {
			return name;
		}
		return main().anonymousName();
	}

	public final Block addBlock(String name) {
		return code().addBlock(name);
	}

	public final Block addBlock(CodeId name) {
		return code().addBlock(name);
	}

	public final Control braces(Block code, CodePos next, String name) {
		return new BracesControl(this, code, next, name);
	}

	public final Control parentheses(Block code, CodePos next) {
		return new NestedControl.ParenthesesControl(this, code, next);
	}

	public final Control issue(CodePos next) {
		return new NestedControl.IssueControl(this, next);
	}

	public final Control alt(Block code, CodePos next) {
		return new NestedControl.AltControl(this, code, next);
	}

	public final DefDirs defDirs() {

		final ValDirs dirs =
				getBuilder().falseWhenUnknown(
						code(),
						falseDir())
				.value(result());

		return new ControlDirs(dirs);
	}

	public abstract void end();

	@Override
	public String toString() {

		final Block code = code();

		if (code == null) {
			return super.toString();
		}

		return getClass().getSimpleName() + '[' + code.getId() + ']';
	}

	abstract MainControl main();

	abstract BracesControl braces();

	abstract CodePos returnDir();

	abstract CodePos exitDir(BracesControl braces);

	abstract CodePos repeatDir(BracesControl braces);

	private BracesControl enclosingBraces(String name) {
		if (name == null) {
			return braces();
		}

		BracesControl braces = braces();

		while (braces != null) {
			if (name.equals(braces.getName())) {
				return braces;
			}
			braces = braces.getEnclosing();
		}

		return null;
	}

	private void unresolvedBlock(LocationInfo location, String name) {
		location.getContext().getLogger().error(
				"unresolved_block",
				location,
				"Block '%s' not found",
				name);
	}

	private final class ControlDirs extends DefDirs {

		ControlDirs(ValDirs valDirs) {
			super(valDirs, returnDir());
		}

		@Override
		public void returnValue(Block code, ValOp value) {
			Control.this.returnValue(code, value);
		}

		@Override
		public ValOp result() {
			return Control.this.result();
		}

	}

}
