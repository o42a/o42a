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
package org.o42a.core.artifact.array;

import static org.o42a.core.Rescoper.upgradeRescoper;

import org.o42a.core.Distributor;
import org.o42a.core.Rescoper;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.MaterializableArtifactScope;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public abstract class ArrayElement extends MaterializableArtifactScope<Link> {

	private final Ref indexRef;
	private final Obj owner;

	public ArrayElement(
			LocationInfo location,
			Distributor enclosing,
			Ref indexRef) {
		super(location, enclosing);
		indexRef.assertScopeIs(enclosing.getScope());
		this.indexRef = indexRef;
		this.owner = enclosing.getScope().toObject();
		assert this.owner != null :
			"Enclosing scope is not object: " + enclosing.getScope();
	}

	protected ArrayElement(
			Scope enclosing,
			ArrayElement propagatedFrom) {
		this(
				enclosing,
				propagatedFrom,
				upgradeRescoper(propagatedFrom.getEnclosingScope(), enclosing));
	}

	ArrayElement(
			Scope enclosing,
			ArrayElement propagatedFrom,
			Rescoper rescoper) {
		super(enclosing, propagatedFrom);
		this.indexRef = propagatedFrom.getIndexRef().rescope(rescoper);
		this.owner = enclosing.getScope().toObject();
		assert this.owner != null :
			"Enclosing scope is not object: " + enclosing.getScope();
	}

	public final Obj getOwner() {
		return this.owner;
	}

	public final ArrayValueStruct getArrayStruct() {
		return (ArrayValueStruct) getOwner().value().getValueStruct();
	}

	public final boolean isConstant() {
		return getArrayStruct().isConstant();
	}

	public final TypeRef getTypeRef() {
		return getArrayStruct().getItemTypeRef();
	}

	public final Ref getIndexRef() {
		return this.indexRef;
	}

	@Override
	public abstract Link getArtifact();

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public ArrayElement getPropagatedFrom() {
		return (ArrayElement) super.getPropagatedFrom();
	}

	@Override
	public ArrayElement getFirstDeclaration() {
		return (ArrayElement) super.getFirstDeclaration();
	}

	@Override
	public ArrayElement getLastDefinition() {
		return (ArrayElement) super.getLastDefinition();
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}
		return getArtifact().materialize().type().derivedFrom(
				other.getArtifact().materialize().type());
	}

	@Override
	public String toString() {
		if (this.indexRef == null) {
			return super.toString();
		}
		return getEnclosingScope() + "[" + this.indexRef + "]";
	}

}
