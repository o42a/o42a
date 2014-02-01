/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Scope;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;


public class SyntheticObjectConstructor extends ObjectConstructor {

	private final ObjectConstructor constructor;

	public SyntheticObjectConstructor(ObjectConstructor constructor) {
		super(constructor, constructor.distribute(), false);
		this.constructor = constructor;
	}

	@Override
	public boolean mayContainDeps() {
		return false;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return this.constructor.ancestor(location, ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return this.constructor.iface(ref);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return this.constructor.fieldDefinition(ref);
	}

	@Override
	public ObjectConstructor reproduce(PathReproducer reproducer) {

		final ObjectConstructor reproduced =
				this.constructor.reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return new SyntheticObjectConstructor(reproduced);
	}

	@Override
	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		throw new UnsupportedOperationException(
				"Synthetic object's value shoule never be used");
	}

	@Override
	public HostOp op(HostOp host) {
		throw new UnsupportedOperationException(
				"Synthetic object IR should never be constructed");
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected ObjectConstructor createStateful() {
		return this;
	}

	@Override
	protected Obj createObject() {
		return this.constructor.getConstructed();
	}

	@Override
	protected Obj propagateObject(Scope scope) {
		return this.constructor.resolve(scope);
	}

}
