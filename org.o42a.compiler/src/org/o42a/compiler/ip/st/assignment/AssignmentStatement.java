/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.statement.AssignmentNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.compiler.ip.ref.KeepValueFragment;
import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.ref.Resolution;
import org.o42a.core.source.Location;
import org.o42a.core.st.*;


public class AssignmentStatement extends Statement {

	private final AssignmentNode node;
	private final AccessRules accessRules;
	private final Ref destination;
	private final RefBuilder value;
	private final boolean binding;
	private AssignmentKind assignmentKind;

	public AssignmentStatement(
			AssignmentNode node,
			AccessDistributor distributor,
			Ref destination,
			RefBuilder value,
			boolean binding) {
		super(
				location(destination, node.getOperator()),
				distributor);
		this.node = node;
		this.accessRules = distributor.getAccessRules();
		this.destination = destination;
		this.value = binding ? value : new StatefulRef(value);
		this.binding = binding;
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
		this.binding = prototype.binding;
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

	public final boolean isBinding() {
		return this.binding;
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
		return this.destination + (this.binding ? "<-" : "=") + this.value;
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

	private static final class StatefulRef implements RefBuilder {

		private final RefBuilder ref;

		StatefulRef(RefBuilder ref) {
			this.ref = ref;
		}

		@Override
		public Location getLocation() {
			return this.ref.getLocation();
		}

		@Override
		public Ref buildRef(Distributor distributor) {

			final Ref ref = this.ref.buildRef(distributor);
			final KeepValueFragment keepValue = new KeepValueFragment(ref);

			return SELF_PATH.bind(getLocation(), distributor.getScope())
					.append(keepValue)
					.target(distributor);
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return "\\\\" + this.ref;
		}

	}

}
