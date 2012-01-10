/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.normalizer;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


public final class MaterialMultiScope extends MultiScope {

	private final MultiScope artifacts;

	public MaterialMultiScope(MultiScope artifacts) {
		super(artifacts.getScope().getArtifact().materialize().getScope());
		this.artifacts = artifacts;
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return this.artifacts.getScopeSet();
	}

	@Override
	public Iterator<Scope> iterator() {
		return new MaterialIterator(this.artifacts);
	}

	@Override
	public String toString() {
		if (this.artifacts == null) {
			return super.toString();
		}
		return "MaterializedMultiScope[" + this.artifacts + ']';
	}

	private static final class MaterialIterator implements Iterator<Scope> {

		private final Iterator<Scope> artifacts;

		MaterialIterator(MultiScope artifacts) {
			this.artifacts = artifacts.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.artifacts.hasNext();
		}

		@Override
		public Scope next() {

			final Scope artifact = this.artifacts.next();

			return artifact.getArtifact().materialize().getScope();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
