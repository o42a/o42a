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

import static org.o42a.core.def.DefKind.*;
import static org.o42a.core.ref.Logical.logicalTrue;

import java.util.Collection;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.def.impl.rescoper.UpgradeRescoper;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.*;
import org.o42a.util.log.LogInfo;


public class Definitions extends Scoped {

	static final CondDefs NO_REQUIREMENTS = new CondDefs(REQUIREMENT);
	static final CondDefs NO_CONDITIONS = new CondDefs(CONDITION);
	static final ValueDefs NO_CLAIMS = new ValueDefs(CLAIM);
	static final ValueDefs NO_PROPOSITIONS = new ValueDefs(PROPOSITION);

	public static Definitions emptyDefinitions(
			LocationInfo location,
			Scope scope) {
		return new Empty(location, scope);
	}

	public static Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope,
			ValueStruct<?, ?> valueStruct) {

		final CondDefs conditions = new CondDefs(
				DefKind.CONDITION,
				logicalTrue(location, scope).toCondDef());

		return new Definitions(
				location,
				scope,
				valueStruct,
				NO_REQUIREMENTS,
				conditions,
				NO_CLAIMS,
				NO_PROPOSITIONS);
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

		ValueStruct<?, ?> valueStruct = null;
		int claimLen = 0;
		int defLen = 0;

		for (ValueDef definition : definitions) {
			definition.assertScopeIs(scope);
			if (valueStruct == null) {
				valueStruct = definition.getValueStruct();
			} else {

				final ValueStruct<?, ?> struct = definition.getValueStruct();

				if (struct != valueStruct) {
					scope.getContext().getLogger().incompatible(
							definition,
							valueStruct);
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
			if (valueStruct != definition.getValueStruct()) {
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
				valueStruct,
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				new ValueDefs(CLAIM, newClaims),
				new ValueDefs(PROPOSITION, newPropositions));
	}

	private final ValueStruct<?, ?> valueStruct;
	private final CondDefs requirements;
	private final CondDefs conditions;
	private final ValueDefs claims;
	private final ValueDefs propositions;

	private Value<?> constant;

	Definitions(
			LocationInfo location,
			Scope scope,
			ValueStruct<?, ?> valueStruct,
			CondDefs requirements,
			CondDefs conditions,
			ValueDefs claims,
			ValueDefs propositions) {
		super(location, scope);
		assert requirements.assertValid(scope, REQUIREMENT);
		assert conditions.assertValid(scope, CONDITION);
		assert claims.assertValid(scope, CLAIM);
		assert propositions.assertValid(scope, PROPOSITION);
		this.valueStruct = valueStruct;
		this.requirements = requirements;
		this.conditions = conditions;
		this.claims = claims;
		this.propositions = propositions;
		assert assertEmptyWithoutValues();
	}

	private Definitions(LocationInfo location, Scope scope) {
		this(
				location,
				scope,
				null,
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	Definitions(
			Definitions prototype,
			ValueStruct<?, ?> valueStruct,
			CondDefs requirements,
			CondDefs conditions,
			ValueDefs claims,
			ValueDefs propositions) {
		this(
				prototype,
				prototype.getScope(),
				valueStruct,
				requirements,
				conditions,
				claims,
				propositions);
	}

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public final boolean hasValues() {
		return getValueStruct() != null;
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
	}

	public boolean isEmpty() {
		return false;
	}

	public final boolean isConstant() {
		return getConstant().isDefinite();
	}

	public final Value<?> getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		final ValueStruct<?, ?> valueStruct =
				isEmpty() ? ValueStruct.VOID : getValueStruct();

		switch (requirements().getConstant()) {
		case RUNTIME:
			return valueStruct.runtimeValue();
		case FALSE:
			return valueStruct.falseValue();
		case TRUE:
		case UNKNOWN:
			switch (conditions().getConstant()) {
			case TRUE:
				break;
			case RUNTIME:
				return valueStruct.runtimeValue();
			case UNKNOWN:
			case FALSE:
				return valueStruct.falseValue();
			}
			break;
		}

		final Value<?> claim = claims().getConstant();

		if (!claim.isUnknown()) {
			return claim;
		}

		return propositions().getConstant();
	}

	public final CondDefs requirements() {
		return this.requirements;
	}

	public final CondDefs conditions() {
		return this.conditions;
	}

	public final ValueDefs claims() {
		return this.claims;
	}

	public final ValueDefs propositions() {
		return this.propositions;
	}

	public final Defs<?, ?> defs(DefKind defKind) {
		switch (defKind) {
		case REQUIREMENT:
			return requirements();
		case CONDITION:
			return conditions();
		case CLAIM:
			return claims();
		case PROPOSITION:
			return propositions();
		}
		throw new IllegalArgumentException(
				"Unknown definition kind: " + defKind);
	}

	public final boolean onlyClaims() {
		return propositions().isEmpty() && conditions().isEmpty();
	}

	public final boolean noClaims() {
		return claims().isEmpty() && requirements().isEmpty();
	}

	public Value<?> value(Resolver resolver) {

		final Condition requirement = requirements().condition(resolver);

		if (!requirement.isConstant()) {
			return requirement.toValue(valueStruct());
		}
		if (requirement.isFalse() && !requirement.isUnknown()) {
			return requirement.toValue(getValueStruct());
		}

		final Condition condition = conditions().condition(resolver);

		if (!condition.isConstant()) {
			return condition.toValue(valueStruct());
		}
		if (condition.isFalse()) {
			return condition.toValue(valueStruct());
		}

		final Value<?> claim = claims().value(resolver);

		if (!claim.isUnknown()) {
			return claim;
		}

		return propositions().value(resolver);
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
				return refineRequirements(new CondDefs(REQUIREMENT, condition));
			}
			if (requirements().imply(condition)) {
				return this;
			}

			return refineConditions(new CondDefs(CONDITION, condition));
		}

		final ValueDef value = refinement.toValue();
		final ValueStruct<?, ?> valueStruct = compatibleStruct(value);

		if (valueStruct.isNone()) {
			return this;
		}
		if (value.getPrerequisite().isFalse()) {
			return this;
		}
		if (value.isClaim()) {
			return refineClaims(valueStruct, new ValueDefs(CLAIM, value));
		}
		if (claims().imply(value)) {
			return this;
		}

		return refinePropositions(valueStruct, new ValueDefs(PROPOSITION, value));
	}

	public Definitions refine(Definitions refinements) {
		assertSameScope(refinements);
		if (refinements.isEmpty()) {
			return this;
		}

		final ValueStruct<?, ?> valueStruct = compatibleStruct(refinements);

		if (valueStruct != null && valueStruct.isNone()) {
			return this;
		}

		return refineRequirements(refinements.requirements())
				.refineConditions(refinements.conditions())
				.refineClaims(valueStruct, refinements.claims())
				.refinePropositions(valueStruct, refinements.propositions());
	}

	public Definitions override(Definitions overriders) {
		if (overriders.isEmpty()) {
			return this;
		}

		final ValueStruct<?, ?> valueStruct;

		if (hasValues()
				&& getValueStruct().isVoid()
				&& overriders.hasValues()) {
			// void can be overridden by non-void
			valueStruct = overriders.getValueStruct();
		} else if (overriders.hasValues()
				&& overriders.getValueStruct().isVoid()
				&& hasValues()) {
			// non-void can be overridden by void
			valueStruct = getValueStruct();
		} else {
			valueStruct = compatibleStruct(overriders);
			if (valueStruct != null && valueStruct.isNone()) {
				return this;
			}
		}

		if (overriders.propositions().isEmpty()) {
			// No propositions specified.
			if (overriders.conditions().isEmpty()) {
				// No condition specified.
				return refineRequirements(overriders.requirements())
						.refineClaims(valueStruct, overriders.claims());
			}
			return removeConditions()
					.refineRequirements(overriders.requirements())
					.refineConditions(overriders.conditions())
					.refineClaims(valueStruct, overriders.claims());
		}

		// Inherit claims, but not propositions.
		return removePropositions()
				.refineRequirements(overriders.requirements())
				.refineConditions(overriders.conditions())
				.refineClaims(valueStruct, overriders.claims())
				.refinePropositions(valueStruct, overriders.propositions());
	}

	public Definitions claim() {
		if (onlyClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getValueStruct(),
				conditions().claim(requirements()),
				NO_CONDITIONS,
				propositions().claim(claims()),
				NO_PROPOSITIONS);
	}

	public Definitions unclaim() {
		if (noClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getValueStruct(),
				NO_REQUIREMENTS,
				requirements().unclaim(conditions()),
				NO_CLAIMS,
				claims().unclaim(propositions()));
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
				getValueStruct(),
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				requirements().toValues());
	}

	public Definitions conditionPart(LocationInfo location) {
		return new Definitions(
				location,
				getScope(),
				getValueStruct(),
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				conditions().toValues());
	}

	public Definitions valuePart(LocationInfo location) {
		return new Definitions(
				this,
				getValueStruct(),
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				claims().unclaim(propositions()));
	}

	public Definitions claimPart(LocationInfo location) {
		return new Definitions(
				this,
				getValueStruct(),
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				claims().unclaim(NO_PROPOSITIONS));
	}

	public Definitions propositionPart(LocationInfo location) {
		return new Definitions(
				this,
				getValueStruct(),
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				propositions());
	}

	public Definitions runtime() {
		return new Definitions(
				this,
				getValueStruct(),
				requirements(),
				conditions().runtime(this),
				claims(),
				propositions());
	}

	public final void resolveAll() {
		getContext().fullResolution().start();
		try {
			requirements().resolveAll(this);
			conditions().resolveAll(this);
			claims().resolveAll(this);
			propositions().resolveAll(this);
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final ValueStruct<?, ?> valueStruct = getValueStruct();

		if (valueStruct != null) {
			out.append("Definitions(");
			out.append(valueStruct);
			out.append(")[");
		} else {
			out.append("Definitions[");
		}

		boolean comma = false;

		comma = this.requirements.defsToString(out, comma);
		comma = this.conditions.defsToString(out, comma);
		comma = this.claims.defsToString(out, comma);
		comma = this.propositions.defsToString(out, comma);

		out.append(']');

		return out.toString();
	}

	private boolean assertEmptyWithoutValues() {
		assert (hasValues()
				|| (propositions().isEmpty() && claims().isEmpty())) :
				"Non-empty definitions should have a value type";
		return true;
	}

	private final ValueStruct<?, ?> valueStruct() {
		return this.valueStruct != null ? this.valueStruct : ValueStruct.VOID;
	}

	private ValueStruct<?, ?> compatibleStruct(ValueDef refinement) {
		return compatibleStruct(refinement, refinement.getValueStruct());
	}

	private ValueStruct<?, ?> compatibleStruct(Definitions refinements) {
		return compatibleStruct(refinements, refinements.getValueStruct());
	}

	private ValueStruct<?, ?> compatibleStruct(
			LogInfo refinement,
			ValueStruct<?, ?> valueStruct) {
		if (valueStruct == null) {
			return getValueStruct();
		}
		if (getValueStruct() == null) {
			return valueStruct;
		}
		if (getValueStruct().assignableFrom(valueStruct)) {
			return getValueStruct();
		}

		getLogger().incompatible(refinement, getValueStruct());

		return ValueStruct.NONE;
	}

	private final Definitions refineRequirements(CondDefs refinements) {
		return requirements().refineRequirements(this, refinements);
	}

	private final Definitions refineConditions(CondDefs refinements) {
		return conditions().refineConditions(this, refinements);
	}

	private final Definitions refineClaims(
			ValueStruct<?, ?> valueStruct,
			ValueDefs refinements) {
		return claims().refineClaims(this, valueStruct, refinements);
	}

	private final Definitions refinePropositions(
			ValueStruct<?, ?> valueStruct,
			ValueDefs refinements) {
		return propositions().refinePropositions(
				this,
				valueStruct,
				refinements);
	}

	private Definitions removeConditions() {
		if (conditions().isEmpty()) {
			return this;
		}
		return new Definitions(
				this,
				this.valueStruct,
				requirements(),
				NO_CONDITIONS,
				claims(),
				propositions());
	}

	private Definitions removePropositions() {
		if (onlyClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getValueStruct(),
				requirements(),
				NO_CONDITIONS,
				claims(),
				NO_PROPOSITIONS);
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

}
