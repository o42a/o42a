/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.*;
import org.o42a.core.def.SourceInfo;
import org.o42a.core.ref.type.TypeRef;


abstract class DefCollector<D extends SourceInfo> {

	public static boolean explicitDef(Obj object, SourceInfo sourceInfo) {

		final Obj source = sourceInfo.getSource();

		if (source == object) {
			return true;
		}

		final ObjectType sourceType = source.type();

		for (Sample sample : object.type().getSamples()) {
			if (sample.type(dummyUser()).derivedFrom(
					sourceType,
					Derivation.PROPAGATION)) {
				return true;
			}
		}

		return false;
	}

	private final Obj object;
	private final Obj ancestor;

	public DefCollector(Obj object) {
		this.object = object;

		final TypeRef ancestorRef = object.type().getAncestor();

		if (ancestorRef == null) {
			this.ancestor = null;
		} else {
			this.ancestor = ancestorRef.typeObject(dummyUser());
		}
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Obj getAncestor() {
		return this.ancestor;
	}

	public void addDef(D def) {
		if (explicitDef(this.object, def)) {
			explicitDef(def);
			return;
		}
		ancestorDef(def);
	}

	public void addDefs(D[] sources) {
		for (D source : sources) {
			addDef(source);
		}
	}

	protected abstract void explicitDef(D def);

	protected abstract void ancestorDef(D def);

}
