/*
    Compiler Core
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
package org.o42a.core.object.state;

import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.impl.KeeperObject;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public final class Keeper extends ObjectConstructor {

	private final Obj declaredIn;
	private final Ref ref;
	private final String name;
	private Keeper next;

	Keeper(Obj declaredIn, LocationInfo location, Ref ref, String name) {
		super(location, ref.distribute());
		this.declaredIn = declaredIn;
		this.name = name;
		this.ref = ref;
	}

	public final Obj getDeclaredIn() {
		return this.declaredIn;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return getRef().getValueType().typeRef(location, getScope());
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref);
	}

	@Override
	public final Keeper reproduce(PathReproducer reproducer) {
		getRef().assertCompatible(reproducer.getReproducingScope());

		final Ref ref = getRef().reproduce(reproducer.getReproducer());

		if (ref == null) {
			return null;
		}

		return reproducer.getScope().toObject().keepers().addKeeper(this, ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "//" + this.ref;
	}

	@Override
	protected Obj createObject() {
		return new KeeperObject(this);
	}

	final Keeper getNext() {
		return this.next;
	}

	final void setNext(Keeper next) {
		this.next = next;
	}

}
