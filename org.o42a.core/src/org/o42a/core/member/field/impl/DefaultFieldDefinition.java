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
package org.o42a.core.member.field.impl;

import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Call;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.ValueType;


public final class DefaultFieldDefinition extends FieldDefinition {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder definitions;
	private Ref value;

	public DefaultFieldDefinition(
			LocationInfo location,
			Distributor scope,
			AscendantsDefinition ascendants,
			BlockBuilder definitions) {
		super(location, scope);
		this.ascendants = ascendants;
		this.definitions = definitions;
	}

	@Override
	public boolean isLink() {

		final TypeRef ancestor = this.ascendants.getAncestor();

		if (ancestor != null) {

			final ValueType<?> valueType = ancestor.getValueType();

			if (!valueType.isVoid()) {
				return valueType.isLink();
			}
		}

		for (StaticTypeRef sample : this.ascendants.getSamples()) {

			final ValueType<?> valueType = sample.getValueType();

			if (!valueType.isVoid()) {
				return valueType.isLink();
			}
		}

		return false;
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.ascendants.updateAscendants(definer);
		definer.define(this.definitions);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		if (!linkDefiner(definer) || isLink()) {
			defineObject(definer);
			return;
		}
		definer.define(valueBlock(getValue()));
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(getValue(), this.ascendants.getAncestor());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.ascendants);
		out.append(this.definitions);

		return out.toString();
	}

	private Ref getValue() {
		if (this.value != null) {
			return this.value;
		}

		if (this.ascendants.isEmpty()) {
			getLogger().noDefinition(this);
		}

		return this.value = new Call(
				this,
				distribute(),
				this.ascendants,
				this.definitions).toRef();
	}

}
