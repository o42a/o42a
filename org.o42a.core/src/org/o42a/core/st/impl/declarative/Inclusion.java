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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueStruct;


public abstract class Inclusion extends Statement implements Instruction {

	private InclusionEnv env;

	public Inclusion(LocationInfo location, Declaratives statements) {
		super(location, statements.nextDistributor());
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return noDefinitions();
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		throw new UnsupportedOperationException();
	}

	public final StatementEnv getInitialEnv() {
		return this.env.getInitialEnv();
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		return this.env = new InclusionEnv(env);
	}

	@Override
	public final void execute(InstructionContext context) {

		final DeclarativeBlock block = context.getBlock().toDeclarativeBlock();

		this.env.setBlock(block);
		includeInto(block);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return this;
	}

	@Override
	public Definitions define(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	protected abstract void includeInto(DeclarativeBlock block);

	@Override
	protected void fullyResolve(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

}
