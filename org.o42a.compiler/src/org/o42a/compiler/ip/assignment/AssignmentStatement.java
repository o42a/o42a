/*
    Compiler
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
package org.o42a.compiler.ip.assignment;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.assignment.CustomAssignment.customAssignment;
import static org.o42a.compiler.ip.assignment.DerefAssignment.derefAssignment;
import static org.o42a.compiler.ip.assignment.VariableAssignment.variableAssignment;

import org.o42a.ast.statement.AssignmentNode;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueStruct;


public class AssignmentStatement extends Statement {

	private final AssignmentNode node;
	private final Ref destination;
	private final Ref value;
	private AssignmentKind assignmentKind;

	public AssignmentStatement(
			AssignmentNode node,
			Ref destination,
			Ref value) {
		super(
				location(destination, node.getOperator()),
				destination.distribute());
		this.node = node;
		this.destination = destination;
		this.value = value;
	}

	private AssignmentStatement(
			AssignmentStatement prototype,
			Reproducer reproducer,
			Ref destination,
			Ref value) {
		super(prototype, reproducer.distribute());
		this.node = prototype.node;
		this.destination = destination;
		this.value = value;
		this.assignmentKind =
				prototype.getAssignmentKind().reproduce(this, reproducer);
	}

	public final AssignmentNode getNode() {
		return this.node;
	}

	public final Ref getDestination() {
		return this.destination;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public Definer define(StatementEnv env) {
		return new AssignmentDefiner(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		if (getAssignmentKind().isError()) {
			return null;
		}

		final Ref destination = this.destination.reproduce(reproducer);
		final Ref value = this.value.reproduce(reproducer);

		if (destination == null || value == null) {
			return null;
		}

		return new AssignmentStatement(this, reproducer, destination, value);
	}

	@Override
	public InlineCmd inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return getAssignmentKind().inline(normalizer, valueStruct, origin);
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
		getAssignmentKind().normalize(normalizer);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.destination + " = " + this.value;
	}

	@Override
	protected void fullyResolveImperative(LocalResolver resolver) {
		getAssignmentKind().resolve(resolver);
	}

	@Override
	protected Cmd createCmd(CodeBuilder builder) {
		return getAssignmentKind().op(builder);
	}

	private AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}
		if (this.value.getResolution().isError()) {
			return this.assignmentKind = new AssignmentError(this);
		}

		final Resolution destResolution = this.destination.getResolution();

		if (destResolution.isError()) {
			return this.assignmentKind = new AssignmentError(this);
		}

		final Obj object = destResolution.toObject();

		if (object == null) {
			getLogger().error(
					"not_variable_assigned",
					this.destination,
					"Can only assign to variable");

			return this.assignmentKind = new AssignmentError(this);
		}

		final AssignmentKind deref = derefAssignment(this, object);

		if (deref != null) {
			return this.assignmentKind = deref;
		}

		final AssignmentKind custom = customAssignment(this, object);

		if (custom != null) {
			return this.assignmentKind = custom;
		}

		return this.assignmentKind = variableAssignment(this, object);
	}

}
