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

import static org.o42a.core.def.Definitions.emptyDefinitions;

import java.util.IdentityHashMap;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ConstructorOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


@Deprecated
public abstract class ObjectConstructor extends Ref {

	private Obj constructed;
	private IdentityHashMap<Scope, Obj> propagated;

	public ObjectConstructor(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public boolean isConstant() {

		final Resolution resolution = getResolution();

		if (!resolution.isConstant()) {
			return false;
		}

		final TypeRef ancestor = resolution.materialize().type().getAncestor();

		return ancestor == null || ancestor.isStatic();
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public abstract TypeRef ancestor(LocationInfo location);

	@Override
	public final Resolution resolve(Resolver resolver) {

		final Scope scope = resolver.getScope();
		final Obj object;

		if (scope == getScope()) {
			object = construct();
		} else {
			object = propagate(scope);
		}

		return resolver.newObject(this, object);
	}

	protected abstract Obj createObject();

	protected Obj propagateObject(Scope scope) {
		return new Propagated(scope, this);
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return defaultFieldDefinition();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver).resolveValues(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new ConstructorOp(host, this);
	}

	private Obj construct() {
		if (this.constructed != null) {
			return this.constructed;
		}
		return this.constructed = createObject();
	}

	private Obj propagate(Scope scope) {
		if (this.propagated == null) {
			this.propagated = new IdentityHashMap<Scope, Obj>();
		} else {

			final Obj cached = this.propagated.get(scope);

			if (cached != null) {
				return cached;
			}
		}

		final Obj propagated = propagateObject(scope);

		this.propagated.put(scope, propagated);

		return propagated;
	}

	private static final class Propagated extends Obj {

		private final StaticTypeRef propagatedFrom;

		Propagated(Scope scope, Ref propagatedFrom) {
			super(
					propagatedFrom.distributeIn(scope.getContainer()),
					propagatedFrom.getResolution().toObject());
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
		protected Definitions explicitDefinitions() {
			return emptyDefinitions(this, getScope());
		}

	}

}
