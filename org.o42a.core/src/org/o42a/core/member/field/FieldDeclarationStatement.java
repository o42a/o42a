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

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;
import static org.o42a.core.st.DefinitionTarget.fieldDeclaration;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalFieldOp;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.Member;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;


final class FieldDeclarationStatement extends DeclarationStatement {

	private final FieldBuilder builder;
	private final DeclaredMemberField member;
	private StatementEnv initialEnv;

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

	public final StatementEnv getInitialEnv() {
		return this.initialEnv;
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		assert this.initialEnv == null :
			"Environment already assigned to " + this;
		this.initialEnv = env;
		return env.notCondition(this);
	}

	@Override
	public Action initialValue(LocalResolver resolver) {

		final Member member = resolver.getLocal().member(this.member.getKey());
		final Field<?> field = member.toField(resolver);
		final LogicalValue logicalValue =
				field.getArtifact()
				.materialize()
				.value()
				.getDefinitions()
				.value(resolver)
				.getCondition()
				.toLogicalValue();

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

		final FieldDefinition definition = reproduceDefinition(reproducer);

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
		return ("FieldDeclarationStatement["
				+ this.member + "]:"
				+ this.builder.getDefinition());
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new LocalFieldOp(
				builder,
				this,
				this.member.toField(dummyUser()));
	}

	private FieldDefinition reproduceDefinition(Reproducer reproducer) {
		if (!this.builder.getDefinition().isValid()) {
			return invalidDefinition(
					this.builder.getDefinition(),
					reproducer.distribute());
		}

		final FieldVariant<?> variant =
				this.member.toDeclaredField().getVariants().get(0);

		return variant.reproduceDefinition(reproducer);
	}

}
