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
package org.o42a.core.object.def;

import static org.o42a.core.ir.op.InlineValue.inlineUnknown;

import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.impl.InlineValueDefs;
import org.o42a.core.object.def.impl.RuntimeDef;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.ArrayUtil;


public final class ValueDefs extends Defs<ValueDef, ValueDefs> {

	private Value<?> constant;

	ValueDefs(DefKind defKind, ValueDef... defs) {
		super(defKind, defs);
		assert defKind.isValue() :
			"Value definition kind expected";
	}

	public final boolean unconditional() {

		final ValueDef[] defs = get();
		final int length = defs.length;

		if (length == 0) {
			return false;
		}

		return defs[length - 1].unconditional();
	}

	public final Value<?> constant(Definitions definitions) {
		if (this.constant != null) {
			return this.constant;
		}

		for (ValueDef def : get()) {

			final Value<?> constantValue = def.getConstantValue();

			if (!constantValue.getKnowledge().hasUnknownCondition()) {
				return this.constant = constantValue;
			}
		}

		return this.constant = definitions.getValueStruct().unknownValue();
	}

	public final Value<?> value(Definitions definitions, Resolver resolver) {
		for (ValueDef def : get()) {

			final Value<?> value = def.value(resolver);

			if (!value.getKnowledge().hasUnknownCondition()) {
				return value;
			}
		}

		return definitions.getValueStruct().unknownValue();
	}

	@Override
	final ValueDefs create(DefKind defKind, ValueDef[] defs) {
		return new ValueDefs(defKind, defs);
	}

	final ValueDefs add(ValueDefs additions) {
		if (additions.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return additions;
		}
		if (unconditional()) {
			return this;
		}

		return new ValueDefs(
				getDefKind(),
				ArrayUtil.append(get(), additions.get()));
	}

	final Definitions refineClaims(
			Definitions definitions,
			ValueStruct<?, ?> valueStruct,
			ValueDefs refinements) {

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
			inlines[i] = defs[i].inline(normalizer, valueStruct);
		}

		return normalizer.isCancelled() ? null : new InlineValueDefs(inlines);
	}

	final ValueDefs runtime(Definitions definitions) {
		return new ValueDefs(
				getDefKind(),
				new RuntimeDef(definitions));
	}

	boolean upgradeValueStruct(
			Definitions definitions,
			ValueStruct<?, ?> valueStruct) {

		boolean ok = true;

		for (ValueDef def : get()) {
			if (!valueStruct.assignableFrom(def.getValueStruct())) {
				definitions.getLogger().incompatible(def, valueStruct);
			}
		}

		return ok;
	}

	final ValueDefs toVoid() {

		final ValueDef[] oldDefs = get();

		if (oldDefs.length == 0) {
			return this;
		}

		final ValueDef[] newDefs = new ValueDef[oldDefs.length];

		for (int i = 0; i < newDefs.length; ++i) {
			newDefs[i] = oldDefs[i].toVoid();
		}

		return new ValueDefs(getDefKind(), newDefs);
	}

	final void resolveTargets(TargetResolver wrapper) {
		for (ValueDef def : get()) {
			def.resolveTarget(wrapper);
		}
	}

	@Override
	void resolveAll(Definitions definitions) {
		validate(definitions.getValueStruct());
		super.resolveAll(definitions);
	}

	private void validate(ValueStruct<?, ?> valueStruct) {
		for (ValueDef def : get()) {
			if (!valueStruct.assignableFrom(def.getValueStruct())) {
				def.getContext().getLogger().incompatible(
						def,
						valueStruct);
			}
		}
	}

}

