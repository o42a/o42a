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

import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public class OverriddenEx extends Ref {

	private final Ref host;
	private Resolution resolution;

	public OverriddenEx(
			LocationInfo location,
			Distributor distributor,
			Ref host) {
		super(location, distributor);
		this.host = host;
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
	public void resolveAll() {
		getResolution().resolveAll();
	}

	@Override
	public Resolution resolve(Scope scope) {
		if (this.resolution != null) {
			return this.resolution;
		}
		return this.resolution = objectResolution(new Overridden(
				this,
				distributeIn(scope.getContainer())));
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Overridden extends PlainObject {

		Overridden(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected Definitions explicitDefinitions() {

			final Path selfPath = getScope().getEnclosingScopePath();
			final Obj self =
				selfPath.resolveArtifact(this, this, getScope()).toObject();
			final Definitions overriddenDefinitions =
				self.getOverriddenDefinitions();

			return selfPath.rescoper(getScope()).update(overriddenDefinitions);
		}

		@Override
		protected Ascendants buildAscendants() {

			final Definitions definitions = getExplicitDefinitions();
			final ValueType<?> valueType = definitions.getValueType();

			if (valueType == null) {
				return createAscendants(definitions, ValueType.VOID);
			}

			return createAscendants(definitions, valueType);
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
		}

		private Ascendants createAscendants(
				final Definitions definitions,
				final ValueType<?> valueType) {
			return new Ascendants(this).setAncestor(
					valueType.typeRef(
							definitions,
							getScope().getEnclosingScope()));
		}

	}

	private static final class Op extends ConstructorOp {

		Op(HostOp host, OverriddenEx ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {

			final Code code = dirs.code();
			final ValOp result =
				code.allocate(null, VAL_TYPE).storeIndefinite(code);

			writeValue(dirs, result);
		}

		@Override
		public void writeValue(CodeDirs dirs, ValOp result) {

			final OverriddenEx ref = (OverriddenEx) getRef();
			final RefOp hostRef = ref.host.op(host());
			final ObjectOp object =
				hostRef.target(dirs).toObject(dirs);
			final Code code = dirs.code();

			object.objectType(code).writeOverriddenValue(
					dirs.code(),
					result);
			result.go(code, dirs);
		}

	}

}
