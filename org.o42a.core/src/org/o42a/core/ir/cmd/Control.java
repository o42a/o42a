/*
    Compiler Core
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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public abstract class Control {

	public static Control mainControl(DefDirs dirs) {

		final MainControl mainControl =
				new MainControl(dirs, dirs.addBlock("continuation"));

		mainControl.init();

		return mainControl;
	}


	private ResumeCallback resumeCallback;
	private Control done;

	Control() {
	}

	public final Generator getGenerator() {
		return getBuilder().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return main().builder();
	}

	public final boolean isDone() {
		return this.done != null;
	}

	public final ObjectOp host() {
		return getBuilder().host();
	}

	public abstract LocalsCode locals();

	public final ValOp result() {
		return main().mainStore().value();
	}

	public abstract Block code();

	public abstract CodePos exit();

	public abstract CodePos falseDir();

	public final void returnValue(ValOp value) {
		returnValue(code(), value);
	}

	public final void returnValue(Block code, ValOp value) {
		main().mainStore().store(code, value);
		code.go(returnDir());
	}

	public final void exitBraces() {
		exitBraces(null, null);
	}

	public final void exitBraces(LocationInfo location, Name name) {

		final BracesControl braces = enclosingBraces(name);

		if (braces != null) {
			code().go(braces.exit());
		} else {
			unresolvedBlock(location, name);
		}
	}

	public final void repeat(LocationInfo location, Name name) {

		final BracesControl braces = enclosingBraces(name);

		if (braces != null) {
			code().go(braces.code().head());
		} else {
			unresolvedBlock(location, name);
		}
	}

	public ID name(Name name) {
		if (name != null) {
			return name.toID();
		}
		return main().anonymousName();
	}

	public final Block addBlock(String name) {
		return code().addBlock(name);
	}

	public final Block addBlock(ID name) {
		return code().addBlock(name);
	}

	public final Control braces(Block code, CodePos next, Name name) {
		return new BracesControl(this, code, next, name);
	}

	public final Control parentheses(Block code, CodePos next) {
		return new NestedControl.ParenthesesControl(this, code, next);
	}

	public final Control interrogation(CodePos next) {
		return new NestedControl.InterrogationControl(this, next);
	}

	public final Control alt(Block code, CodePos next) {
		return new NestedControl.AltControl(this, code, next);
	}

	public final Control command(String index, ControlIsolator isolator) {
		return new CommandControl(this, index + "_cmd", isolator);
	}

	public final Control resume(
			String index,
			ControlIsolator isolator,
			ResumeCallback prevResumeCallback) {

		final CommandControl control =
				new CommandControl(this, index + "_resume", isolator);
		final Block code = control.code();

		code.debug("Resumed @" + code.getId());
		main().addResumePosition(code.head(), prevResumeCallback);

		return control;
	}

	public final CodeDirs dirs() {
		return getBuilder().dirs(code(), falseDir());
	}

	public final DefDirs defDirs() {
		return new DefDirs(
				main().mainStore(),
				dirs().value(result()),
				returnDir());
	}

	public final ResumeCallback getResumeCallback() {
		return this.resumeCallback;
	}

	public final void setResumeCallback(ResumeCallback resumeCallback) {
		this.resumeCallback = resumeCallback;
	}

	/**
	 * Closes this control and returns the parent one.
	 *
	 * <p>The returned control can be used to share the allocations made by
	 * command. This is necessary e.g. for locals, but isn't compatible with
	 * value yielding.</p>
	 *
	 * @return parent allocation.
	 */
	public final Control end() {
		if (this.done != null) {
			return this.done;
		}
		return this.done = done();
	}

	/**
	 * Creates an isolated control.
	 *
	 * <p>Allocations made by isolated control are always made in dedicated
	 * allocator. Otherwise they will be made in original allocator, in which
	 * other commands can do their allocations too.</p>
	 *
	 * <p>Explicit control isolation is only necessary for commands like loops
	 * to prevent unintended allocations sharing leading to "The node does not
	 * dominate all its usages" error.</p>
	 *
	 * @return this control if it isolated control.
	 */
	public Control isolate() {
		return this;
	}

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

	abstract Control done();

	private BracesControl enclosingBraces(Name name) {
		if (name == null) {
			return braces();
		}

		BracesControl braces = braces();

		while (braces != null) {
			if (name.is(braces.getName())) {
				return braces;
			}
			braces = braces.getEnclosing();
		}

		return null;
	}

	private void unresolvedBlock(LocationInfo location, Name name) {
		location.getLocation().getLogger().error(
				"unresolved_block",
				location,
				"Block '%s' not found",
				name);
	}

}
