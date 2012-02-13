/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.backend.constant.code;


public abstract class JumpBE extends TermBE implements EntryBE {

	private final CCodePos target;

	public JumpBE(CBlockPart part, CCodePos target) {
		super(part);
		this.target = target;
		target.part().comeFrom(this);
	}

	public final CCodePos target() {
		return this.target;
	}

	static class Unconditional extends JumpBE {

		Unconditional(CBlockPart part, CCodePos target) {
			super(part, target);
		}

		@Override
		public boolean conditional() {
			return false;
		}

		@Override
		public boolean toNext() {
			return false;
		}

		@Override
		public String toString() {
			return part() + "->" + target();
		}

		@Override
		protected void emit() {
			part().underlying().go(target().getUnderlying());
		}

	}

	static final class Next extends Unconditional {

		Next(CBlockPart part, CCodePos target) {
			super(part, target);
		}

		@Override
		public boolean toNext() {
			return true;
		}

		@Override
		public String toString() {
			return part() + ".." + target();
		}

	}

}
