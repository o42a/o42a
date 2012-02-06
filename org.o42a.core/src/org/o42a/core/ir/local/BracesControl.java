/*
    Compiler Core
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
package org.o42a.core.ir.local;

import java.util.IdentityHashMap;
import java.util.Map;

import org.o42a.codegen.code.*;


final class BracesControl extends Control {

	private final MainControl main;
	private final Block enclosingCode;
	private final Block code;
	private final Control parent;
	private final BracesControl enclosing;
	private final String name;
	private final CodePos done;
	private Block exitCode;
	private Block falseCode;
	private AllocationCode allocation;
	private Block returnCode;
	private IdentityHashMap<BracesControl, Block> exits;
	private IdentityHashMap<BracesControl, Block> repeats;

	BracesControl(Control parent, Block code, CodePos next, String name) {
		super(parent);
		this.main = parent.main();
		this.enclosingCode = code;
		this.code = code.addBlock(code.id().detail("eval"));
		this.parent = parent;
		this.enclosing = parent.braces();
		this.name = name;
		this.done = next;
	}

	public final BracesControl getEnclosing() {
		return this.enclosing;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final Block code() {
		return this.code;
	}

	@Override
	public AllocationCode allocation() {
		if (this.allocation != null) {
			return this.allocation;
		}
		return this.allocation = this.enclosingCode.allocate();
	}

	@Override
	public final CodePos exit() {
		if (this.exitCode != null) {
			return this.exitCode.head();
		}

		this.exitCode = allocation().alt("exit");

		return this.exitCode.head();
	}

	@Override
	public CodePos falseDir() {
		if (this.falseCode != null) {
			return this.falseCode.head();
		}

		this.falseCode = allocation().alt("false");

		return this.falseCode.head();
	}

	@Override
	public void end() {
		if (this.allocation == null) {
			this.enclosingCode.go(this.code.head());
			this.code.go(this.enclosingCode.tail());
		} else {
			this.allocation.go(this.code.head());
			this.code.go(this.allocation.tail());
			this.allocation.done();
		}
		if (this.exitCode != null && this.exitCode.exists()) {
			this.exitCode.go(this.done);
		}
		if (this.falseCode != null && this.falseCode.exists()) {
			this.falseCode.go(this.parent.falseDir());
		}
		if (this.returnCode != null) {
			this.returnCode.go(this.parent.returnDir());
		}
		if (this.exits != null) {
			for (Map.Entry<BracesControl, Block> e : this.exits.entrySet()) {

				final BracesControl braces = e.getKey();
				final Block exit = e.getValue();

				exit.go(this.parent.exitDir(braces));
			}
		}
		if (this.repeats != null) {
			for (Map.Entry<BracesControl, Block> e : this.repeats.entrySet()) {

				final BracesControl braces = e.getKey();
				final Block repeat = e.getValue();

				if (braces == this) {
					repeat.go(this.enclosingCode.head());
					continue;
				}
				repeat.go(this.parent.repeatDir(braces));
			}
		}
	}

	@Override
	public String toString() {

		final Code code = this.enclosingCode;

		if (code == null) {
			return super.toString();
		}

		return getClass().getSimpleName() + '[' + code.getId() + ']';
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
		if (this.returnCode != null) {
			return this.returnCode.head();
		}

		this.returnCode = allocation().alt("return");

		return this.returnCode.head();
	}

	@Override
	final CodePos exitDir(BracesControl braces) {
		if (braces == this) {
			return exit();
		}
		if (this.exits == null) {
			this.exits = new IdentityHashMap<BracesControl, Block>(1);
		} else {

			final Block exit = this.exits.get(braces);

			if (exit != null) {
				return exit.head();
			}
		}

		final Block exit;
		final String name = braces.getName();

		if (name == null) {
			exit = allocation().alt("exit");
		} else {
			exit = allocation().alt(this.code.id("exit").sub(name));
		}

		this.exits.put(braces, exit);

		return exit.head();
	}

	@Override
	final CodePos repeatDir(BracesControl braces) {
		if (this.repeats == null) {
			this.repeats = new IdentityHashMap<BracesControl, Block>(1);
		} else {

			final Block repeat = this.repeats.get(braces);

			if (repeat != null) {
				return repeat.head();
			}
		}

		final Block repeat;
		final String name = braces.getName();

		if (name == null) {
			repeat = allocation().alt("repeat");
		} else {
			repeat = allocation().alt(this.code.id("repeat").sub(name));
		}

		this.repeats.put(braces, repeat);

		return repeat.head();
	}

}
