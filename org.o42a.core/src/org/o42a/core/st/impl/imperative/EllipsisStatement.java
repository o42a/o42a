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
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueStruct;


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
	public InlineCmd inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return null;
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
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
	protected Cmd createCmd(CodeBuilder builder) {
		if (this.exit) {
			return new ExitCmd(builder, this);
		}
		return new RepeatCmd(builder, this);
	}

	private static final class ExitCmd extends Cmd {

		ExitCmd(CodeBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void write(Control control, ValOp result) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.exitBraces(st, st.name);
		}

	}

	private static final class RepeatCmd extends Cmd {

		RepeatCmd(CodeBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void write(Control control, ValOp result) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.repeat(st, st.name);
		}

	}

}
