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
package org.o42a.core.def;

import static org.o42a.core.def.DefKind.PROPOSITION;
import static org.o42a.core.ref.InlineCond.INLINE_UNKNOWN;
import static org.o42a.util.func.Cancellation.cancelUpToNull;

import org.o42a.core.def.impl.InlineCondDefs;
import org.o42a.core.def.impl.RuntimeCondDef;
import org.o42a.core.ref.InlineCond;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Condition;
import org.o42a.util.ArrayUtil;


public final class CondDefs extends Defs<CondDef, CondDefs> {

	private Condition constant;

	CondDefs(DefKind defKind, CondDef... defs) {
		super(defKind, defs);
		assert !defKind.isValue() :
			"Condition definition kind expected";
	}

	public final boolean isFalse() {
		return getConstant().isFalse();
	}

	public final Condition getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		final CondDef[] defs = get();
		Condition result = null;
		int i = 0;

		while (i < defs.length) {

			final CondDef def = defs[i];
			final Condition constantValue = def.getConstantValue();

			if (constantValue.isUnknown()) {
				// Prerequisite not met - try next.
				++i;
				continue;
			}
			if (constantValue.isFalse()) {
				// Value is false.
				return this.constant = constantValue;
			}
			if (result == null || result.isConstant()) {
				// Indefinite value takes precedence.
				// But false value may appear later, so go on.
				result = constantValue;
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
			return this.constant = Condition.UNKNOWN;
		}

		return this.constant = result;
	}

	public Condition condition(Resolver resolver) {

		final CondDef[] defs = get();
		Condition result = null;
		int i = 0;

		while (i < defs.length) {

			final CondDef def = defs[i];
			final Condition condition = def.condition(resolver);

			if (condition.isUnknown()) {
				// Prerequisite not met - try next.
				++i;
				continue;
			}
			if (condition.isFalse()) {
				// Value is false.
				return condition;
			}
			if (result == null || result.isConstant()) {
				// Indefinite value takes precedence.
				// But false value may appear later, so go on.
				result = condition;
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
			return Condition.UNKNOWN;
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
				definitions.getValueStruct(),
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
				definitions.getValueStruct(),
				definitions.requirements(),
				newConditions,
				definitions.claims(),
				definitions.propositions());
	}

	final InlineCond inline(Normalizer normalizer) {
		if (isEmpty()) {
			return INLINE_UNKNOWN;
		}

		final CondDef[] defs = get();

		if (defs.length == 1) {
			return defs[0].inline(normalizer);
		}

		final InlineCond[] inlines = new InlineCond[defs.length];

		for (int i = 0; i < defs.length; ++i) {

			final InlineCond inline = defs[i].inline(normalizer);

			if (inline == null) {
				cancelUpToNull(inlines);
				return null;
			}
			inlines[i] = inline;
		}

		return new InlineCondDefs(this, inlines);
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
