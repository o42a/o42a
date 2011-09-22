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

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


final class RuntimeArrayItemConstructor extends ObjectConstructor {

	private final ArrayItem item;

	RuntimeArrayItemConstructor(ArrayItem item) {
		super(item, item.getEnclosingScope().distribute());
		this.item = item;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.item.getTypeRef();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		reproducer.getLogger().notReproducible(this);
		return null;
	}

	@Override
	public String toString() {
		if (this.item == null) {
			return super.toString();
		}
		return this.item.toString();
	}

	@Override
	protected Obj createObject() {
		return new ItemObject(this.item);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new RuntimeArrayItemOp(host, this);
	}

	private static final class ItemObject extends Obj {

		private final ArrayItem item;

		public ItemObject(ArrayItem item) {
			super(item, item.distribute());
			this.item = item;
		}

		@Override
		public ConstructionMode getConstructionMode() {
			return ConstructionMode.RUNTIME_CONSTRUCTION;
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).setAncestor(this.item.getTypeRef());
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
		}

		@Override
		protected Definitions explicitDefinitions() {
			return null;
		}

		@Override
		protected ObjectIR createIR(Generator generator) {
			throw new UnsupportedOperationException();
		}

	}

}
