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
import org.o42a.core.object.state.impl.KeeperAccessor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public final class Keeper extends ObjectConstructor implements SubID {

	private final Obj declaredIn;
	private final Ref value;
	private final ID id;
	private Keeper next;

	Keeper(Obj declaredIn, LocationInfo location, Ref value, ID id) {
		super(location, value.distribute());
		this.declaredIn = declaredIn;
		this.id = id;
		this.value = value;
		value.assertSameScope(this);
	}

	public final Obj getDeclaredIn() {
		return this.declaredIn;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {

		final ValueStruct<?, ?> valueStruct =
				getValue().valueStruct(getScope());

		return valueStruct.getValueType()
				.typeRef(location, getScope(), valueStruct.getParameters());
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref);
	}

	@Override
	public final Keeper reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref value = getValue().reproduce(reproducer.getReproducer());

		if (value == null) {
			return null;
		}

		return reproducer.getScope().toObject().keepers().keep(this, value);
	}

	@Override
	public final ID toID() {
		return this.id;
	}

	@Override
	public final ID toDisplayID() {
		return this.id;
	}

	public final void resolveAll() {

		final ObjectKeepers keepers = getDeclaredIn().keepers();

		keepers.keeperResolved(this);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	@Override
	protected Obj createObject() {
		return new KeeperAccessor(this);
	}

	final Keeper getNext() {
		return this.next;
	}

	final void setNext(Keeper next) {
		this.next = next;
	}

}