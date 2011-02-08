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

import static java.lang.System.arraycopy;
import static org.o42a.core.def.DefValue.nonExistingValue;
import static org.o42a.core.def.LogicalDef.emptyLogicalDef;
import static org.o42a.core.def.LogicalDef.falseLogicalDef;
import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.disjunction;
import static org.o42a.core.ref.Logical.runtimeTrue;

import java.util.Collection;

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.LogInfo;


public class Definitions extends Scoped {

	static final Def[] NO_DEFS = new Def[0];

	public static Definitions emptyDefinitions(
			LocationSpec location,
			Scope scope) {
		return new Empty(location, scope);
	}

	public static Definitions noValueDefinitions(
			LocationSpec location,
			Scope scope,
			ValueType<?> valueType) {

		final LogicalDef condition = trueLogicalDef(location, scope);

		return new Definitions(
				location,
				scope,
				valueType,
				condition,
				condition);
	}

	public static Definitions conditionDefinitions(
			LocationSpec location,
			Scope scope,
			Logical condition) {
		return new Definitions(
				location,
				scope,
				null,
				trueLogicalDef(location, scope),
				condition.toLogicalDef());
	}

	public static Definitions requirementDefinitions(
			LocationSpec location,
			Scope scope,
			Logical requirement) {

		final LogicalDef requirementDef = requirement.toLogicalDef();

		return new Definitions(
				location,
				scope,
				null,
				requirementDef,
				requirementDef);
	}

	public static Definitions falseClaims(LocationSpec location, Scope scope) {

		final LogicalDef requirement = falseLogicalDef(location, scope);

		return new Definitions(location, scope, null, requirement, requirement);
	}

	public static Definitions definitions(
			LocationSpec location,
			Scope scope,
			Collection<? extends Def> definitions) {

		final int size = definitions.size();

		if (size == 0) {
			return emptyDefinitions(location, scope);
		}

		return definitions(
				location,
				scope,
				definitions.toArray(new Def[size]));
	}

	public static Definitions definitions(
			LocationSpec location,
			Scope scope,
			Def... definitions) {
		if (definitions.length == 0) {
			return emptyDefinitions(location, scope);
		}

		ValueType<?> valueType = null;
		int claimLen = 0;
		int defLen = 0;

		for (Def definition : definitions) {
			if (valueType == null) {
				valueType = definition.getValueType();
			} else {

				final ValueType<?> type = definition.getValueType();

				if (type != valueType) {
					scope.getContext().getLogger().incompatible(
							definition,
							valueType);
					continue;
				}
			}
			if (definition.getPrerequisite().isFalse()) {
				continue;// ignore definition with false prerequisite
			}
			defLen++;
			if (definition.isClaim()) {
				claimLen++;
			}
		}

		if (defLen == 0) {
			return emptyDefinitions(location, scope);
		}

		final Def[] newClaims = new Def[claimLen];
		final Def[] newPropositions = new Def[defLen - claimLen];
		int claimIdx = 0;
		int propositionIdx = 0;

		for (Def definition : definitions) {
			if (valueType != definition.getValueType()) {
				continue;
			}
			if (definition.getPrerequisite().isFalse()) {
				continue;
			}
			if (definition.isClaim()) {
				newClaims[claimIdx++] = definition;
			} else {
				newPropositions[propositionIdx++] = definition;
			}
		}

		final LogicalDef condition = emptyLogicalDef(location, scope);

		return new Definitions(
				location,
				scope,
				valueType,
				condition,
				condition,
				newClaims,
				newPropositions);
	}

	private final ValueType<?> valueType;
	private final LogicalDef requirement;
	private final LogicalDef condition;
	private final Def[] claims;
	private final Def[] propositions;

	Definitions(
			LocationSpec location,
			Scope scope,
			ValueType<?> valueType,
			LogicalDef requirement,
			LogicalDef condition,
			Def[] claims,
			Def[] propositions) {
		super(location, scope);
		this.valueType = valueType;
		this.requirement = requirement;
		this.condition = condition;
		this.claims = claims;
		this.propositions = propositions;
		assertEmptyWithoutDefinitions();
	}

	private Definitions(LocationSpec location, Scope scope) {
		super(location, scope);
		this.valueType = null;
		this.requirement = this.condition = emptyLogicalDef(location, scope);
		this.claims = this.propositions = NO_DEFS;
		assertEmptyWithoutDefinitions();
	}

	private Definitions(
			LocationSpec location,
			Scope scope,
			ValueType<?> valueType,
			LogicalDef requirement,
			LogicalDef condition) {
		super(location, scope);
		this.valueType = valueType;
		this.requirement = requirement;
		this.condition = condition;
		this.claims = this.propositions = NO_DEFS;
		assertEmptyWithoutDefinitions();
	}

	private Definitions(
			Definitions prototype,
			ValueType<?> valueType,
			LogicalDef requirement,
			LogicalDef condition,
			Def[] claims,
			Def[] propositions) {
		super(prototype, prototype.getScope());
		this.valueType = valueType;
		this.requirement = requirement;
		this.condition = condition;
		this.claims = claims;
		this.propositions = propositions;
		assertEmptyWithoutDefinitions();
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	public final LogicalDef getRequirement() {
		return this.requirement;
	}

	public final LogicalDef getCondition() {
		return this.condition;
	}

	public final Def[] getClaims() {
		return this.claims;
	}

	public final Def[] getPropositions() {
		return this.propositions;
	}

	public boolean isEmpty() {
		return false;
	}

	public final Logical fullLogical() {

		final int len =
			this.claims.length
			+ this.propositions.length;

		if (len == 0) {
			return this.condition.fullLogical();
		}

		final Logical[] result = new Logical[len];
		int idx = 0;

		for (Def claim : this.claims) {
			result[idx++] = claim.fullLogical();
		}
		for (Def proposition : this.propositions) {
			result[idx++] = proposition.fullLogical();
		}

		final Logical disjunction = disjunction(this, getScope(), result);

		return disjunction.and(this.condition.fullLogical());
	}

	public final boolean onlyClaims() {
		return (this.propositions.length == 0
				&& this.condition.fullLogical().sameAs(
						this.requirement.fullLogical()));
	}

	public final boolean noClaims() {
		return this.claims.length == 0 && this.requirement.isTrue();
	}

	public DefValue requirement(Scope scope) {
		if (getRequirement().isEmpty()) {
			return DefValue.nonExistingValue(this);
		}

		return DefValue.logicalValue(
				getRequirement(),
				getRequirement().logicalValue(scope),
				true);
	}

	public DefValue claim(Scope scope) {
		for (Def claim : this.claims) {

			final DefValue value = claim.definitionValue(scope);

			if (!value.isUnknown()) {
				return value;
			}
		}
		return nonExistingValue(this);
	}

	public DefValue proposition(Scope scope) {
		for (Def proposition : this.propositions) {

			final DefValue value = proposition.definitionValue(scope);

			if (!value.isUnknown()) {
				return value;
			}
		}
		return nonExistingValue(this);
	}

	public DefValue condition(Scope scope) {
		if (getCondition().isEmpty()) {
			return DefValue.nonExistingValue(this);
		}

		final DefValue requirement = requirement(scope);

		if (requirement.exists()) {
			if (!requirement.isUnknown()) {
				return DefValue.logicalValue(
						getCondition(),
						getCondition().logicalValue(scope),
						false);
			}
			if (requirement.isFalse()) {
				return requirement;
			}
		}

		return DefValue.logicalValue(
				getCondition(),
				getCondition().logicalValue(scope),
				false);
	}

	public DefValue value(Scope scope) {

		final DefValue condition = condition(scope);

		if (condition.isFalse() && !condition.isUnknown()) {
			return condition;
		}

		final DefValue value;
		final DefValue claim = claim(scope);

		if (!claim.isUnknown()) {
			value = claim;
		} else {
			value = proposition(scope);
		}
		if (value.isFalse()) {
			return value;
		}

		return value.and(condition);
	}

	public final Definitions addRequirement(Logical requirement) {
		if (requirement == null) {
			return this;
		}
		return addRequirement(requirement.toLogicalDef());
	}

	public Definitions addRequirement(LogicalDef requirement) {

		final LogicalDef newRequirement = this.requirement.and(requirement);

		if (newRequirement == this.requirement) {
			return this;
		}

		return new Definitions(
				this,
				getValueType(),
				newRequirement,
				this.condition.and(requirement),
				this.claims,
				this.propositions);
	}

	public Definitions addCondition(Logical condition) {
		if (condition == null) {
			return this;
		}
		return addCondition(condition.toLogicalDef());
	}

	public Definitions addCondition(LogicalDef condition) {

		final LogicalDef newCondition = this.condition.and(condition);

		if (newCondition == this.condition) {
			return this;
		}

		return new Definitions(
				this,
				getValueType(),
				this.requirement,
				newCondition,
				this.claims,
				this.propositions);
	}

	public Definitions addPrerequisite(LogicalDef prerequisite) {
		if (prerequisite == null || prerequisite.isTrue()) {
			return this;
		}

		final Def[] newClaims = addPrerequisite(this.claims, prerequisite);
		final Def[] newPropositions =
			addPrerequisite(this.propositions, prerequisite);

		if (this.claims == newClaims
				&& this.propositions == newPropositions) {
			return this;
		}

		return new Definitions(
				this,
				getValueType(),
				this.requirement,
				this.condition,
				newClaims,
				newPropositions);
	}

	public Definitions and(Logical condition) {
		if (condition == null || condition.isTrue()) {
			return this;
		}

		final Def[] newClaims = and(this.claims, condition);
		final Def[] newPropositions = and(this.propositions, condition);

		if (this.claims == newClaims
				&& this.propositions == newPropositions) {
			return this;
		}

		return new Definitions(
				this,
				getValueType(),
				this.requirement,
				this.condition,
				newClaims,
				newPropositions);
	}

	public Definitions refine(Def refinement) {

		final ValueType<?> valueType = compatibleType(refinement);

		if (valueType == ValueType.NONE) {
			return this;
		}

		if (refinement.getPrerequisite().isFalse()) {
			return this;
		}
		if (refinement.isClaim()) {
			return refineClaims(valueType, new Def[] {refinement});
		}
		if (impliedBy(refinement, this.claims)) {
			return this;
		}

		return refinePropositions(valueType, new Def[] {refinement});
	}

	public Definitions refine(Definitions refinements) {
		if (refinements.isEmpty()) {
			return this;
		}

		final ValueType<?> valueType = compatibleType(refinements);

		if (valueType == ValueType.NONE) {
			return this;
		}

		return refineConditions(valueType, refinements)
		.refineClaims(valueType, refinements.getClaims())
		.refinePropositions(valueType, refinements.getPropositions());
	}

	public Definitions override(Definitions overriders) {
		if (overriders.isEmpty()) {
			return this;
		}

		final ValueType<?> valueType;

		if (getValueType() == ValueType.VOID
				&& overriders.getValueType() != null) {
			// void can be overridden by non-void
			valueType = overriders.getValueType();
		} else if (
				overriders.getValueType() == ValueType.VOID
				&& getValueType() != null) {
			// non-void can be overridden by void
			valueType = getValueType();
		} else {
			valueType = compatibleType(overriders);
			if (valueType == ValueType.NONE) {
				return this;
			}
		}

		if (overriders.propositions.length == 0) {
			// no propositions specified
			if (overriders.condition.isEmpty()) {
				// no condition specified
				return refineConditions(valueType, overriders)
				.refineClaims(valueType, overriders.getClaims());
			}
			return removeCondition()
			.refineConditions(valueType, overriders)
			.refineClaims(valueType, overriders.getClaims());
		}

		// inherit claims, but not propositions
		return removePropositions()
		.refineConditions(valueType, overriders)
		.refineClaims(valueType, overriders.getClaims())
		.refinePropositions(valueType, overriders.getPropositions());
	}

	public Definitions claim() {
		if (onlyClaims()) {
			return this;
		}

		final Def[] claims =
			new Def[this.claims.length + this.propositions.length];

		arraycopy(this.claims, 0, claims, 0, this.claims.length);
		int idx = this.claims.length;

		for (Def proposition : this.propositions) {
			claims[idx++] = proposition.claim();
		}

		return new Definitions(
				this,
				getValueType(),
				this.condition,
				this.condition,
				claims,
				NO_DEFS);
	}

	public Definitions unclaim() {
		if (noClaims()) {
			return this;
		}

		final Def[] propositions =
			new Def[this.claims.length + this.propositions.length];
		int idx = 0;

		for (Def claim : this.claims) {
			propositions[idx++] = claim.unclaim();
		}

		arraycopy(
				this.propositions,
				0,
				propositions,
				idx,
				this.propositions.length);

		return new Definitions(
				this,
				getValueType(),
				trueLogicalDef(this, getScope()),
				this.condition,
				NO_DEFS,
				propositions);
	}

	public Definitions upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return this;
		}
		assertCompatible(scope);
		return new UpgradeRescoper(getScope(), scope).update(this);
	}

	public Definitions removeCondition(LocationSpec location) {
		if (this.condition.isTrue()) {
			return this;
		}

		final LogicalDef condition = emptyLogicalDef(this, getScope());

		return new Definitions(
				location,
				getScope(),
				getValueType(),
				condition,
				condition,
				this.claims,
				this.propositions);
	}

	public Definitions runtime(Scope scope) {
		return addCondition(new SingleLogicalDef(
				/* The source should differ from scope, as this logical
				 * definition is not explicit. */
				scope.getContext().getVoid(),
				runtimeTrue(scope, scope),
				transparentRescoper(scope)));
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final ValueType<?> valueType = getValueType();

		if (valueType != null) {
			out.append("Definitions(");
			out.append(valueType);
			out.append(")[");
		} else {
			out.append("Definitions[");
		}

		String space = "";

		if (!this.requirement.isTrue()) {
			out.append(this.requirement).append("!");
			space = " ";
		}
		if (!this.condition.isTrue()) {
			out.append(space).append(this.condition);
			space = ". ";
		}
		if (this.claims.length > 0) {
			out.append(space);
			for (int i = 0; i < this.claims.length; ++i) {
				if (i > 0) {
					out.append(',');
				}
				out.append(this.claims[i]);
			}
			out.append("!");
			space = " ";
		}
		if (this.propositions.length > 0) {
			out.append(space);
			for (int i = 0; i < this.propositions.length; ++i) {
				if (i > 0) {
					out.append(',');
				}
				out.append(this.propositions[i]);
			}
		}
		out.append(']');

		return out.toString();
	}

	private void assertEmptyWithoutDefinitions() {
		assert (this.valueType != null
				|| (this.propositions.length == 0
						&& this.claims.length == 0)) :
					"Non-empty definitions should have a value type";
	}

	private ValueType<?> compatibleType(Def refinement) {
		return compatibleType(refinement, refinement.getValueType());
	}

	private ValueType<?> compatibleType(Definitions refinements) {
		return compatibleType(refinements, refinements.getValueType());
	}

	private ValueType<?> compatibleType(
			LogInfo refinement,
			ValueType<?> valueType) {
		if (valueType == null) {
			return getValueType();
		}
		if (getValueType() == null) {
			return valueType;
		}
		if (getValueType() == valueType) {
			return valueType;
		}

		getLogger().incompatible(refinement, getValueType());

		return ValueType.NONE;
	}

	private Def[] addPrerequisite(Def[] defs, LogicalDef requirement) {
		if (defs.length == 0) {
			return defs;
		}

		final Def[] result = new Def[defs.length];
		boolean changed = false;

		for (int i = 0; i < defs.length; ++i) {

			final Def def = defs[i];
			final Def newDef = def.addPrerequisite(requirement);

			result[i] = newDef;
			changed = changed || def != newDef;
		}

		if (!changed) {
			return defs;
		}

		return result;
	}

	private Def[] and(Def[] defs, Logical condition) {
		if (defs.length == 0) {
			return defs;
		}

		final Def[] result = new Def[defs.length];
		boolean changed = false;

		for (int i = 0; i < defs.length; ++i) {

			final Def def = defs[i];
			final Def newDef = def.and(condition);

			result[i] = newDef;
			changed = changed || def != newDef;
		}

		if (!changed) {
			return defs;
		}

		return result;
	}

	private Definitions removeCondition() {
		if (this.condition.isTrue()) {
			return this;
		}
		return new Definitions(
				this,
				this.valueType,
				this.requirement,
				this.requirement,
				this.claims,
				this.propositions);
	}

	private Definitions removePropositions() {
		if (onlyClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getValueType(),
				this.requirement,
				this.requirement,
				this.claims,
				NO_DEFS);
	}

	private Definitions refineConditions(
			ValueType<?> valueType,
			Definitions refinements) {

		final LogicalDef newRequirement =
			this.requirement.and(refinements.requirement);
		final LogicalDef newCondition =
			this.condition.and(refinements.condition);

		if (newCondition == this.condition &&
				newRequirement == this.requirement) {
			return this;
		}

		return new Definitions(
					this,
					valueType,
					newRequirement,
					newCondition,
					this.claims,
					this.propositions);
	}

	private Definitions refineClaims(ValueType<?> valueType, Def[] claims) {
		if (claims.length == 0 && this.valueType == valueType) {
			return this;
		}

		final Def[] newClaims = addClaims(claims);
		final Def[] newPropositions =
			removeImpliedBy(this.propositions, claims);

		if (newClaims == this.claims && newPropositions == this.propositions) {
			return this;
		}

		return new Definitions(
				this,
				valueType,
				this.requirement,
				this.condition,
				newClaims,
				newPropositions);
	}

	private Definitions refinePropositions(
			ValueType<?> valueType,
			Def[] refinements) {
		if (refinements.length == 0 && this.valueType == valueType) {
			return this;
		}

		final Def[] newPropositions = addPropositions(refinements);

		if (newPropositions == this.propositions) {
			return this;
		}

		return new Definitions(
				this,
				valueType,
				this.requirement,
				this.condition,
				this.claims,
				newPropositions);
	}

	private Def[] addClaims(Def[] claims) {

		final int len = this.claims.length;

		if (len == 0) {
			return claims;
		}

		final Def[] newClaims = new Def[len + claims.length];
		int idx = 0;

		for (Def claim : claims) {

			final Logical prerequisite = claim.getPrerequisite().fullLogical();

			for (int i = 0; i < len; ++i) {

				final Def c1 = this.claims[i];
				final Logical prereq = c1.getPrerequisite().fullLogical();

				if (prereq.implies(prerequisite)) {
					if (claims.length == 1) {
						return this.claims;
					}
				} else if (prerequisite.implies(prereq)) {
					i++;
					for (; i < len; ++i) {

						final Def c2 = this.claims[i];

						if (!prerequisite.implies(
								c2.getPrerequisite().fullLogical())) {
							newClaims[idx++] = c2;
						}
					}
					newClaims[idx++] = claim;
					break;
				}
				newClaims[idx++] = c1;
			}
		}

		return ArrayUtil.clip(newClaims, idx);
	}

	private Def[] addPropositions(Def[] propositions) {

		final int len = this.propositions.length;

		if (len == 0) {
			return propositions;
		}

		final Def[] newPropositions = new Def[len + propositions.length];

		arraycopy(
				this.propositions,
				0,
				newPropositions,
				0,
				this.propositions.length);

		int idx = this.propositions.length;

		for (Def proposition : propositions) {
			if (impliedBy(proposition, this.propositions)) {
				continue;
			}
			if (impliedBy(proposition, this.claims)) {
				continue;
			}
			newPropositions[idx++] = proposition;
		}

		return ArrayUtil.clip(newPropositions, idx);
	}

	private Def[] removeImpliedBy(Def[] defs, Def[] existing) {

		final int len = this.propositions.length;
		final Def[] newPropositions = new Def[len];
		int idx = 0;

		for (Def def : defs) {
			if (!impliedBy(def, existing)) {
				newPropositions[idx++] = def;
			}
		}
		if (idx == len) {
			return defs;
		}

		return ArrayUtil.clip(newPropositions, idx);
	}

	private boolean impliedBy(Def def, Def[] defs) {
		for (Def claim : defs) {
			if (claim.getPrerequisite().fullLogical().implies(
					def.getPrerequisite().fullLogical())) {
				return true;
			}
		}
		return false;
	}

	private static final class Empty extends Definitions {

		Empty(LocationSpec location, Scope scope) {
			super(location, scope);
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String toString() {
			return "Empty Definitions";
		}

	}

}
