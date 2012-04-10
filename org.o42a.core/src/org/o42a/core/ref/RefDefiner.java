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
package org.o42a.core.ref;

import static org.o42a.core.st.DefinitionTarget.valueDefinition;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.impl.RefEnv;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


public class RefDefiner extends AbstractDefiner {

	private ValueAdapter valueAdapter;

	RefDefiner(Ref ref, DefinerEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	@Override
	public DefinerEnv nextEnv() {
		return new RefEnv(this);
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return valueDefinition(getRef());
	}

	public ValueAdapter getValueAdapter() {
		if (this.valueAdapter != null) {
			return this.valueAdapter;
		}

		final ValueStruct<?, ?> expectedStruct = env().getExpectedValueStruct();

		return this.valueAdapter = getRef().valueAdapter(expectedStruct, true);
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return getValueAdapter().valueStruct(scope);
	}

	@Override
	public Definitions define(Scope scope) {
		if (getDefinitionTargets().isEmpty()) {
			return null;
		}

		final ValueDef def = getValueAdapter().valueDef();

		return env().apply(def).toDefinitions(env().getExpectedValueStruct());
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public String toString() {
		return '=' + super.toString();
	}

}

