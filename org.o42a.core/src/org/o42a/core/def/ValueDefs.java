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

import static org.o42a.core.ref.InlineValue.inlineUnknown;

import org.o42a.core.def.impl.InlineValueDefs;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public final class ValueDefs extends Defs<ValueDef, ValueDefs> {

	private Value<?> constant;

	ValueDefs(DefKind defKind, ValueDef... defs) {
		super(defKind, defs);
		assert defKind.isValue() :
			"Value definition kind expected";
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return isEmpty() ? ValueStruct.VOID : get()[0].getValueStruct();
	}

	public final Value<?> getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		for (ValueDef def : get()) {

			final Value<?> constantValue = def.getConstantValue();

			if (!constantValue.getKnowledge().hasUnknownCondition()) {
				return this.constant = constantValue;
			}
		}

		return this.constant = getValueStruct().unknownValue();
	}

	public final Value<?> value(Resolver resolver) {
		for (ValueDef def : get()) {

			final Value<?> value = def.value(resolver);

			if (!value.getKnowledge().hasUnknownCondition()) {
				return value;
			}
		}

		return getValueStruct().unknownValue();
	}

	@Override
	final ValueDefs create(DefKind defKind, ValueDef[] defs) {
		return new ValueDefs(defKind, defs);
	}

	final Definitions refineClaims(
			Definitions definitions,
			ValueStruct<?, ?> valueStruct,
			ValueDefs refinements) {
		if (refinements.isEmpty()
				&& definitions.getValueStruct() == valueStruct) {
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
				valueStruct,
				definitions.requirements(),
				definitions.conditions(),
				newClaims,
				newPropositions);
	}

	final Definitions refinePropositions(
			Definitions definitions,
			ValueStruct<?, ?> valueStruct,
			ValueDefs refinements) {
		if (refinements.isEmpty()
				&& definitions.getValueStruct() == valueStruct) {
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
				valueStruct,
				definitions.requirements(),
				definitions.conditions(),
				definitions.claims(),
				newPropositions);
	}

	InlineValue inline(Normalizer normalizer, Definitions definitions) {

		final ValueStruct<?, ?> valueStruct = definitions.getValueStruct();

		if (isEmpty()) {
			return inlineUnknown(valueStruct);
		}

		final ValueDef[] defs = get();

		if (defs.length == 1) {
			return defs[0].inline(normalizer, valueStruct);
		}

		final InlineValue[] inlines = new InlineValue[defs.length];

		for (int i = 0; i < defs.length; ++i) {

			final InlineValue inline = defs[i].inline(normalizer, valueStruct);

			if (inline == null) {
				return null;
			}
			inlines[i] = inline;
		}

		return new InlineValueDefs(inlines);
	}

}

