/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.impl.imperative.AssignmentKind.ASSIGNMENT_ERROR;
import static org.o42a.core.st.impl.imperative.AssignmentKind.VARIABLE_ASSIGNMENT;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;


public class AssignmentStatement extends Statement {

	private final Ref destination;
	private final Ref value;
	private AssignmentKind assignmentKind;

	public AssignmentStatement(
			LocationInfo location,
			Imperatives enclosing,
			Ref destination,
			Ref value) {
		super(location, destination.distribute());
		this.destination = destination;
		this.value = value;
	}

	private AssignmentStatement(
			AssignmentStatement prototype,
			Distributor distributor,
			Ref destination,
			Ref value) {
		super(prototype, distributor);
		this.destination = destination;
		this.value = value;
		this.assignmentKind = prototype.assignmentKind;
	}

	public final Ref getDestination() {
		return this.destination;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return conditionDefinition(this);
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Definitions define(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return new ExecuteCommand(this, LogicalValue.RUNTIME);
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
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

		return new AssignmentStatement(
				this,
				reproducer.distribute(),
				destination,
				value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.destination + " = " + this.value;
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		if (getAssignmentKind().isError()) {
			return;
		}

		final Resolution value = this.value.resolve(resolver);
		final Resolution destination = this.destination.resolve(resolver);

		this.destination.resolveAll(resolver);
		this.value.resolveAll(resolver);

		if (!destination.isError() && !value.isError()) {
			destination.materialize().type().wrapBy(
					value.materialize().type());
		}
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		if (getAssignmentKind().isError()) {
			return;
		}

		final Resolution value = this.value.resolve(resolver);
		final Resolution destination = this.destination.resolve(resolver);

		this.value.resolveValues(resolver);
		this.destination.resolveValues(resolver);

		if (!destination.isError() && !value.isError()) {
			destination.materialize().value().wrapBy(
					value.materialize().value());
		}
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return getAssignmentKind().op(builder, this);
	}

	private AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}

		final Resolution destResolution = this.destination.getResolution();
		final Resolution valueResolution = this.value.getResolution();

		if (destResolution.isError() || valueResolution.isError()) {
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		final Link link = destResolution.toLink();

		if (link != null) {
			if (!link.isVariable()) {
				return this.assignmentKind = invalidDestination();
			}
			return this.assignmentKind =
					variableAssignment(link, valueResolution);
		}

		final Array array = destResolution.toArray();

		if (array != null) {
			return this.assignmentKind =
					arrayAssinment(array, valueResolution);
		}

		return this.assignmentKind = invalidDestination();
	}

	private AssignmentKind invalidDestination() {
		getLogger().error(
				"invalid_destination_assigned",
				this.destination,
				"Can only assign to variable or array");
		return ASSIGNMENT_ERROR;
	}

	private AssignmentKind variableAssignment(
			Link destination,
			Resolution valueResolution) {

		final Obj objectValue = valueResolution.toObject();
		final TypeRef valueType;

		if (objectValue != null) {
			valueType = objectValue.type().getAncestor();
		} else {

			final Artifact<?> artifactValue = valueResolution.toArtifact();

			valueType = artifactValue.getTypeRef();

			if (valueType == null) {
				getLogger().error(
						"illegal_value_assigned",
						this.value,
						"Illegal value assigned");
				return ASSIGNMENT_ERROR;
			}
		}
		if (destination.getTypeRef().type(dummyUser()).derivedFrom(
				valueType.type(dummyUser()))) {
			return VARIABLE_ASSIGNMENT;
		}

		getLogger().incompatible(destination, valueType);

		return ASSIGNMENT_ERROR;
	}

	private AssignmentKind arrayAssinment(
			Array destination,
			Resolution valueResolution) {
		getLogger().error(
				"not_implemented_array_assignment",
				this,
				"Array assignment not implemented yet");
		return ASSIGNMENT_ERROR;
	}

}
