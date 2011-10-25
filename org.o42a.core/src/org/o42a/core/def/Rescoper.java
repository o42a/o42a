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
package org.o42a.core.def;

import static org.o42a.core.def.Definitions.emptyDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.def.impl.rescoper.CompoundRescoper;
import org.o42a.core.def.impl.rescoper.TransparentRescoper;
import org.o42a.core.def.impl.rescoper.UpgradeRescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;


public abstract class Rescoper {

	public static Rescoper transparentRescoper(Scope finalScope) {
		return new TransparentRescoper(finalScope);
	}

	public static Rescoper upgradeRescoper(Scope fromScope, Scope toScope) {
		if (fromScope == toScope) {
			return transparentRescoper(fromScope);
		}
		toScope.assertDerivedFrom(fromScope);
		return new UpgradeRescoper(fromScope, toScope);
	}

	private final Scope finalScope;

	public Rescoper(Scope finalScope) {
		this.finalScope = finalScope;
	}

	public final Scope getFinalScope() {
		return this.finalScope;
	}

	public abstract boolean isStatic();

	public boolean isTransparent() {
		return false;
	}

	public abstract <R extends Rescopable<R>> R update(R rescopable);

	public Definitions update(Definitions definitions) {

		final Scope resultScope = updateScope(definitions.getScope());

		if (definitions.isEmpty()) {
			return emptyDefinitions(definitions, resultScope);
		}

		final CondDefs requirements = definitions.requirements();
		final CondDefs newRequirements = requirements.rescope(this);
		final CondDefs conditions = definitions.conditions();
		final CondDefs newConditions = conditions.rescope(this);
		final ValueDefs claims = definitions.claims();
		final ValueDefs newClaims = claims.rescope(this);
		final ValueDefs propositions = definitions.propositions();
		final ValueDefs newPropositions = propositions.rescope(this);
		final ValueStruct<?, ?> valueStruct = definitions.getValueStruct();
		final ValueStruct<?, ?> newValueStruct =
				valueStruct != null ? valueStruct.rescope(this) : null;

		if (resultScope == definitions.getScope()
				// This may fail when there is no definitions.
				&& valueStruct == newValueStruct
				&& requirements == newRequirements
				&& conditions == newConditions
				&& claims == newClaims
				&& propositions == newPropositions) {
			return definitions;
		}

		return new Definitions(
				definitions,
				resultScope,
				newValueStruct,
				newRequirements,
				newConditions,
				newClaims,
				newPropositions);
	}

	public abstract Scope rescope(Scope scope);

	public abstract Resolver rescope(Resolver resolver);

	public abstract Scope updateScope(Scope scope);

	public Rescoper and(Rescoper other) {
		if (other.isTransparent()) {
			return this;
		}
		return new CompoundRescoper(this, other);
	}

	public abstract void resolveAll(Resolver resolver);

	public abstract Rescoper reproduce(Reproducer reproducer);

	public abstract HostOp rescope(CodeDirs dirs, HostOp host);

}
