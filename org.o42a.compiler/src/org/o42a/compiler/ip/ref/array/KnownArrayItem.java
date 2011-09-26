/*
    Compiler
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
package org.o42a.compiler.ip.ref.array;

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;


public class KnownArrayItem extends ArrayItem {

	private final int index;
	private final Ref valueRef;

	public KnownArrayItem(int index, Ref valueRef) {
		super(
				valueRef,
				valueRef.distribute(),
				ValueStruct.INTEGER.constantRef(
						valueRef,
						valueRef.distribute(),
						Long.valueOf(index)));
		this.index = index;
		this.valueRef = valueRef;
	}

	public final Ref getValueRef() {
		return this.valueRef;
	}

	@Override
	public Link getArtifact() {
		return new ItemLink(this);
	}

	public KnownArrayItem reproduce(Reproducer reproducer) {
		getEnclosingScope().assertCompatible(reproducer.getReproducingScope());

		final Ref valueRef = getValueRef().reproduce(reproducer);

		if (valueRef == null) {
			return null;
		}

		return new KnownArrayItem(this.index, valueRef);
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		return new KnownArrayItemIR(generator, this);
	}

	private static final class ItemLink extends Link {

		private final KnownArrayItem item;

		ItemLink(KnownArrayItem item) {
			super(
					item,
					item.isConstant()
					? ArtifactKind.LINK : ArtifactKind.VARIABLE);
			this.item = item;
		}

		@Override
		protected TargetRef buildTargetRef() {
			return this.item.getValueRef().toTargetRef(this.item.getTypeRef());
		}

	}

}
