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
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Ref;


public class RtArrayElement extends ArrayElement {

	private RtArrayElement propagatedFrom;
	private ItemLink artifact;

	public RtArrayElement(Ref indexRef) {
		super(indexRef, indexRef.distribute(), indexRef);
	}

	private RtArrayElement(Scope scope, RtArrayElement propagatedFrom) {
		super(
				propagatedFrom,
				propagatedFrom.distributeIn(scope.getContainer()),
				propagatedFrom.getIndexRef().upgradeScope(scope));
	}

	public RtArrayElement propagateTo(Scope scope) {
		return new RtArrayElement(scope, this);
	}

	@Override
	public ItemLink getArtifact() {
		if (this.artifact != null) {
			return this.artifact;
		}
		if (this.propagatedFrom != null) {
			return this.artifact = new ItemLink(this);
		}
		return this.artifact =
				this.propagatedFrom.getArtifact().propagateTo(this);
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		throw new UnsupportedOperationException();
	}

	private static final class ItemLink extends Link {

		private final TargetRef targetRef;

		ItemLink(RtArrayElement element) {
			super(
					element,
					element.isConstant()
					? ArtifactKind.LINK : ArtifactKind.VARIABLE);
			this.targetRef =
					new RtArrayElementConstructor(element)
					.toRef()
					.toTargetRef(element.getTypeRef());
		}

		private ItemLink(RtArrayElement element, ItemLink propagatedFrom) {
			super(element, propagatedFrom);
			this.targetRef = propagatedFrom.targetRef.upgradeScope(element);
		}

		public ItemLink propagateTo(RtArrayElement element) {
			return new ItemLink(element, this);
		}

		@Override
		protected TargetRef buildTargetRef() {
			return this.targetRef;
		}

	}

}
