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

import static org.o42a.core.def.Rescoper.wrapper;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ref.type.TypeRef;


final class LinkFieldWrap extends FieldWrap<Link> {

	LinkFieldWrap(
			Container enclosingContainer,
			Field<?> type,
			Field<?> wrapped) {
		super(enclosingContainer, type, wrapped);
	}

	private LinkFieldWrap(Container enclosingContainer, FieldWrap<Link> overridden) {
		super(enclosingContainer, overridden);
	}

	@Override
	protected Link wrapArtifact() {
		return new Wrap(this);
	}

	@Override
	protected Field<Link> propagate(Scope enclosingScope) {
		return new LinkFieldWrap(enclosingScope.getContainer(), this);
	}

	@Override
	protected Link propagateArtifact(Field<Link> overridden) {
		return new Wrap(this, overridden.getArtifact());
	}

	private static final class Wrap extends Link {

		Wrap(LinkFieldWrap scope) {
			super(scope, scope.getArtifact().getKind());
		}

		Wrap(LinkFieldWrap scope, Link sample) {
			super(scope, sample);
		}

		@Override
		protected TypeRef buildTypeRef() {

			final TypeRef typeRef =
				field().getInterface().getArtifact().getTypeRef();

			return typeRef.rescope(wrapper(
					getScope().getEnclosingScope(),
					field().getWrapped().getScope().getEnclosingScope()));
		}

		@Override
		protected TargetRef buildTargetRef() {

			final TargetRef ref =
				field().getWrapped().getArtifact().getTargetRef();

			return ref.rescope(wrapper(
					getScope().getEnclosingScope(),
					field().getWrapped().getScope().getEnclosingScope()));
		}

		private LinkFieldWrap field() {
			return (LinkFieldWrap) getScope();
		}

	}

}
