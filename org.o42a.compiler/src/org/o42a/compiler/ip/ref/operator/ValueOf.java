/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public class ValueOf extends ObjectConstructor {

	private final Ref operand;
	private TypeRef valueTypeInterface;

	public ValueOf(
			Interpreter ip,
			CompilerContext context,
			UnaryNode node,
			AccessDistributor distributor,
			boolean stateful) {
		super(new Location(context, node), distributor, stateful);
		this.operand = node.getOperand().accept(
				ip.expressionVisitor(),
				distributor);
	}

	private ValueOf(ValueOf prototype, boolean stateful) {
		super(prototype, prototype.distribute(), stateful);
		this.operand = prototype.operand;
		this.valueTypeInterface = prototype.valueTypeInterface;
	}

	private ValueOf(ValueOf prototype, Distributor distributor, Ref operand) {
		super(prototype, distributor, prototype.isStateful());
		this.operand = operand;
	}

	public final Ref operand() {
		return this.operand;
	}

	public final TypeRef getValueTypeInterface() {
		if (this.valueTypeInterface != null) {
			return this.valueTypeInterface;
		}
		return this.valueTypeInterface = operand().getValueTypeInterface();
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return getValueTypeInterface();
	}

	@Override
	public TypeRef iface(Ref ref) {
		return getValueTypeInterface();
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(
				ref.toStateful(isStateful()),
				rescopedTypeParameters(ref));
	}

	@Override
	public ValueOf reproduce(PathReproducer reproducer) {

		final Ref operand = operand().reproduce(reproducer.getReproducer());

		if (operand == null) {
			return null;
		}

		return new ValueOf(this, reproducer.distribute(), operand);
	}

	@Override
	public String toString() {
		if (this.operand != null) {
			return "/" + this.operand;
		}
		return super.toString();
	}

	@Override
	protected ValueOf createStateful() {
		return new ValueOf(this, true);
	}

	@Override
	protected Obj createObject() {
		return new ValueObject(this);
	}

	private TypeRefParameters rescopedTypeParameters(Ref ref) {

		final TypeRefParameters typeParameters =
				getValueTypeInterface().copyParameters();
		final BoundPath path = ref.getPath();

		if (path.rawLength() == 1) {
			return typeParameters;
		}

		final PrefixPath prefix = path.cut(1).toPrefix(ref.getScope());

		return typeParameters.prefixWith(prefix);
	}

}
