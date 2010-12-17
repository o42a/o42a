/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;


public abstract class Rescoper {

	public static Rescoper transparentRescoper(Scope finalScope) {
		return new TransparentRescoper(finalScope);
	}

	public static Rescoper upgradeRescoper(Scope fromScope, Scope toScope) {
		toScope.assertDerivedFrom(fromScope);
		return new UpgradeRescoper(fromScope, toScope);
	}

	public static Rescoper wrapper(Scope scope, Scope wrapped) {
		return new Wrapper(scope, wrapped);
	}

	private final Scope finalScope;

	public Rescoper(Scope finalScope) {
		this.finalScope = finalScope;
	}

	public final Scope getFinalScope() {
		return this.finalScope;
	}

	public boolean isTransparent() {
		return false;
	}

	public Path getPath() {
		return null;
	}

	public Definitions update(Definitions definitions) {

		final Scope resultScope = updateScope(definitions.getScope());

		if (definitions.isEmpty()) {
			return emptyDefinitions(definitions, resultScope);
		}

		final Def[] claims = definitions.getClaims();
		final Def[] newClaims = new Def[claims.length];
		final Def[] propositions = definitions.getPropositions();
		final Def[] newPropositions = new Def[propositions.length];
		boolean changed = false;

		for (int i = 0; i < claims.length; ++i) {

			final Def claim = claims[i];
			final Def newClaim = updateDef(claim);

			newClaims[i] = newClaim;
			changed |= newClaim != claim;
		}
		for (int i = 0; i < propositions.length; ++i) {

			final Def proposition = propositions[i];
			final Def newProposition = updateDef(proposition);

			newPropositions[i] = newProposition;
			changed |= newProposition != proposition;
		}

		final CondDef requirement = definitions.getRequirement();
		final CondDef newRequirement = requirement.rescope(this);

		changed = changed || requirement != newRequirement;

		final CondDef postCondition = definitions.getPostCondition();
		final CondDef newPostCondition = postCondition.rescope(this);

		changed |= postCondition != newPostCondition;

		if (!changed) {
			return definitions;
		}

		return new Definitions(
				definitions,
				resultScope,
				definitions.getValueType(),
				newRequirement,
				newPostCondition,
				newClaims,
				newPropositions);
	}

	public abstract Scope rescope(Scope scope);

	public abstract Scope updateScope(Scope scope);

	public Def updateDef(Def def) {
		return def.rescope(this);
	}

	public Rescoper and(Rescoper other) {
		if (other.isTransparent()) {
			return this;
		}
		return new CompoundRescoper(this, other);
	}

	public abstract HostOp rescope(Code code, CodePos exit, HostOp host);

	public abstract Rescoper reproduce(
			LocationSpec location,
			Reproducer reproducer);

}
