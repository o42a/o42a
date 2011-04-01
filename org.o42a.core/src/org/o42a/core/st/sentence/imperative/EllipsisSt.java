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
package org.o42a.core.st.sentence.imperative;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.st.action.RepeatLoop;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueType;


public final class EllipsisSt extends Statement {

	private final String name;
	private final boolean exit;

	public EllipsisSt(
			LocationInfo location,
			Imperatives enclosing,
			String name) {
		super(location, enclosing.nextDistributor());
		this.name = name;
		this.exit = enclosing.getSentence().isClaim();
	}

	private EllipsisSt(EllipsisSt prototype, Distributor distributor) {
		super(prototype, distributor);
		this.name = prototype.name;
		this.exit = prototype.exit;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return noDefinitions();
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Definitions define(Scope scope) {
		return null;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		if (this.exit) {
			return new ExitLoop(this, this.name);
		}
		return new RepeatLoop(this, this.name);
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		return initialValue(scope);
	}

	@Override
	public EllipsisSt reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new EllipsisSt(this, reproducer.distribute());
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		if (this.exit) {
			return new ExitOp(builder, this);
		}
		return new RepeatOp(builder, this);
	}

	private static final class ExitOp extends StOp {

		ExitOp(LocalBuilder builder, EllipsisSt statement) {
			super(builder, statement);
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {

			final EllipsisSt st = (EllipsisSt) getStatement();

			control.exitBraces(st, st.name);
		}

		@Override
		public void writeLogicalValue(Control control) {
		}

	}

	private static final class RepeatOp extends StOp {

		RepeatOp(LocalBuilder builder, EllipsisSt statement) {
			super(builder, statement);
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {

			final EllipsisSt st = (EllipsisSt) getStatement();

			control.repeat(st, st.name);
		}

		@Override
		public void writeLogicalValue(Control control) {
		}

	}

}
