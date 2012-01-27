/*
    Compiler Core
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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.impl.imperative.AssignmentKind.ASSIGNMENT_ERROR;
import static org.o42a.core.st.impl.imperative.AssignmentKind.VARIABLE_ASSIGNMENT;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueStruct;


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

		return new AssignmentStatement(
				this,
				reproducer.distribute(),
				destination,
				value);
	}

	@Override
	public InlineValue inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
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
		if (getAssignmentKind().isError()) {
			return;
		}

		final Resolution value =
				this.value.resolve(resolver).resolveValue();
		final Resolution destination =
				this.destination.resolve(resolver).resolveAssignee();

		if (!destination.isError() && !value.isError()) {
			destination.materialize().value().wrapBy(
					value.materialize().value());
			if (resolver.getScope() == getScope()) {
				destination.toLink().assign(this.value);
			}
		}
	}

	@Override
	protected StOp createOp(CodeBuilder builder) {
		return getAssignmentKind().op(builder, this);
	}

	private AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}

		final Resolution destResolution = this.destination.getResolution();
		final Resolution valueResolution = this.value.getResolution();
		final Link link = destResolution.toLink();

		if (link == null || !link.isVariable()) {
			getLogger().error(
					"not_variable_assigned",
					this.destination,
					"Can only assign to variable");
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		final TypeRef variableTypeRef =
				this.destination.ancestor(this.destination);

		if (!this.value.toTypeRef().checkDerivedFrom(variableTypeRef)) {
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		return this.assignmentKind = variableAssignment(link, valueResolution);
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

	private static final class AssignmentDefiner extends Definer {

		AssignmentDefiner(AssignmentStatement assignment, StatementEnv env) {
			super(assignment, env);
		}

		@Override
		public StatementEnv nextEnv() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return conditionDefinition(getStatement());
		}

		@Override
		public ValueStruct<?, ?> valueStruct(Scope scope) {
			return null;
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

	}

}
