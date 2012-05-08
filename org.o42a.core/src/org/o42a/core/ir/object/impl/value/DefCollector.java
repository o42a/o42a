/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl.value;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Defs;
import org.o42a.core.object.def.SourceInfo;
import org.o42a.core.object.type.Derivation;
import org.o42a.core.ref.type.TypeRef;


final class DefCollector {

	public static boolean explicitDef(Obj object, SourceInfo sourceInfo) {

		final Obj source = sourceInfo.getSource();

		if (source == object) {
			return true;
		}

		return object.type().derivedFrom(source.type(), Derivation.PROPAGATION);
	}

	private final Obj object;
	private final Obj ancestor;
	private final Def[] explicitDefs;
	private int size;
	private int ancestorIndex = -1;

	DefCollector(Obj object, int capacity) {
		this.object = object;
		this.explicitDefs = new Def[capacity];

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

	public final int getAncestorIndex() {
		return this.ancestorIndex;
	}

	public final Def[] getExplicitDefs() {
		return this.explicitDefs;
	}

	public final int size() {
		return this.size;
	}

	public final void addDef(Def def) {
		if (explicitDef(this.object, def)) {
			explicitDef(def);
			return;
		}
		ancestorDef();
	}

	public final void addDefs(Defs defs) {
		for (Def source : defs.get()) {
			addDef(source);
		}
	}

	public final void addDefs(Def[] sources) {
		for (Def source : sources) {
			addDef(source);
		}
	}

	protected void explicitDef(Def def) {
		this.explicitDefs[this.size++] = def;
	}

	protected void ancestorDef() {
		if (this.ancestorIndex >= 0) {
			return;
		}
		this.ancestorIndex = this.size;
		++this.size;
	}

}
