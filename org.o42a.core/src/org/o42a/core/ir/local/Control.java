/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationSpec;


public abstract class Control {

	private static final byte REACHABLE = 0;
	private static final byte REPEAT = 1;
	private static final byte DONE = 2;
	private static final byte VALUE_RETURNED = 3;
	private static final byte UNREACHABLE = 4;

	static Control createControl(
			LocalBuilder builder,
			Code code,
			CodePos exit) {
		return new Main(builder, code, exit);
	}

	private final Main main;
	private final Braces braces;
	private final Code code;
	private final CodePos exit;

	private byte reachability;

	private Control(Control parent, Code code, CodePos exit) {
		this.main = parent.main;
		this.braces =
			getClass() == Braces.class ? (Braces) this : parent.braces;
		this.code = code;
		this.exit = exit;
		this.reachability = parent.reachability;
	}

	private Control(Code code, CodePos exit) {
		this.main = (Main) this;
		this.braces = null;
		this.code = code;
		this.exit = exit;
	}

	public final LocalBuilder getBuilder() {
		return this.main.builder;
	}

	public final boolean isDone() {
		return this.reachability != REACHABLE;
	}

	public final boolean mayContinue() {
		return this.reachability != UNREACHABLE;
	}

	public final Code code() {
		return this.code;
	}

	public final CodePos exit() {
		return this.exit;
	}

	public final void returnValue() {
		this.code.returnVoid();
		if (!isDone()) {
			this.reachability = VALUE_RETURNED;
		}
	}

	public final void exitBraces() {
		exitBraces(null, null);
	}

	public final void exitBraces(LocationSpec location, String name) {
		if (isDone()) {
			return;
		}

		final Braces braces = enclosingBraces(name);

		if (braces != null) {
			this.code.go(braces.done);
		} else {
			location.getContext().getLogger().unresolved(location, name);
		}

		if (!isDone()) {
			this.reachability = DONE;
		}
	}

	public final void repeat(LocationSpec location, String name) {
		if (isDone()) {
			return;
		}

		final Braces braces = enclosingBraces(name);

		if (braces != null) {
			this.code.go(braces.code().head());
		} else {
			location.getContext().getLogger().unresolved(location, name);
		}

		if (!isDone()) {
			this.reachability = REPEAT;
		}
	}

	public final boolean reach(LocationSpec location) {
		if (!isDone()) {
			return true;
		}
		if (this.reachability < UNREACHABLE) {
			location.getContext().getLogger().ignored(location);
			this.reachability = UNREACHABLE;
		}
		return false;
	}

	public final void reachability(Control reason) {
		if (reason.reachability > this.reachability) {
			this.reachability = reason.reachability;
		}
	}

	public String name(String name) {
		if (name != null) {
			return name;
		}
		return Integer.toString(++this.main.seq);
	}

	public final Code addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Control braces(Code code, CodePos next, String name) {
		return new Braces(this, code, next, name);
	}

	public final Control parentheses(Code code, CodePos next) {
		return new Parentheses(this, code, next);
	}

	public final Control issue(CodePos next) {
		return new Issue(this, next);
	}

	public final Control alt(Code code, CodePos next) {
		return new Alt(this, code, next);
	}

	private Braces enclosingBraces(String name) {
		if (name == null) {
			return this.braces;
		}

		Braces braces = this.braces;

		while (braces != null) {
			if (name.equals(braces.getName())) {
				return braces;
			}
			braces = braces.enclosing;
		}

		return null;
	}

	private static final class Main extends Control {

		private final LocalBuilder builder;
		private int seq;

		Main(LocalBuilder builder, Code code, CodePos exit) {
			super(code, exit);
			this.builder = builder;
		}

	}

	private static final class Braces extends Control {

		private final Braces enclosing;
		private final String name;
		private final CodePos done;

		Braces(Control parent, Code code, CodePos next, String name) {
			super(parent, code, next);
			this.enclosing = parent.braces;
			this.name = name;
			this.done = next;
		}

		public final String getName() {
			return this.name;
		}

	}

	private static final class Parentheses extends Control {

		Parentheses(Control parent, Code code, CodePos next) {
			super(parent, code, next);
		}

	}

	private static final class Issue extends Control {

		Issue(Control parent, CodePos next) {
			super(parent, parent.code, next);
		}

	}

	private static final class Alt extends Control {

		Alt(Control parent, Code code, CodePos next) {
			super(parent, code, next);
		}

	}

}
