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
import static org.o42a.util.use.User.dummyUser;

import java.util.HashMap;

import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.common.Expression;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public final class ValuePartRef extends Expression {

	static final HashMap<String, ValuePart> partsById =
		new HashMap<String, ValuePart>();

	public static Ref overridden(
			LocationInfo location,
			Distributor distributor) {
		return new ValuePartRef(location, distributor, ValuePart.ALL, true);
	}

	public static Ref valuePartRef(
			IntrinsicRefNode ref,
			Distributor distributor) {

		final Location location = location(distributor, ref);
		final ValuePart part = partsById.get(ref.getName().getName());

		if (part == null) {
			distributor.getContext().getLogger().unresolved(
					ref,
					ref.printContent());
			return falseRef(location, distributor);
		}

		return new ValuePartRef(location, distributor, part, false);
	}

	private final ValuePart valuePart;
	private final boolean overridden;
	private Resolution resolution;

	private ValuePartRef(
			LocationInfo location,
			Distributor distributor,
			ValuePart valuePart,
			boolean overridden) {
		super(location, distributor);
		this.valuePart = valuePart;
		this.overridden = overridden;
	}

	public final boolean isOverridden() {
		return this.overridden;
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

		out.append(this.valuePart.partName);
		if (this.overridden) {
			out.append("^[");
		} else {
			out.append('[');
		}
		out.append(getScope().getEnclosingContainer()).append(']');

		return out.toString();
	}

	@Override
	protected Resolution resolveExpression(Resolver resolver) {
		if (this.resolution != null) {
			return this.resolution;
		}
		return this.resolution = objectResolution(
				new ValuePartObj(this, distributeIn(resolver.getContainer())));
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
		value(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new ValuePartOp(host, this);
	}

	final ValuePart getValuePart() {
		return this.valuePart;
	}

	private final class ValuePartObj extends PlainObject {

		ValuePartObj(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected Definitions explicitDefinitions() {

			final Path selfPath = getScope().getEnclosingScopePath();
			final Obj self = selfPath.resolveArtifact(
					this,
					value(dummyUser()),
					getScope()).toObject();
			final Definitions definitions;

			if (!ValuePartRef.this.overridden) {
				definitions = self.getDefinitions();
			} else {
				definitions = self.getOverriddenDefinitions();
			}

			return ValuePartRef.this.valuePart.valuePart(
					ValuePartRef.this,
					selfPath.rescoper(getScope()).update(definitions));
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

}
