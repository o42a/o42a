/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.object.array.impl;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;


public class RtArrayElement extends ArrayElement {

	private final Ref indexRef;

	public RtArrayElement(Scope arrayScope, Ref indexRef) {
		super(indexRef, indexRef.distributeIn(arrayScope.getContainer()));
		this.indexRef = indexRef;
	}

	@Override
	public final boolean isRuntime() {
		return true;
	}

	@Override
	public void resolveAll(Resolver resolver) {
		getTarget().resolveAll();
	}

	@Override
	public String toString() {
		if (this.indexRef == null) {
			return super.toString();
		}
		return getScope() + "[" + this.indexRef + ']';
	}

	@Override
	protected Obj createTarget() {
		return new RtArrayElementObject(this);
	}

	@Override
	protected ArrayElement findLinkIn(Scope enclosing) {
		return new RtArrayElement(enclosing, this.indexRef);
	}

}
