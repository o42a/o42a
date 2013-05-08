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
package org.o42a.core.ref.path.impl.member;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueType;


final class TypeParameterConstructor extends ObjectConstructor {

	private final MemberKey parameterKey;

	TypeParameterConstructor(
			LocationInfo location,
			Distributor distributor,
			MemberKey parameterKey) {
		super(location, distributor);
		this.parameterKey = parameterKey;
	}

	public final MemberKey getParameterKey() {
		return this.parameterKey;
	}

	public final TypeRef ancestor(LocationInfo location) {
		return ValueType.MACRO.typeRef(location, getScope());
	}

	@Override
	public boolean mayContainDeps() {
		return false;
	}

	@Override
	public boolean isAllowedInsidePrototype() {
		return true;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return ancestor(location);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref, null);
	}

	@Override
	public ObjectConstructor reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new TypeParameterConstructor(
				this,
				reproducer.distribute(),
				this.parameterKey);
	}

	@Override
	public String toString() {
		if (this.parameterKey == null) {
			return super.toString();
		}
		return this.parameterKey.toString();
	}

	@Override
	protected Obj createObject() {
		return new TypeParameterObject(this);
	}

	@Override
	protected Obj propagateObject(Scope scope) {
		return new PropagatedTypeParameterObject(this, scope);
	}

}
