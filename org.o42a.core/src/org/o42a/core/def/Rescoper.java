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

import java.lang.reflect.Array;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
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

		final CondDef[] requirements = definitions.getRequirements();
		final CondDef[] newRequirements = updateDefs(requirements);
		final CondDef[] conditions = definitions.getConditions();
		final CondDef[] newConditions = updateDefs(conditions);
		final ValueDef[] claims = definitions.getClaims();
		final ValueDef[] newClaims = updateDefs(claims);
		final ValueDef[] propositions = definitions.getPropositions();
		final ValueDef[] newPropositions = updateDefs(propositions);

		if (requirements == newRequirements
				&& conditions == newConditions
				&& claims == newClaims
				&& propositions == newPropositions) {
			if (resultScope == definitions.getScope()) {
				// This may fail when there is no definitions.
				return definitions;
			}
		}

		return new Definitions(
				definitions,
				resultScope,
				definitions.getValueType(),
				newRequirements,
				newConditions,
				newClaims,
				newPropositions);
	}

	public abstract Scope rescope(Scope scope);

	public abstract Resolver rescope(Resolver resolver);

	public abstract Scope updateScope(Scope scope);

	public <D extends Def<D>> D updateDef(D def) {
		return def.rescope(this);
	}

	public Rescoper and(Rescoper other) {
		if (other.isTransparent()) {
			return this;
		}
		return new CompoundRescoper(this, other);
	}

	public abstract void resolveAll(Resolver resolver);

	public abstract HostOp rescope(CodeDirs dirs, HostOp host);

	public abstract Rescoper reproduce(
			LocationInfo location,
			Reproducer reproducer);

	private <D extends Def<D>> D[] updateDefs(D[] defs) {

		for (int i = 0; i < defs.length; ++i) {

			final D def = defs[i];
			final D newDef = updateDef(def);

			newDef.assertScopeIs(getFinalScope());
			if (def == newDef) {
				continue;
			}

			@SuppressWarnings("unchecked")
			final D[] newDefs = (D[]) Array.newInstance(
					defs.getClass().getComponentType(),
					defs.length);

			System.arraycopy(defs, 0, newDefs, 0, i);
			newDefs[i++] = newDef;
			for (;i < defs.length; ++i) {
				newDefs[i] = updateDef(defs[i]);
			}

			return newDefs;
		}

		return defs;
	}

}
