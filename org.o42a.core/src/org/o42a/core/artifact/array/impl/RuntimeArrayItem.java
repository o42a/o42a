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
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Ref;


public class RuntimeArrayItem extends ArrayItem {

	private ItemLink artifact;

	public RuntimeArrayItem(Ref indexRef) {
		super(indexRef, indexRef.distribute(), indexRef);
	}

	@Override
	public Link getArtifact() {
		if (this.artifact != null) {
			return this.artifact;
		}
		return this.artifact = new ItemLink(this);
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		throw new UnsupportedOperationException();
	}

	private static final class ItemLink extends Link {

		private final ArrayItem item;

		ItemLink(ArrayItem item) {
			super(
					item,
					item.isConstant()
					? ArtifactKind.LINK : ArtifactKind.VARIABLE);
			this.item = item;
		}

		@Override
		protected TargetRef buildTargetRef() {

			final RuntimeArrayItemConstructor ref =
					new RuntimeArrayItemConstructor(this.item);

			return ref.toTargetRef(this.item.getTypeRef());
		}

	}

}
