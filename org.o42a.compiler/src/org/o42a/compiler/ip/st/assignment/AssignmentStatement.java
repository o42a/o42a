/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.assignment;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.st.assignment.CustomAssignment.customAssignment;
import static org.o42a.compiler.ip.st.assignment.VariableAssignment.variableAssignment;

import org.o42a.ast.statement.AssignmentNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Local;


public class AssignmentStatement extends Statement {

	private final AssignmentNode node;
	private final AccessRules accessRules;
	private final Ref destination;
	private final RefBuilder value;
	private final Local local;
	private final Ref localRef;
	private AssignmentKind assignmentKind;

	public AssignmentStatement(
			AssignmentNode node,
			AccessDistributor distributor,
			Ref destination,
			RefBuilder value) {
		super(
				location(destination, node.getOperator()),
				distributor);
		this.node = node;
		this.accessRules = distributor.getAccessRules();
		this.destination = destination;
		this.value = value;
		this.local = null;
		this.localRef = null;
	}

	public AssignmentStatement(
			AssignmentNode node,
			AccessDistributor distributor,
			Ref destination,
			Local local) {
		super(
				location(destination, node.getOperator()),
				distributor);
		this.node = node;
		this.accessRules = distributor.getAccessRules();
		this.destination = destination;
		this.value = this.localRef = local.toRef();
		this.local = local;
	}

	AssignmentStatement(
			AssignmentStatement prototype,
			Reproducer reproducer,
			AssignmentKind assignmentKind,
			Ref destination,
			RefBuilder value) {
		super(prototype, reproducer.distribute());
		this.node = prototype.getNode();
		this.accessRules = prototype.getAccessRules();
		this.destination = destination;
		this.value = value;
		this.local = prototype.local;
		if (prototype.localRef == null) {
			this.localRef = null;
		} else {
			this.localRef = prototype.localRef.reproduce(reproducer);
		}
		this.assignmentKind = assignmentKind;
		assignmentKind.init(this);
	}

	public final AssignmentNode getNode() {
		return this.node;
	}

	public final AccessRules getAccessRules() {
		return this.accessRules;
	}

	public final Ref getDestination() {
		return this.destination;
	}

	public final RefBuilder getValue() {
		return this.value;
	}

	public final Local getLocal() {
		return this.local;
	}

	public final Ref getLocalRef() {
		return this.localRef;
	}

	public final boolean isBinding() {
		return getLocal() == null;
	}

	@Override
	public boolean isValid() {
		return !getAssignmentKind().isError();
	}

	@Override
	public Command command(CommandEnv env) {
		return new AssignmentCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return getAssignmentKind().reproduce(reproducer, this);
	}

	public final AccessDistributor distributeAccess() {
		return getAccessRules().distribute(distribute());
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		if (this.assignmentKind != null) {
			return this.assignmentKind.toString();
		}
		if (isBinding()) {
			return this.destination + "<-" + this.value;
		}
		return this.destination + "=" + this.local.originalRef();
	}

	public AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}

		final Resolution destResolution = this.destination.getResolution();

		if (destResolution.isError()) {
			return this.assignmentKind = new AssignmentError(this);
		}

		final Obj object = destResolution.toObject();

		if (object == null) {
			return assignmentError();
		}

		final AssignmentKind custom = customAssignment(this, object);

		if (custom != null) {
			return this.assignmentKind = custom;
		}

		return this.assignmentKind = variableAssignment(this, object);
	}

	AssignmentKind assignmentError() {
		getLogger().error(
				"not_variable_assigned",
				getDestination(),
				"Can only assign to links or variables");
		return this.assignmentKind = new AssignmentError(this);
	}

}
