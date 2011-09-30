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
package org.o42a.core.artifact.array.impl;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


final class RtArrayElementConstructor extends ObjectConstructor {

	private final RtArrayElement element;

	RtArrayElementConstructor(RtArrayElement elementm) {
		super(elementm, elementm.getEnclosingScope().distribute());
		this.element = elementm;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.element.getTypeRef();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		reproducer.getLogger().notReproducible(this);
		return null;
	}

	@Override
	public String toString() {
		if (this.element == null) {
			return super.toString();
		}
		return this.element.toString();
	}

	@Override
	protected Obj createObject() {
		return new RtArrayElementObject(this.element);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new RtArrayElementOp(host, this);
	}

}
