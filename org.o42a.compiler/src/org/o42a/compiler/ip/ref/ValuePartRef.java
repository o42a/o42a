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

import static org.o42a.compiler.ip.Interpreter.location;

import java.util.HashMap;

import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public final class ValuePartRef extends ObjectConstructor {

	static final HashMap<String, RefValuePart> partsById =
		new HashMap<String, RefValuePart>();

	public static Ref valuePartRef(
			IntrinsicRefNode ref,
			Distributor distributor) {

		final Location location = location(distributor, ref);
		final RefValuePart part = partsById.get(ref.getName().getName());

		if (part == null) {
			distributor.getContext().getLogger().error(
					"unknown_intrinsic",
					ref,
					"Unknown intrinsic: $%s$",
					ref.getName());
			return falseRef(location, distributor);
		}

		return new ValuePartRef(location, distributor, part, false);
	}

	private final RefValuePart valuePart;
	private final boolean overridden;

	private ValuePartRef(
			LocationInfo location,
			Distributor distributor,
			RefValuePart valuePart,
			boolean overridden) {
		super(location, distributor);
		this.valuePart = valuePart;
		this.overridden = overridden;
	}

	public final boolean isOverridden() {
		return this.overridden;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {

		final Scope scope = getScope();
		final Obj object = scope.toObject();

		if (object == null) {
			return ValueType.VOID.typeRef(location, scope);
		}

		return this.valuePart.valueType(object).typeRef(location, scope);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new ValuePartRef(
				this,
				reproducer.distribute(),
				this.valuePart,
				this.overridden);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.valuePart.partName());
		if (this.overridden) {
			out.append("^[");
		} else {
			out.append('[');
		}
		out.append(getScope().getEnclosingContainer()).append(']');

		return out.toString();
	}

	@Override
	protected Obj createObject() {
		return new ValuePartObj(this, distribute());
	}

	@Override
	protected Obj propagateObject(Scope scope) {
		return new ValuePartObj(this, scope.distribute());
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new ValuePartOp(host, this);
	}

	final RefValuePart getValuePart() {
		return this.valuePart;
	}

	private final class ValuePartObj extends Obj {

		ValuePartObj(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected Definitions explicitDefinitions() {

			final Path selfPath = getScope().getEnclosingScopePath();
			final Obj self = selfPath.resolveArtifact(
					this,
					value().proposition(),
					getScope()).toObject();
			final Definitions definitions;

			if (!ValuePartRef.this.overridden) {
				definitions = self.value().getDefinitions();
			} else {
				definitions = self.value().getOverriddenDefinitions();
			}

			return ValuePartRef.this.valuePart.valuePart(
					ValuePartRef.this,
					selfPath.rescoper(getScope()).update(definitions));
		}

		@Override
		protected Ascendants buildAscendants() {

			final Definitions definitions = value().getExplicitDefinitions();
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

}
