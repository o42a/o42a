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
package org.o42a.core.artifact.object;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.UseCase;


public class ObjectAnalysis {

	private final Obj object;
	private byte staticAncestor;

	ObjectAnalysis(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final boolean hasStaticAncestor() {
		if (this.staticAncestor != 0) {
			return this.staticAncestor > 0;
		}

		final TypeRef ancestor =
			getObject().type().useBy(dummyUser()).getAncestor();

		if (ancestor == null || ancestor.isStatic()) {
			this.staticAncestor = 1;
			return true;
		}

		this.staticAncestor = -1;
		return false;
	}

	public final boolean accessedBy(UseCase useCase) {
		return getObject().content().isUsedBy(useCase);
	}

	public final boolean typeAccessedBy(UseCase useCase) {
		return getObject().type().isUsedBy(useCase);
	}

	public final boolean valueAccessedBy(UseCase useCase) {
		return getObject().value().isUsedBy(useCase);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectAnalysis[" + this.object + ']';
	}

}
