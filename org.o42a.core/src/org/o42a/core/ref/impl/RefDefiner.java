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
package org.o42a.core.ref.impl;

import static org.o42a.core.st.DefinitionTarget.valueDefinition;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.ValueDef;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.ValueStruct;


public class RefDefiner extends Definer {

	private Ref expectedTypeAdapter;

	public RefDefiner(Ref ref, StatementEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	@Override
	public StatementEnv nextEnv() {
		return new RefEnv(this);
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return valueDefinition(getRef());
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return expectedTypeAdapter().valueStruct(scope);
	}

	@Override
	public Definitions define(Scope scope) {
		if (getDefinitionTargets().isEmpty()) {
			return null;
		}

		final ValueDef def = expectedTypeAdapter().toValueDef();

		return env().apply(def).toDefinitions();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return new ReturnValue(this, resolver, getRef().value(resolver));
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		return new ExecuteCommand(
				this,
				getRef().value(resolver).getCondition().toLogicalValue());
	}

	public final Ref expectedTypeAdapter() {
		if (this.expectedTypeAdapter != null) {
			return this.expectedTypeAdapter;
		}

		final ValueStruct<?, ?> expectedStruct =
				env().getExpectedValueStruct();

		if (expectedStruct == null) {
			return this.expectedTypeAdapter = getRef();
		}

		return this.expectedTypeAdapter = getRef().adapt(
				this,
				expectedStruct.getValueType().typeRef(this, getScope()));
	}

	@Override
	public String toString() {
		return '=' + super.toString();
	}

}

