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
package org.o42a.core.artifact.common;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.StandaloneArtifactScope;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.source.LocationInfo;


public abstract class MaterializableArtifactScope<
		A extends MaterializableArtifact<A>>
				extends StandaloneArtifactScope<A> {

	private final MaterializableArtifactScope<A> propagatedFrom;
	private ArtifactContainer<A> container;

	public MaterializableArtifactScope(
			LocationInfo location,
			Distributor enclosing) {
		super(location, enclosing);
		this.propagatedFrom = null;
	}

	protected MaterializableArtifactScope(
			Scope enclosing,
			MaterializableArtifactScope<A> propagatedFrom) {
		super(
				propagatedFrom,
				propagatedFrom.distributeIn(enclosing.getContainer()));
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public MemberContainer getContainer() {
		if (this.container != null) {
			return this.container;
		}
		return this.container = new ArtifactContainer<A>(this);
	}

	public MaterializableArtifactScope<A> getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public MaterializableArtifactScope<A> getFirstDeclaration() {

		final MaterializableArtifactScope<A> propagatedFrom =
				getPropagatedFrom();

		if (propagatedFrom == null) {
			return this;
		}

		return propagatedFrom.getFirstDeclaration();
	}

	@Override
	public MaterializableArtifactScope<A> getLastDefinition() {
		return getFirstDeclaration();
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		if (this == other) {
			return true;
		}

		return getArtifact().materialize().type().derivedFrom(
				other.getArtifact().materialize().type());
	}

}
