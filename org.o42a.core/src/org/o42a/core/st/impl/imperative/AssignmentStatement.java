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

import static org.o42a.core.object.link.LinkValueType.VARIABLE;
import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.impl.imperative.AssignmentKind.*;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.ref.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueStruct;


public class AssignmentStatement extends Statement {

	private final Ref destination;
	private final Ref value;
	private AssignmentKind assignmentKind;

	public AssignmentStatement(
			LocationInfo location,
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
	public InlineCmd inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
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
		getAssignmentKind().resolve(resolver, this.destination, this.value);
	}

	@Override
	protected Cmd createCmd(CodeBuilder builder) {
		return getAssignmentKind().op(builder, this);
	}

	private AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}
		if (this.value.getResolution().isError()) {
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		final Resolution destResolution = this.destination.getResolution();

		if (destResolution.isError()) {
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		final Obj object = destResolution.toObject();

		if (object != null) {

			final ObjectLink dereferencedLink = object.getDereferencedLink();

			if (dereferencedLink != null && dereferencedLink.isSynthetic()) {
				return this.assignmentKind = derefAssignment(dereferencedLink);
			}

			return this.assignmentKind = valueAssignment(object);
		}

		final Link link = destResolution.toLink();

		if (link == null || !link.isVariable()) {
			getLogger().error(
					"not_variable_assigned",
					this.destination,
					"Can only assign to variable");
			return this.assignmentKind = ASSIGNMENT_ERROR;
		}

		return this.assignmentKind = variableAssignment();
	}

	private AssignmentKind derefAssignment(ObjectLink destination) {

		final LinkValueStruct destStruct =
				destination.getValueStruct().toLinkStruct();

		if (destStruct == null || destStruct.getValueType() != VARIABLE) {
			getLogger().error(
					"not_variable_assigned",
					this.destination,
					"Can only assign to variable");
			return ASSIGNMENT_ERROR;
		}

		final TypeRef destTypeRef =
				destStruct.getTypeRef().prefixWith(
						this.destination.getPath().toPrefix(getScope()));

		if (!this.value.toTypeRef().checkDerivedFrom(destTypeRef)) {
			return ASSIGNMENT_ERROR;
		}

		return DEREF_ASSIGNMENT;
	}

	private AssignmentKind valueAssignment(Obj destination) {

		final LinkValueStruct destStruct =
				destination.value().getValueStruct().toLinkStruct();

		if (destStruct == null || destStruct.getValueType() != VARIABLE) {
			getLogger().error(
					"not_variable_assigned",
					this.destination,
					"Can only assign to variable");
			return ASSIGNMENT_ERROR;
		}

		final TypeRef destTypeRef =
				destStruct.getTypeRef().prefixWith(
						this.destination.getPath().toPrefix(getScope()));

		if (!this.value.toTypeRef().checkDerivedFrom(destTypeRef)) {
			return ASSIGNMENT_ERROR;
		}

		return VALUE_ASSIGNMENT;
	}

	private AssignmentKind variableAssignment() {

		final TypeRef variableTypeRef =
				this.destination.ancestor(this.destination);

		if (!this.value.toTypeRef().checkDerivedFrom(variableTypeRef)) {
			return ASSIGNMENT_ERROR;
		}

		return VARIABLE_ASSIGNMENT;
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
