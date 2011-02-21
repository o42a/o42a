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
package org.o42a.core.member.field;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public class ImplicitSamplesDefinition extends AscendantsDefinition {

	public ImplicitSamplesDefinition(
			LocationSpec location,
			Distributor distributor,
			StaticTypeRef sample) {
		this(location, distributor, null, sample);
	}

	protected ImplicitSamplesDefinition(
			LocationSpec location,
			Distributor distributor,
			TypeRef ancestor,
			StaticTypeRef... samples) {
		super(location, distributor, ancestor, samples);
	}

	@Override
	public Ascendants updateAscendants(Ascendants ascendants) {

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {
			ascendants = ascendants.setAncestor(ancestor);
		}
		for (StaticTypeRef sample : getSamples()) {
			ascendants = ascendants.addImplicitSample(sample);
		}

		return ascendants;
	}

	@Override
	public Ascendants updateAscendants(Scope scope, Ascendants ascendants) {

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {
			ascendants = ascendants.setAncestor(ancestor.rescope(scope));
		}
		for (StaticTypeRef sample : getSamples()) {
			ascendants = ascendants.addImplicitSample(sample.rescope(scope));
		}

		return ascendants;
	}

	@Override
	protected AscendantsDefinition create(
			LocationSpec location,
			Distributor distributor,
			TypeRef ancestor,
			StaticTypeRef[] samples) {
		return new ImplicitSamplesDefinition(
				location,
				distributor,
				ancestor,
				samples);
	}

}
