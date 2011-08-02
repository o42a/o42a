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
package org.o42a.core.def;

import static org.o42a.core.def.DefKind.PROPOSITION;
import static org.o42a.core.def.DefValue.nonExistingValue;

import org.o42a.core.def.impl.RuntimeCondDef;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.ArrayUtil;


public final class CondDefs extends Defs<CondDef, CondDefs> {

	private LogicalValue constantValue;

	CondDefs(DefKind defKind, CondDef... defs) {
		super(defKind, defs);
		assert !defKind.isValue() :
			"Condition definition kind expected";
	}

	public final boolean isFalse() {
		return getConstant().isFalse();
	}

	public final LogicalValue getConstant() {
		if (this.constantValue != null) {
			return this.constantValue;
		}

		for (CondDef condition : get()) {

			final LogicalValue constantValue = condition.getConstantValue();

			if (!constantValue.isTrue()) {
				return this.constantValue = constantValue;
			}
		}

		return this.constantValue = LogicalValue.TRUE;
	}

	public DefValue resolve(Resolver resolver) {

		final CondDef[] defs = get();
		DefValue result = null;
		int i = 0;

		while (i < defs.length) {

			final CondDef def = defs[i];
			final DefValue value = def.definitionValue(resolver);

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

	@Override
	final CondDefs create(DefKind defKind, CondDef[] defs) {
		return new CondDefs(defKind, defs);
	}

	final ValueDefs toValues() {
		if (isEmpty()) {
			return Definitions.NO_PROPOSITIONS;
		}

		final CondDef[] conditions = get();
		final ValueDef[] values = new ValueDef[conditions.length];

		for (int i = 0; i < conditions.length; ++i) {
			values[i] = conditions[i].toValue().unclaim();
		}

		return new ValueDefs(PROPOSITION, values);
	}

	final CondDefs runtime(Definitions definitions) {
		return new CondDefs(
				getDefKind(),
				ArrayUtil.prepend(
						new RuntimeCondDef(definitions),
						get()));
	}

	final Definitions refineRequirements(
			Definitions definitions,
			CondDefs refinements) {
		if (refinements.isEmpty()) {
			return definitions;
		}

		final CondDefs newRequirements = addClaims(refinements);
		final CondDefs oldConditions = definitions.conditions();
		final CondDefs newConditions =
				oldConditions.removeImpliedBy(refinements);

		if (newRequirements == this
				&& newConditions == definitions.conditions()) {
			return definitions;
		}

		return new Definitions(
				definitions,
				definitions.getValueType(),
				newRequirements,
				newConditions,
				definitions.claims(),
				definitions.propositions());
	}

	final Definitions refineConditions(
			Definitions definitions,
			CondDefs refinements) {
		if (refinements.isEmpty()) {
			return definitions;
		}

		final CondDefs newConditions = addPropositions(
				definitions.requirements(),
				refinements);

		if (newConditions == this) {
			return definitions;
		}

		return new Definitions(
				definitions,
				definitions.getValueType(),
				definitions.requirements(),
				newConditions,
				definitions.claims(),
				definitions.propositions());
	}

	private static int nextNonPrereq(CondDef[] defs, int start) {

		int index = start;

		while (index < defs.length) {
			if (!defs[index].hasPrerequisite()) {
				return index;
			}
			++index;
		}

		return index;
	}

	private static int nextPrereq(CondDef[] defs, int start) {

		int index = start;

		while (index < defs.length) {
			if (defs[index].hasPrerequisite()) {
				return index;
			}
			++index;
		}

		return index;
	}

}
