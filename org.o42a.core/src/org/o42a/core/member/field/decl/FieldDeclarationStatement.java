/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.member.field.decl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.LocalFieldCmd;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;
import org.o42a.core.value.link.TargetResolver;


public final class FieldDeclarationStatement extends DeclarationStatement {

	private final FieldBuilder builder;
	private final DeclaredMemberField member;
	private Definer definer;

	public FieldDeclarationStatement(
			FieldBuilder builder,
			DeclaredMemberField member) {
		super(builder, builder.distribute());
		this.builder = builder;
		this.member = member;
	}

	@Override
	public final DeclaredMemberField toMember() {
		return this.member;
	}

	public final DefinerEnv getInitialEnv() {
		return this.definer.env();
	}

	@Override
	public DeclarationDefiner define(DefinerEnv env) {
		return this.definer = new Definer(this, env);
	}

	@Override
	public DeclarationCommand command(CommandEnv env) {
		return new Command(this, env);
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

	private FieldDefinition reproduceDefinition(Reproducer reproducer) {
		if (!this.builder.getDefinition().isValid()) {
			return invalidDefinition(
					this.builder.getDefinition(),
					reproducer.distribute());
		}

		final DeclaredField field = this.member.toDeclaredField();

		return new ReproducedObjectDefinition(field, reproducer);
	}

	private static final class Definer extends DeclarationDefiner {

		Definer(FieldDeclarationStatement statement, DefinerEnv env) {
			super(statement, env);
		}

		@Override
		public DefTargets getDefTargets() {
			return fieldDef();
		}

	}

	private static final class Command extends DeclarationCommand {

		Command(DeclarationStatement statement, CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getCommandTargets() {
			return actionCommand();
		}

		@Override
		public Action initialValue(LocalResolver resolver) {

			final Member member =
					resolver.getLocal().member(
							getDeclarationStatement()
							.toMember()
							.getMemberKey());
			final Obj object = member.toField().object(dummyUser());
			final Condition condition =
					object.value()
					.getValue()
					.getKnowledge()
					.getCondition();

			return new ExecuteCommand(this, condition);
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public Cmd cmd() {
			assert getStatement().assertFullyResolved();

			final FieldDeclarationStatement statement =
					(FieldDeclarationStatement) getStatement();

			return new LocalFieldCmd(
					statement.member.toField().field(dummyUser()));
		}

	}

}
