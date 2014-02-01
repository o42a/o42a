/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.backend.constant.code.op.BoolCOp;


public abstract class JumpBE extends TermBE implements EntryBE {

	private final CCodePos target;

	public JumpBE(CBlockPart part, CCodePos target) {
		super(part);
		this.target = target;
		target.part().comeFrom(this);
	}

	public abstract boolean conditional();

	public final CCodePos target() {
		return this.target;
	}

	@Override
	public final JumpBE toJump() {
		return this;
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
		public boolean continuation() {
			return false;
		}

		@Override
		public void prepare() {
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

	static class Fake extends Unconditional {

		Fake(CBlockPart part, CCodePos target) {
			super(part, target);
		}

		@Override
		protected void emit() {
		}

	}

	static final class Next extends Unconditional {

		Next(CBlockPart part, CCodePos target) {
			super(part, target);
		}

		@Override
		public boolean continuation() {
			return true;
		}

		@Override
		public String toString() {
			return part() + ".." + target();
		}

	}

	static final class Conditional extends JumpBE {

		private final BoolCOp condition;
		private final CCodePos falseTarget;

		Conditional(
				CBlockPart part,
				BoolCOp condition,
				CCodePos trueTarget,
				CCodePos falseTarget) {
			super(part, trueTarget);
			this.condition = condition;
			this.falseTarget = falseTarget;
			new FalseEntryBE(this);
		}

		@Override
		public boolean conditional() {
			return true;
		}

		@Override
		public boolean continuation() {
			return false;
		}

		@Override
		public void prepare() {
			use(this.condition);
		}

		@Override
		public String toString() {
			return (part() + ": " + this.condition
					+ "? " + target() + " : " + this.falseTarget);
		}

		@Override
		protected void emit() {
			this.condition.backend().underlying().go(
					part().underlying(),
					target().getUnderlying(),
					this.falseTarget.getUnderlying());
		}

	}

	private static final class FalseEntryBE implements EntryBE {

		private final Conditional jump;

		FalseEntryBE(Conditional jump) {
			this.jump = jump;
			jump.falseTarget.part().comeFrom(this);
		}

		@Override
		public CBlockPart part() {
			return part();
		}

		@Override
		public boolean continuation() {
			return false;
		}

		@Override
		public JumpBE toJump() {
			return this.jump;
		}

		@Override
		public String toString() {
			if (this.jump == null) {
				return super.toString();
			}
			return (part() + ": --" + this.jump.condition
					+ "? " + this.jump.falseTarget
					+ " : " + this.jump.target());
		}

	}

}
