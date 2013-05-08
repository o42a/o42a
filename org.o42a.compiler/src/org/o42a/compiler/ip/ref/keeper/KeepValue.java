/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class KeepValue extends ObjectConstructor {

	private Ref value;

	public KeepValue(LocationInfo location, Ref value) {
		super(location, value.distribute());
		this.value = value;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return this.value.getValueTypeInterface().setLocation(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new KeptValueFieldDefinition(ref, this);
	}

	@Override
	public ObjectConstructor reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref value = getValue().reproduce(reproducer.getReproducer());

		if (value == null) {
			return null;
		}

		return new KeepValue(this, value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "//" + this.value;
	}

	@Override
	protected Obj createObject() {
		return new KeptValue(this);
	}

}
