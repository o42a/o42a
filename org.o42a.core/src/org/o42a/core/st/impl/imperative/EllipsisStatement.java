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
package org.o42a.core.st.impl.imperative;

import org.o42a.core.Distributor;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Imperatives;


public final class EllipsisStatement extends Statement {

	private final String name;
	private final boolean exit;

	public EllipsisStatement(
			LocationInfo location,
			Imperatives enclosing,
			String name) {
		super(location, enclosing.nextDistributor());
		this.name = name;
		this.exit = enclosing.getSentence().isClaim();
	}

	private EllipsisStatement(
			EllipsisStatement prototype,
			Distributor distributor) {
		super(prototype, distributor);
		this.name = prototype.name;
		this.exit = prototype.exit;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public Definer define(StatementEnv env) {
		if (this.exit) {
			return new EllipsisDefiner.ExitDefiner(this, env);
		}
		return new EllipsisDefiner.RepeatDefiner(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new EllipsisStatement(this, reproducer.distribute());
	}

	@Override
	public String toString() {
		if (!this.exit) {
			if (this.name == null) {
				return "(...)";
			}
			return "(... " + this.name + ')';
		}
		if (this.name == null) {
			return "(...!)";
		}
		return "(... " + this.name + "!)";
	}

	@Override
	protected void fullyResolveImperative(LocalResolver resolver) {
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		if (this.exit) {
			return new ExitOp(builder, this);
		}
		return new RepeatOp(builder, this);
	}

	private static final class ExitOp extends StOp {

		ExitOp(LocalBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.exitBraces(st, st.name);
		}

		@Override
		public void writeLogicalValue(Control control) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class RepeatOp extends StOp {

		RepeatOp(LocalBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.repeat(st, st.name);
		}

		@Override
		public void writeLogicalValue(Control control) {
			throw new UnsupportedOperationException();
		}

	}

}
