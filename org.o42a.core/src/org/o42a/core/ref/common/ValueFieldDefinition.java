/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ref.common;

import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.value.ObjectTypeParameters;


public class ValueFieldDefinition extends DefaultFieldDefinition {

	private final TypeRefParameters typeParameters;

	public ValueFieldDefinition(Ref ref, TypeRefParameters typeParameters) {
		super(ref);
		this.typeParameters = typeParameters;
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return refDefinitionTarget(getRef());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(ancestor());
		if (this.typeParameters != null) {
			definer.setParameters(typeParameters(definer));
		}
		refAsValue(definer);
	}

	@Override
	public void overridePlainObject(ObjectDefiner definer) {
		definer.setAncestor(ancestor());
		if (this.typeParameters != null) {
			definer.setParameters(typeParameters(definer));
		}
		super.overridePlainObject(definer);
	}

	protected TypeRef ancestor() {
		return getRef().ancestor(this);
	}

	private ObjectTypeParameters typeParameters(ObjectDefiner definer) {
		return this.typeParameters
				.rescope(definer.getField())
				.toObjectTypeParameters();
	}

}
