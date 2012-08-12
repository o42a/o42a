/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.macro;

import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.macro.MacroConsumer;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public final class StatementConsumer implements Consumer {

	private final Statements<?, ?> statements;
	private final boolean condition;
	private MemberField tempField;

	public StatementConsumer(Statements<?, ?> statements, boolean condition) {
		this.statements = statements;
		this.condition = condition;
	}

	public final MemberField getTempField() {
		return this.tempField;
	}

	@Override
	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {

		final Statements<?, ?> statements = createTopLevelSentence(macroRef);

		this.tempField = createTempField(macroRef, expansion, statements);
		if (this.tempField == null) {
			return null;
		}

		return new StatementMacroConsumer(macroRef, template, this.tempField);
	}

	private Statements<?, ?> createTopLevelSentence(LocationInfo location) {

		Statements<?, ?> st = this.statements;

		for (;;) {

			final Block<?, ?> block = st.getSentence().getBlock();
			final Statements<?, ?> enclosing = block.getEnclosing();

			if (enclosing == null) {
				return block.propose(location).alternative(location);
			}

			st = enclosing;
		}
	}

	private MemberField createTempField(
			Ref macroRef,
			Ref expansion,
			Statements<?, ?> statements) {

		final FieldDeclaration declaration = fieldDeclaration(
				macroRef,
				statements.nextDistributor(),
				statements.getMemberRegistry().tempMemberId())
				.setVisibility(Visibility.PROTECTED);
		final FieldBuilder field = statements.field(
				declaration,
				new TempFieldDefinition(expansion, this.condition));

		if (field == null) {
			return null;
		}

		final DeclarationStatement statement = field.build();

		if (statement == null) {
			return null;
		}

		statements.statement(statement);

		return statement.toMember().toField();
	}

	private static final class StatementMacroConsumer implements MacroConsumer {

		private final Ref macroRef;
		private final PathTemplate template;
		private final MemberField tempField;

		StatementMacroConsumer(
				Ref macroRef,
				PathTemplate template,
				MemberField tempField) {
			this.macroRef = macroRef;
			this.tempField = tempField;
			this.template = template;
		}

		@Override
		public Ref expandMacro(Ref macroExpansion) {

			final TempMetaDep dep =
					new TempMacroDep(this.tempField)
					.buildDep(this.macroRef, this.template);

			if (dep != null) {
				dep.register();
			}

			return macroExpansion;
		}

	}

}
