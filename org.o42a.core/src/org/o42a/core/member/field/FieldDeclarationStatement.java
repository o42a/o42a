/*
    Compiler Core
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
package org.o42a.core.member.field;

import static org.o42a.core.st.DefinitionTarget.fieldDeclaration;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalFieldOp;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;


final class FieldDeclarationStatement extends DeclarationStatement {

	private final FieldBuilder builder;
	private final DeclaredMemberField member;
	private StatementEnv env;

	FieldDeclarationStatement(
			FieldBuilder builder,
			DeclaredMemberField member) {
		super(builder, builder.distribute());
		this.builder = builder;
		this.member = member;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return fieldDeclaration(this);
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public final DeclaredMemberField toMember() {
		return this.member;
	}

	public final StatementEnv getEnv() {
		return this.env;
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		assert this.env == null :
			"Environment already assigned to " + this;
		this.env = env;
		return env.notCondition(this);
	}

	@Override
	public Definitions define(Scope scope) {
		return null;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return initialLogicalValue(scope);
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {

		final Field<?> field =
			scope.member(this.member.getKey())
			.toField(scope);
		final LogicalValue logicalValue =
			field.getArtifact()
			.materialize()
			.getDefinitions()
			.value(scope)
			.getLogicalValue();

		return new ExecuteCommand(this, logicalValue);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final FieldDeclaration declaration =
			this.builder.getDeclaration().reproduce(reproducer);

		if (declaration == null) {
			return null;
		}

		final FieldDefinition definition =
			this.builder.getDefinition().reproduce(reproducer);

		if (definition == null) {
			return null;
		}

		final FieldBuilder builder =
			reproducer.getStatements().field(declaration, definition);

		if (builder == null) {
			return null;
		}

		return builder.build();
	}

	@Override
	public String toString() {
		return "FieldVariantSt[" + this.member + "]:"
		+ this.builder.getDefinition();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new LocalFieldOp(builder, this, this.member.toField(getScope()));
	}

}
