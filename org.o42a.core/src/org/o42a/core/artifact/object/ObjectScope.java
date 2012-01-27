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
package org.o42a.core.artifact.object;

import static org.o42a.core.ref.impl.prediction.ObjectPrediction.predictObject;

import org.o42a.codegen.Generator;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.StandaloneArtifactScope;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;


public abstract class ObjectScope extends StandaloneArtifactScope<Obj> {

	private Path enclosingScopePath;

	protected ObjectScope(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	@Override
	public final Obj getContainer() {
		return getArtifact();
	}

	@Override
	public Path getEnclosingScopePath() {
		if (this.enclosingScopePath != null) {
			return this.enclosingScopePath;
		}
		if (getEnclosingScope().isTopScope()) {
			return null;
		}
		return this.enclosingScopePath = getArtifact().scopePath();
	}

	@Override
	public ObjectScope getFirstDeclaration() {

		final Obj propagatedFrom = getArtifact().getPropagatedFrom();

		if (propagatedFrom == null) {
			return this;
		}

		return (ObjectScope) propagatedFrom.getScope().getFirstDeclaration();
	}

	@Override
	public ObjectScope getLastDefinition() {
		return getFirstDeclaration();
	}

	@Override
	public final Prediction predict(Prediction enclosing) {
		return predictObject(enclosing, toObject());
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj otherObject = other.toObject();

		if (otherObject == null) {
			return false;
		}

		return toObject().type().derivedFrom(otherObject.type());
	}

	@Override
	protected abstract ScopeIR createIR(Generator generator);

}
