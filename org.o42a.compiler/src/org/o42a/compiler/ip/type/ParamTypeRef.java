/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.type;

import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParametersBuilder;


public final class ParamTypeRef {

	private final TypeRef typeRef;
	private final TypeParametersBuilder parameters;

	public ParamTypeRef(TypeRef typeRef) {
		this(typeRef, null);
	}

	public ParamTypeRef(
			TypeRef typeRef,
			TypeParametersBuilder parameters) {
		assert typeRef != null :
			"Type not specified";
		this.typeRef = typeRef;
		this.parameters = parameters;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final TypeParametersBuilder getParameters() {
		return this.parameters;
	}

	public final AscendantsDefinition updateAncestor(
			AscendantsDefinition ascendants) {

		final AscendantsDefinition result =
				ascendants.setAncestor(getTypeRef());
		final TypeParametersBuilder parameters = getParameters();

		if (parameters == null) {
			return result;
		}

		return result.setTypeParameters(parameters.toObjectTypeParameters());
	}

	public final Phrase updateAncestor(Phrase phrase) {

		final Phrase result = phrase.setAncestor(getTypeRef());
		final TypeParametersBuilder parameters = getParameters();

		if (parameters == null) {
			return result;
		}

		return result.setTypeParameters(parameters.toObjectTypeParameters());
	}

	public final TypeRef parameterize() {

		final TypeParametersBuilder parameters = getParameters();

		if (parameters == null) {
			return getTypeRef();
		}

		return getTypeRef().setParameters(parameters);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		if (this.parameters == null) {
			return this.typeRef.toString();
		}
		return this.typeRef.toString() + this.parameters.toString();
	}

}
