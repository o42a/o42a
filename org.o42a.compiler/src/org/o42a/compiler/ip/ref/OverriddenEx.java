/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.*;
import org.o42a.core.artifact.object.ValuePartId;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public class OverriddenEx extends ObjectConstructor {

	private final Ref host;

	public OverriddenEx(
			LocationInfo location,
			Distributor distributor,
			Ref host) {
		super(location, distributor);
		this.host = host;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {

		final Scope scope = getScope();
		final Obj object = scope.toObject();

		if (object == null) {
			return ValueType.VOID.typeRef(this, scope);
		}

		return object.value().getValueType().typeRef(this, scope);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref host = this.host.reproduce(reproducer);

		if (host == null) {
			return null;
		}

		return new OverriddenEx(this, reproducer.distribute(), host);
	}

	@Override
	protected Obj createObject() {
		return new Overridden(this, distribute());
	}

	@Override
	protected Obj propagateObject(Scope scope) {
		return new Overridden(this, scope.distribute());
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Overridden extends Obj {

		Overridden(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected Definitions explicitDefinitions() {

			final Path selfPath = getScope().getEnclosingScopePath();
			final Obj self = selfPath.resolveArtifact(
					this,
					value().partUser(ValuePartId.PROPOSITION),
					getScope()).toObject();
			final Definitions overriddenDefinitions =
				self.value().getOverriddenDefinitions();

			return selfPath.rescoper(getScope()).update(overriddenDefinitions);
		}

		@Override
		protected Ascendants buildAscendants() {

			final Scope enclosingScope = getScope().getEnclosingScope();
			final ValueType<?> valueType =
				enclosingScope.toObject().value().getValueType();

			return new Ascendants(this).setAncestor(
					valueType.typeRef(this, enclosingScope));
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
		}

	}

	private static final class Op extends ConstructorOp {

		Op(HostOp host, OverriddenEx ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {

			final ValueType<?> valueType =
				getRef().getScope().toObject().value().getValueType();
			final ValDirs valDirs = dirs.value(valueType);

			writeValue(valDirs);
			valDirs.done();
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {

			final OverriddenEx ref = (OverriddenEx) getRef();
			final RefOp hostRef = ref.host.op(host());
			final ObjectOp object =
				hostRef.target(dirs.dirs()).toObject(dirs.dirs());
			final Code code = dirs.code();

			return object.objectType(code).writeOverriddenValue(dirs);
		}

	}

}
