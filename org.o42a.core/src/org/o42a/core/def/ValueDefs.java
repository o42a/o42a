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

import static org.o42a.core.def.DefValue.nonExistingValue;
import static org.o42a.core.value.Value.unknownValue;

import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public final class ValueDefs extends Defs<ValueDef, ValueDefs> {

	private Value<?> constant;

	ValueDefs(DefKind defKind, ValueDef... defs) {
		super(defKind, defs);
		assert defKind.isValue() :
			"Value definition kind expected";
	}

	public final Value<?> getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		for (ValueDef def : get()) {

			final Value<?> constantValue = def.getConstantValue();

			if (!constantValue.isUnknown()) {
				return this.constant = constantValue;
			}
		}

		return this.constant = unknownValue();
	}

	public final DefValue resolve(Resolver resolver) {
		for (ValueDef def : get()) {

			final DefValue value = def.definitionValue(resolver);

			if (!value.isUnknown()) {
				return value;
			}
		}

		return nonExistingValue(this);
	}

	@Override
	final ValueDefs create(DefKind defKind, ValueDef[] defs) {
		return new ValueDefs(defKind, defs);
	}

	final Definitions refineClaims(
			Definitions definitions,
			ValueType<?> valueType,
			ValueDefs refinements) {
		if (refinements.isEmpty() && definitions.getValueType() == valueType) {
			return definitions;
		}

		final ValueDefs newClaims = addClaims(refinements);
		final ValueDefs oldPropositions = definitions.propositions();
		final ValueDefs newPropositions =
				oldPropositions.removeImpliedBy(refinements);

		if (newClaims == this && newPropositions == oldPropositions) {
			return definitions;
		}

		return new Definitions(
				definitions,
				valueType,
				definitions.requirements(),
				definitions.conditions(),
				newClaims,
				newPropositions);
	}

	final Definitions refinePropositions(
			Definitions definitions,
			ValueType<?> valueType,
			ValueDefs refinements) {
		if (refinements.isEmpty() && definitions.getValueType() == valueType) {
			return definitions;
		}

		final ValueDefs newPropositions = addPropositions(
				definitions.claims(),
				refinements);

		if (newPropositions == this) {
			return definitions;
		}

		return new Definitions(
				definitions,
				valueType,
				definitions.requirements(),
				definitions.conditions(),
				definitions.claims(),
				newPropositions);
	}
}

