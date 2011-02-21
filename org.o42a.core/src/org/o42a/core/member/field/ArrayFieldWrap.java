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
package org.o42a.core.member.field;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.array.ArrayTypeRef;
import org.o42a.core.ref.type.TypeRef;


final class ArrayFieldWrap extends FieldWrap<Array> {

	ArrayFieldWrap(
			Container enclosingContainer,
			Field<?> type,
			Field<?> wrapped) {
		super(enclosingContainer, type, wrapped);
	}

	private ArrayFieldWrap(
			Container enclosingContainer,
			FieldWrap<Array> overridden) {
		super(enclosingContainer, overridden);
	}

	@Override
	protected Array wrapArtifact() {
		return new Wrap(this);
	}

	@Override
	protected Field<Array> propagate(Scope enclosingScope) {
		return new ArrayFieldWrap(enclosingScope.getContainer(), this);
	}

	@Override
	protected Array propagateArtifact(Field<Array> overridden) {
		return new Wrap(this, overridden.getArtifact());
	}

	private static final class Wrap extends Array {

		Wrap(ArrayFieldWrap scope) {
			super(scope);
		}

		Wrap(ArrayFieldWrap scope, Array sample) {
			super(scope, sample);
		}

		@Override
		protected ArrayTypeRef buildTypeRef() {
			return field().getInterface().getArtifact().getArrayTypeRef()
			.toScope(getScope().getEnclosingScope());
		}

		@Override
		protected TypeRef buildItemTypeRef() {
			return null;
		}

		@Override
		protected ArrayInitializer buildInitializer() {
			return field().getWrapped().getArtifact().getInitializer();
		}

		private ArrayFieldWrap field() {
			return (ArrayFieldWrap) getScope();
		}

	}

}
