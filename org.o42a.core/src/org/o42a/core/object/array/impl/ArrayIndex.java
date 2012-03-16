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

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.Reproducer;


public class ArrayIndex implements PathBinding<Ref> {

	private final Ref indexRef;

	public ArrayIndex(Ref indexRef) {
		this.indexRef = indexRef;
	}

	@Override
	public Ref getBound() {
		return this.indexRef;
	}

	@Override
	public PathBinding<Ref> modifyPath(PathModifier modifier) {

		final BoundPath indexPath =
				this.indexRef.getPath().modifyPath(modifier);

		if (indexPath == null) {
			return null;
		}

		return new ArrayIndex(
				indexPath.target(
						this.indexRef.distributeIn(
								indexPath.getOrigin().getContainer())));
	}

	@Override
	public ArrayIndex prefixWith(PrefixPath prefix) {
		return new ArrayIndex(this.indexRef.prefixWith(prefix));
	}

	@Override
	public ArrayIndex reproduce(Reproducer reproducer) {

		final Ref indexRef = this.indexRef.reproduce(reproducer);

		if (indexRef == null) {
			return null;
		}

		return new ArrayIndex(indexRef);
	}

	public BoundPath appendToPath(BoundPath path) {
		this.indexRef.assertScopeIs(path.getOrigin());
		return path.addBinding(this).append(new ArrayElementStep(this));
	}

	@Override
	public String toString() {
		if (this.indexRef == null) {
			return super.toString();
		}
		return this.indexRef.toString();
	}

}
