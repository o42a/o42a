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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.LogInfo;


public class Definitions extends Scoped {

	static final ValueDef[] NO_VALUES = new ValueDef[0];
	static final CondDef[] NO_CONDITIONS = new CondDef[0];

	public static Definitions emptyDefinitions(
			LocationInfo location,
			Scope scope) {
		return new Empty(location, scope);
	}

	public static Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope,
			ValueType<?> valueType) {
		return new Definitions(
				location,
				scope,
				valueType,
				NO_CONDITIONS,
				NO_CONDITIONS);
	}

	public static Definitions definitions(
			LocationInfo location,
			Scope scope,
			Collection<? extends ValueDef> definitions) {

		final int size = definitions.size();

		if (size == 0) {
			return emptyDefinitions(location, scope);
		}

		return definitions(
				location,
				scope,
				definitions.toArray(new ValueDef[size]));
	}

	public static Definitions definitions(
			LocationInfo location,
			Scope scope,
			ValueDef... definitions) {
		if (definitions.length == 0) {
			return emptyDefinitions(location, scope);
		}

		ValueType<?> valueType = null;
		int claimLen = 0;
		int defLen = 0;

		for (ValueDef definition : definitions) {
			definition.assertScopeIs(scope);
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

		final ValueDef[] newClaims = new ValueDef[claimLen];
		final ValueDef[] newPropositions = new ValueDef[defLen - claimLen];
		int claimIdx = 0;
		int propositionIdx = 0;

		for (ValueDef definition : definitions) {
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

		return new Definitions(
				location,
				scope,
				valueType,
				NO_CONDITIONS,
				NO_CONDITIONS,
				newClaims,
				newPropositions);
	}

	private final ValueType<?> valueType;
	private final CondDef[] requirements;
	private final CondDef[] conditions;
	private final ValueDef[] claims;
	private final ValueDef[] propositions;
	private LogicalValue constantRequirement;
	private LogicalValue constantCondition;

	Definitions(
			LocationInfo location,
			Scope scope,
			ValueType<?> valueType,
			CondDef[] requirements,
			CondDef[] conditions,
			ValueDef[] claims,
			ValueDef[] propositions) {
		super(location, scope);
		this.valueType = valueType;
		this.requirements = requirements;
		this.conditions = conditions;
		this.claims = claims;
		this.propositions = propositions;
		assert assertDefsScopeIs(requirements, scope);
		assert assertDefsScopeIs(conditions, scope);
		assert assertDefsScopeIs(claims, scope);
		assert assertDefsScopeIs(propositions, scope);
		assertEmptyWithoutValues();
	}

	private Definitions(LocationInfo location, Scope scope) {
		super(location, scope);
		this.valueType = null;
		this.requirements = this.conditions = NO_CONDITIONS;
		this.claims = this.propositions = NO_VALUES;
		assertEmptyWithoutValues();
	}

	private Definitions(
			LocationInfo location,
			Scope scope,
			ValueType<?> valueType,
			CondDef[] requirements,
			CondDef[] conditions) {
		super(location, scope);
		this.valueType = valueType;
		this.requirements = requirements;
		this.conditions = conditions;
		this.claims = this.propositions = NO_VALUES;
		assert assertDefsScopeIs(requirements, scope);
		assert assertDefsScopeIs(conditions, scope);
		assertEmptyWithoutValues();
	}

	private Definitions(
			Definitions prototype,
			ValueType<?> valueType,
			CondDef[] requirements,
			CondDef[] conditions,
			ValueDef[] claims,
			ValueDef[] propositions) {
		super(prototype, prototype.getScope());
		this.valueType = valueType;
		this.requirements = requirements;
		this.conditions = conditions;
		this.claims = claims;
		this.propositions = propositions;
		assert assertDefsScopeIs(requirements, getScope());
		assert assertDefsScopeIs(conditions, getScope());
		assert assertDefsScopeIs(claims, getScope());
		assert assertDefsScopeIs(propositions, getScope());
		assertEmptyWithoutValues();
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	public final CondDef[] getRequirements() {
		return this.requirements;
	}

	public final CondDef[] getConditions() {
		return this.conditions;
	}

	public final ValueDef[] getClaims() {
		return this.claims;
	}

	public final ValueDef[] getPropositions() {
		return this.propositions;
	}

	public boolean isEmpty() {
		return false;
	}

	public final boolean onlyClaims() {
		return this.propositions.length == 0 && this.conditions.length == 0;
	}

	public final boolean noClaims() {
		return this.claims.length == 0 && this.requirements.length == 0;
	}

	public final LogicalValue getConstantRequirement() {
		if (this.constantRequirement != null) {
			return this.constantRequirement;
		}
		return this.constantRequirement = constantValue(this.requirements);
	}

	public final LogicalValue getConstantCondition() {
		if (this.constantCondition != null) {
			return this.constantCondition;
		}
		return this.constantCondition = constantValue(this.conditions);
	}

	public final DefValue requirement(Scope scope) {
		return calculateCondition(scope, this.requirements);
	}

	public final DefValue claim(Scope scope) {
		return calculateValue(scope, this.claims);
	}

	public final DefValue proposition(Scope scope) {
		return calculateValue(scope, this.propositions);
	}

	public final DefValue condition(Scope scope) {
		return calculateCondition(scope, this.conditions);
	}

	public DefValue value(Scope scope) {

		final DefValue requirement = requirement(scope);

		if (requirement.isFalse() && !requirement.isUnknown()) {
			return requirement;
		}

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

	public Definitions refine(Def<?> refinement) {
		assertSameScope(refinement);
		if (!refinement.isValue()) {

			final CondDef condition = refinement.toCondition();

			if (condition.hasPrerequisite()) {
				if (condition.getPrerequisite().isFalse()) {
					return this;
				}
			}

			if (condition.isRequirement()) {
				return refineRequirements(new CondDef[] {condition});
			}
			if (impliedBy(refinement, this.requirements)) {
				return this;
			}

			return refineConditions(new CondDef[] {condition});
		}

		final ValueDef value = refinement.toValue();
		final ValueType<?> valueType = compatibleType(value);

		if (valueType == ValueType.NONE) {
			return this;
		}

		if (refinement.getPrerequisite().isFalse()) {
			return this;
		}
		if (value.isClaim()) {
			return refineClaims(valueType, new ValueDef[] {value});
		}
		if (impliedBy(refinement, this.claims)) {
			return this;
		}

		return refinePropositions(valueType, new ValueDef[] {value});
	}

	public Definitions refine(Definitions refinements) {
		assertSameScope(refinements);
		if (refinements.isEmpty()) {
			return this;
		}

		final ValueType<?> valueType = compatibleType(refinements);

		if (valueType == ValueType.NONE) {
			return this;
		}

		return refineRequirements(refinements.getRequirements())
		.refineConditions(refinements.getConditions())
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
			// No propositions specified.
			if (overriders.conditions.length == 0) {
				// No condition specified.
				return refineRequirements(overriders.getRequirements())
				.refineClaims(valueType, overriders.getClaims());
			}
			return removeConditions()
			.refineRequirements(overriders.getRequirements())
			.refineConditions(overriders.getConditions())
			.refineClaims(valueType, overriders.getClaims());
		}

		// Inherit claims, but not propositions.
		return removePropositions()
		.refineRequirements(overriders.getRequirements())
		.refineConditions(overriders.getConditions())
		.refineClaims(valueType, overriders.getClaims())
		.refinePropositions(valueType, overriders.getPropositions());
	}

	public Definitions claim() {
		if (onlyClaims()) {
			return this;
		}

		final CondDef[] requirements = Arrays.copyOf(
				this.requirements,
				this.requirements.length + this.conditions.length);
		int idx = this.requirements.length;

		for (CondDef condition : this.conditions) {
			requirements[idx++] = condition.claim();
		}

		final ValueDef[] claims = Arrays.copyOf(
				this.claims,
				this.claims.length + this.propositions.length);

		idx = this.claims.length;
		for (ValueDef proposition : this.propositions) {
			claims[idx++] = proposition.claim();
		}

		return new Definitions(
				this,
				getValueType(),
				requirements,
				NO_CONDITIONS,
				claims,
				NO_VALUES);
	}

	public Definitions unclaim() {
		if (noClaims()) {
			return this;
		}

		final CondDef[] conditions =
			new CondDef[this.requirements.length + this.conditions.length];
		int idx = 0;
		for (CondDef requirement : this.requirements) {
			conditions[idx++] = requirement.unclaim();
		}

		arraycopy(
				this.conditions,
				0,
				conditions,
				this.requirements.length,
				this.conditions.length);

		final ValueDef[] propositions = unclaimValues();

		return new Definitions(
				this,
				getValueType(),
				NO_CONDITIONS,
				conditions,
				NO_VALUES,
				propositions);
	}

	public Definitions upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return this;
		}
		assertCompatible(scope);

		final Definitions result =
			new UpgradeRescoper(getScope(), scope).update(this);

		result.assertScopeIs(scope);

		return result;
	}

	public Definitions requirementPart(LocationInfo location) {
		return new Definitions(
				location,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				toValues(this.requirements));
	}

	public Definitions conditionPart(LocationInfo location) {
		return new Definitions(
				location,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				toValues(this.conditions));
	}

	public Definitions valuePart(LocationInfo location) {
		return new Definitions(
				location,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				unclaimValues());
	}

	public Definitions claimPart(LocationInfo location) {

		final ValueDef[] propositions = new ValueDef[this.claims.length];

		for (int i = 0; i < propositions.length; ++i) {
			propositions[i] = this.claims[i].unclaim();
		}

		return new Definitions(
				location,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				propositions);
	}

	public Definitions propositionPart(LocationInfo location) {
		return new Definitions(
				location,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				this.propositions);
	}

	public Definitions runtime() {
		return new Definitions(
				this,
				getScope(),
				getValueType(),
				this.requirements,
				new CondDef[] {
					new RefCondDef(
							/* The source should differ from scope,
							 * as this definition is not explicit. */
							getScope().getContext().getVoid(),
							ValueType.VOID.runtimeRef(
									this,
									getScope().distribute()))
				},
				this.claims,
				NO_VALUES);
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

		boolean comma = false;

		comma = defsToString(out, comma, this.requirements);
		comma = defsToString(out, comma, this.conditions);
		comma = defsToString(out, comma, this.claims);
		comma = defsToString(out, comma, this.propositions);

		out.append(']');

		return out.toString();
	}

	private static boolean defsToString(
			StringBuilder out,
			boolean comma,
			Def<?>[] defs) {
		for (Def<?> def : defs) {
			if (comma) {
				out.append(", ");
			} else {
				comma = true;
			}
			out.append(def);
		}
		return comma;
	}

	private void assertEmptyWithoutValues() {
		assert (this.valueType != null
				|| (this.propositions.length == 0
						&& this.claims.length == 0)) :
				"Non-empty definitions should have a value type";
	}

	private ValueType<?> compatibleType(ValueDef refinement) {
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

	private Definitions removeConditions() {
		if (this.conditions.length == 0) {
			return this;
		}
		return new Definitions(
				this,
				this.valueType,
				this.requirements,
				NO_CONDITIONS,
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
				this.requirements,
				NO_CONDITIONS,
				this.claims,
				NO_VALUES);
	}

	private Definitions refineRequirements(CondDef[] requirements) {
		if (requirements.length == 0) {
			return this;
		}

		final CondDef[] newRequirements =
			addClaims(this.requirements, requirements);
		final CondDef[] newConditions =
			removeImpliedBy(this.conditions, requirements);

		if (newRequirements == this.requirements
				&& newConditions == this.conditions) {
			return this;
		}

		return new Definitions(
				this,
				this.valueType,
				newRequirements,
				newConditions,
				this.claims,
				this.propositions);
	}

	private Definitions refineConditions(CondDef[] refinements) {
		if (refinements.length == 0) {
			return this;
		}

		final CondDef[] newConditions =
			addPropositions(this.requirements, this.conditions, refinements);

		if (newConditions == this.conditions) {
			return this;
		}

		return new Definitions(
				this,
				this.valueType,
				this.requirements,
				newConditions,
				this.claims,
				this.propositions);
	}

	private Definitions refineClaims(
			ValueType<?> valueType,
			ValueDef[] claims) {
		if (claims.length == 0 && this.valueType == valueType) {
			return this;
		}

		final ValueDef[] newClaims = addClaims(this.claims, claims);
		final ValueDef[] newPropositions =
			removeImpliedBy(this.propositions, claims);

		if (newClaims == this.claims && newPropositions == this.propositions) {
			return this;
		}

		return new Definitions(
				this,
				valueType,
				this.requirements,
				this.conditions,
				newClaims,
				newPropositions);
	}

	private Definitions refinePropositions(
			ValueType<?> valueType,
			ValueDef[] refinements) {
		if (refinements.length == 0 && this.valueType == valueType) {
			return this;
		}

		final ValueDef[] newPropositions =
			addPropositions(this.claims, this.propositions, refinements);

		if (newPropositions == this.propositions) {
			return this;
		}

		return new Definitions(
				this,
				valueType,
				this.requirements,
				this.conditions,
				this.claims,
				newPropositions);
	}

	private static <D extends Def<D>> D[] addClaims(D[] claims, D[] defs) {

		final int len = claims.length;

		if (len == 0) {
			return defs;
		}

		@SuppressWarnings("unchecked")
		final D[] newClaims = (D[]) Array.newInstance(
				claims.getClass().getComponentType(),
				len + defs.length);
		int idx = 0;

		for (D def : defs) {

			final Logical prerequisite = def.getPrerequisite();

			for (int i = 0; i < len; ++i) {

				final D c1 = claims[i];
				final Logical prereq = c1.getPrerequisite();

				if (c1.hasPrerequisite() && prereq.implies(prerequisite)) {
					if (defs.length == 1) {
						return claims;
					}
				} else if (def.hasPrerequisite()
						&& prerequisite.implies(prereq)) {
					++i;
					for (; i < len; ++i) {

						final D c2 = claims[i];

						if (!prerequisite.implies(c2.getPrerequisite())) {
							newClaims[idx++] = c2;
						}
					}
					newClaims[idx++] = def;
					break;
				}
				newClaims[idx++] = c1;
			}
		}

		return ArrayUtil.clip(newClaims, idx);
	}

	private static <D extends Def<D>> D[] addPropositions(
			D[] claims,
			D[] propositions,
			D[] defs) {

		final int len = propositions.length;

		if (len == 0) {
			return defs;
		}

		final D[] newPropositions =
			Arrays.copyOf(propositions, len + defs.length);
		int idx = propositions.length;

		for (D proposition : defs) {
			if (impliedBy(proposition, propositions)) {
				continue;
			}
			if (impliedBy(proposition, claims)) {
				continue;
			}
			newPropositions[idx++] = proposition;
		}

		return ArrayUtil.clip(newPropositions, idx);
	}

	private static <D extends Def<D>> D[] removeImpliedBy(
			D[] defs,
			D[] existing) {

		final int len = defs.length;
		@SuppressWarnings("unchecked")
		final D[] newDefs =
			(D[]) Array.newInstance(defs.getClass().getComponentType(), len);
		int idx = 0;

		for (D def : defs) {
			if (!impliedBy(def, existing)) {
				newDefs[idx++] = def;
			}
		}
		if (idx == len) {
			return defs;
		}

		return ArrayUtil.clip(newDefs, idx);
	}

	private static boolean impliedBy(Def<?> def, Def<?>[] defs) {
		if (!def.hasPrerequisite()) {
			return false;
		}

		final Logical defLogical = def.getPrerequisite();

		for (Def<?> claim : defs) {
			if (claim.getPrerequisite().implies(defLogical)) {
				return true;
			}
		}
		return false;
	}

	private static LogicalValue constantValue(CondDef[] conditions) {
		for (CondDef condition : conditions) {

			final LogicalValue constantValue = condition.getConstantValue();

			if (!constantValue.isTrue()) {
				return constantValue;
			}
		}

		return LogicalValue.TRUE;
	}

	private DefValue calculateCondition(Scope scope, CondDef[] defs) {

		DefValue result = null;
		int i = 0;

		while (i < defs.length) {

			final CondDef def = defs[i];
			final DefValue value = def.definitionValue(scope);

			if (value.isUnknown()) {
				// Prerequisite not met - try next.
				++i;
				continue;
			}
			if (value.isFalse()) {
				// Value is false.
				return value;
			}
			if (result == null || result.isDefinite()) {
				// Indefinite value takes precedence.
				// But false value may appear later, so go on.
				result = value;
			}
			if (!def.hasPrerequisite()) {
				// All conditions without prerequisite should be met.
				++i;
				continue;
			}
			// Prerequisite met.
			// Skip the rest of alternatives and the following conditions
			// without prerequisites ('otherwise').
			i = nextNonPrereq(defs, i + 1);
			i = nextPrereq(defs, i + 1);
		}

		if (result == null) {
			return nonExistingValue(this);
		}

		return result;
	}

	private int nextNonPrereq(CondDef[] defs, int index) {
		while (index < defs.length) {
			if (!defs[index].hasPrerequisite()) {
				return index;
			}
			++index;
		}
		return index;
	}

	private int nextPrereq(CondDef[] defs, int index) {
		while (index < defs.length) {
			if (defs[index].hasPrerequisite()) {
				return index;
			}
			++index;
		}
		return index;
	}

	private DefValue calculateValue(Scope scope, ValueDef[] defs) {
		for (ValueDef def : defs) {

			final DefValue value = def.definitionValue(scope);

			if (!value.isUnknown()) {
				return value;
			}
		}

		return nonExistingValue(this);
	}

	private ValueDef[] unclaimValues() {

		final ValueDef[] propositions =
			new ValueDef[this.claims.length + this.propositions.length];

		int idx = 0;

		for (ValueDef claim : this.claims) {
			propositions[idx++] = claim.unclaim();
		}

		arraycopy(
				this.propositions,
				0,
				propositions,
				this.claims.length,
				this.propositions.length);

		return propositions;
	}

	private static ValueDef[] toValues(CondDef[] conditions) {

		final ValueDef[] values = new ValueDef[conditions.length];

		for (int i = 0; i < conditions.length; ++i) {
			values[i] = conditions[i].toValue().unclaim();
		}

		return values;
	}

	private static final class Empty extends Definitions {

		Empty(LocationInfo location, Scope scope) {
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

	private boolean assertDefsScopeIs(Def<?>[] defs, Scope scope) {
		for (Def<?> def : defs) {
			def.assertScopeIs(scope);
		}
		return true;
	}

}
