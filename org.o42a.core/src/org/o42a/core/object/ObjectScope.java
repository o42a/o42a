/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.util.fn.Init.init;
import static org.o42a.util.fn.NullableInit.nullableInit;

import org.o42a.core.AbstractScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.fn.Init;
import org.o42a.util.fn.NullableInit;
import org.o42a.util.string.ID;


public abstract class ObjectScope extends AbstractScope {

	private final Init<Obj> object = init(this::createObject);
	private final NullableInit<Path> enclosingScopePath =
			nullableInit(this::createEnclosingPath);

	@Override
	public final Obj getContainer() {
		return toObject();
	}

	@Override
	public Path getEnclosingScopePath() {
		return this.enclosingScopePath.get();
	}

	@Override
	public final Obj toObject() {
		return this.object.get();
	}

	@Override
	public String toString() {

		final ID id = getId();

		if (id == null) {
			return super.toString();
		}

		return id.toString();
	}

	protected final Obj getScopeObject() {
		return this.object.get();
	}

	protected final Obj setScopeObject(Obj object) {
		this.object.set(object);
		return object;
	}

	protected abstract Obj createObject();

	private Path createEnclosingPath() {
		if (getEnclosingScope().isTopScope()) {
			return null;
		}
		return toObject().ownerPath().toPath();
	}

}
