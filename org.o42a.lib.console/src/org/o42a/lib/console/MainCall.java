/*
    Console Module
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
package org.o42a.lib.console;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.DeclarativeBlock;


final class MainCall extends ObjectConstructor {

	private final TypeRef adapterRef;

	MainCall(
			LocationInfo location,
			Distributor enclosing,
			TypeRef adapterRef) {
		super(location, enclosing);
		this.adapterRef = adapterRef;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.adapterRef;
	}

	@Override
	public ObjectConstructor reproduce(PathReproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Obj createObject() {
		return new MainObject(this, distribute(), this.adapterRef);
	}

	@Override
	public String toString() {
		if (this.adapterRef == null) {
			return super.toString();
		}
		return this.adapterRef + "()";
	}

	private static final class MainObject extends DefinedObject {

		private final TypeRef adapterRef;

		MainObject(
				LocationInfo location,
				Distributor enclosing,
				TypeRef adapterRef) {
			super(location, enclosing);
			this.adapterRef = adapterRef;
		}

		@Override
		public String toString() {
			if (this.adapterRef == null) {
				return super.toString();
			}
			return this.adapterRef + "()";
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).setAncestor(this.adapterRef);
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
		}

		@Override
		protected Obj findObjectIn(Scope enclosing) {
			throw new IllegalArgumentException(
					"Not an enclosing scope: " + enclosing);
		}

	}

}
