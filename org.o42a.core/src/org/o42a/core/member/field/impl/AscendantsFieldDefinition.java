/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Call;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;


public final class AscendantsFieldDefinition extends FieldDefinition {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder definitions;
	private Ref value;

	public AscendantsFieldDefinition(
			LocationInfo location,
			Distributor scope,
			AscendantsDefinition ascendants,
			BlockBuilder definitions) {
		super(location, scope);
		this.ascendants = ascendants;
		this.definitions = definitions;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return this.ascendants.getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.ascendants.updateAscendants(definer);
		definer.define(this.definitions);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {

		final DefinitionTarget target = getDefinitionTarget();

		if (target.isDefault() || target.is(definerTarget(definer))) {
			defineObject(definer);
			return;
		}

		definer.define(valueBlock(getValue()));
	}

	@Override
	public void defineLink(LinkDefiner definer) {

		final TypeRef ancestor = this.ascendants.getAncestor();

		definer.setTargetRef(getValue(), ancestor);
		definer.setParameters(new LinkTypeParameters(
				definer.getField().getDeclaration().getLinkType(),
				ancestor,
				getValue()));
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		definer.setRef(getValue());
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
			getLogger().noDefinition(getLocation());
		}

		return this.value = new Call(
				this,
				distribute(),
				this.ascendants,
				this.definitions).toRef();
	}

}