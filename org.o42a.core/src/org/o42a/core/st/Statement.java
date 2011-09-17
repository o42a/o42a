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
package org.o42a.core.st;

import static org.o42a.core.ir.local.StOp.noStOp;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class Statement extends Placed {

	private StOp op;
	private boolean fullyResolved;

	public Statement(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	public DeclarativeBlock toDeclarativeBlock() {
		return null;
	}

	public ImperativeBlock toImperativeBlock() {
		return null;
	}

	public abstract DefinitionTargets getDefinitionTargets();

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public abstract ValueStruct<?, ?> getValueStruct();

	public abstract StatementEnv setEnv(StatementEnv env);

	public abstract Definitions define(Scope scope);

	public abstract Action initialValue(LocalResolver resolver);

	public abstract Action initialLogicalValue(LocalResolver resolver);

	public abstract Statement reproduce(Reproducer reproducer);

	public void resolveAll(Resolver resolver) {
		this.fullyResolved = true;
		getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public void resolveValues(Resolver resolver) {
		resolveAll(resolver);
		getContext().fullResolution().start();
		try {
			fullyResolveValues(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final StOp op(LocalBuilder builder) {

		final StOp op = this.op;

		if (op != null && op.getBuilder() == builder) {
			return op;
		}

		assert assertFullyResolved();

		return this.op = createOp(builder);
	}

	public final boolean assertFullyResolved() {
		assert this.fullyResolved :
			this + " is not fully resolved";
		return true;
	}

	protected abstract void fullyResolve(Resolver resolver);

	protected abstract void fullyResolveValues(Resolver resolver);

	protected abstract StOp createOp(LocalBuilder builder);

	protected final StOp noOp(LocalBuilder builder) {
		return noStOp(builder, this);
	}

}
