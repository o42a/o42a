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
package org.o42a.core.ref.common;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ConstructorOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public abstract class ObjectConstructor extends Expression {

	public ObjectConstructor(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public abstract TypeRef ancestor(LocationInfo location);

	@Override
	public void resolveAll() {
		getResolution().toObject().resolveAll();
	}

	@Override
	protected final Resolution resolveExpression(Scope scope) {
		if (scope != getScope() && !isStatic()) {
			return objectResolution(new Propagated(scope, this));
		}

		final Obj object = createObject();

		if (object == null) {
			return noResolution();
		}

		return objectResolution(object);
	}

	protected abstract Obj createObject();

	@Override
	protected RefOp createOp(HostOp host) {
		return new ConstructorOp(host, this);
	}

	private static final class Propagated extends Obj {

		private final StaticTypeRef propagatedFrom;

		Propagated(Scope scope, Ref propagatedFrom) {
			super(
					propagatedFrom,
					propagatedFrom.distributeIn(scope.getContainer()));
			this.propagatedFrom =
				propagatedFrom.toStaticTypeRef().upgradeScope(scope);
		}

		@Override
		public String toString() {
			return ("Propagated[" + this.propagatedFrom
					+ " / " + getScope().getEnclosingScope() + "]");
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).addImplicitSample(this.propagatedFrom);
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
		}

		@Override
		protected Definitions overrideDefinitions(
				Scope scope,
				Definitions ascendantDefinitions) {
			return ascendantDefinitions;
		}

	}

}
