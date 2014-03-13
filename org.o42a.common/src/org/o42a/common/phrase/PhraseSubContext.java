/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.common.phrase;

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.common.phrase.part.NextClause;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


class PhraseSubContext extends PhraseContext {

	private final MainPhraseContext mainContext;
	private final Clause clause;

	PhraseSubContext(
			PhraseContext enclosing,
			LocationInfo location,
			Clause clause) {
		super(enclosing, location);
		this.mainContext = enclosing.getMainContext();
		this.clause = clause;
	}

	@Override
	public final Clause getClause() {
		return this.clause;
	}

	@Override
	public final NextClause clauseByName(LocationInfo location, Name name) {
		return findClause(
				getClause().getClauseContainer(),
				location,
				clauseName(name),
				name);
	}

	@Override
	public final NextClause clauseById(
			LocationInfo location,
			ClauseId clauseId) {
		return findClause(
				getClause().getClauseContainer(),
				location,
				clauseId.getMemberId(),
				clauseId);
	}

	@Override
	public Path pathToObject(Scope scope) {
		if (getClause().getScope().is(getClause().toMember().getScope())) {
			// Clause is in the same scope as enclosing one.
			return getEnclosing().pathToObject(scope);
		}

		// Clause declares it's own scope.
		return scope.getEnclosingScopePath().append(
				getEnclosing().pathToObject(scope.getEnclosingScope()));
	}

	@Override
	protected void define(ClauseInstance instance, Block definition) {

		final DefinitionReproducer reproducer =
				new DefinitionReproducer(this.clause, instance, definition);

		this.clause.define(reproducer);
	}

	@Override
	final MainPhraseContext getMainContext() {
		return this.mainContext;
	}

	void applyCause(Statements statements) {

		final Clause clause = getClause();

		switch (clause.getKind()) {
		case OVERRIDER:
			overrideField(statements);
			return;
		case EXPRESSION:
			instantiateObjects(statements);
			return;
		case GROUP:
			groupDefinition(statements);
			return;
		}

		throw new IllegalStateException(
				"Unsupported kind of clause: " + clause.getKind()
				+ " (" + clause + ")");
	}

	private void overrideField(Statements statements) {
		assert getInstances().length == 1 :
			"Exactly one instance of aliased field allowed, but "
			+ getInstances().length + " found";

		final ClauseInstance instance = getInstances()[0];
		final Distributor distributor = statements.nextDistributor();
		final LocationInfo location = instance.getLocation();
		final FieldDefinition definition =
				fieldDefinition(location, distributor);

		if (definition == null) {
			return;
		}

		final FieldDeclaration declaration =
				createDeclaration(location, distributor);
		final FieldBuilder builder =
				statements.field(declaration, definition);

		if (builder == null) {
			return;
		}

		statements.statement(builder.build());
	}

	private FieldDefinition fieldDefinition(
			LocationInfo location,
			Distributor distributor) {

		final ClauseSubstitution substitution = getClause().getSubstitution();

		switch (substitution) {
		case VALUE_SUBSTITUTION:

			final Ref valueSubstitution = substituteValue(distributor);

			if (valueSubstitution == null) {
				return null;
			}

			return valueSubstitution.toFieldDefinition();
		case PREFIX_SUBSITUTION:

			final Ref prefix = getPhrase().substitutePrefix(distributor);

			if (prefix == null) {
				return null;
			}

			return prefix.toFieldDefinition();
		case NO_SUBSTITUTION:
			return ascendants(location, distributor).fieldDefinition(
					location,
					getInstances()[0].getDefinition());
		}

		throw new IllegalStateException(
				"Unsupported clause substitution: " + substitution);
	}

	private Ref substituteValue(Distributor distributor) {

		final ClauseInstance instance = getInstances()[0];
		final Ref substitution = instance.substituteValue(distributor);

		if (substitution == null) {
			getLogger().error(
					"missing_clause_value",
					instance.getLocation(),
					"Value required here");
		}

		return substitution;
	}

	private void instantiateObjects(Statements statements) {

		final PlainClause clause = getClause().toPlainClause();
		final boolean assignment = clause.isAssignment();

		for (ClauseInstance instance : getInstances()) {

			final Ref ref =
					instance.instantiateObject(statements.nextDistributor());

			if (ref == null) {
				continue;
			}
			if (assignment) {
				statements.selfAssign(ref);
			} else {
				statements.expression(ref);
			}
		}
	}

	private void groupDefinition(Statements statements) {
		for (ClauseInstance instance : getInstances()) {

			final Block block = statements.parentheses(instance.getLocation());

			instance.groupDefinition(block);
		}
	}

	private FieldDeclaration createDeclaration(
			LocationInfo location,
			Distributor distributor) {

		final MemberKey overriddenKey =
				getClause().toPlainClause().getOverridden();
		FieldDeclaration declaration;

		if (overriddenKey.isAdapter()) {
			declaration = fieldDeclaration(
					location,
					distributor,
					adapterId(
							overriddenKey.getAdapterId()
							.adapterType(distributor.getScope())));
		} else {
			declaration = fieldDeclaration(
					location,
					distributor,
					overriddenKey.getMemberId());
		}

		final Obj origin = overriddenKey.getOrigin().toObject();

		declaration =
				declaration.override()
				.setDeclaredIn(
						origin.selfRef().toStaticTypeRef())
				.setPrototypeMode(
						getClause().toPlainClause().getPrototypeMode());

		return declaration;
	}

	private static final class DefinitionReproducer extends Reproducer {

		private final ClauseInstance instance;
		private MemberRegistry memberRegistry;
		private Statements statements;

		DefinitionReproducer(
				Clause clause,
				ClauseInstance instance,
				Block block) {
			super(clause.getScope(), block.distribute());
			this.instance = instance;
			this.memberRegistry = block.getMemberRegistry();

			final LocationInfo location = instance.getLocation();

			this.statements = block.declare(location).alternative(location);
		}

		@Override
		public boolean phraseCreatesObject() {
			return this.instance.getContext().getMainContext().createsObject();
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.instance.getContext().getPhrase().getPrefix()
					.getAncestor().getRef();
		}

		@Override
		public MemberRegistry getMemberRegistry() {
			return this.memberRegistry;
		}

		@Override
		public Statements getStatements() {
			return this.statements;
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements statements,
				Clause clause) {

			PhraseSubContext subContext = this.instance.subContext(clause);

			if (subContext == null) {
				if (!clause.isMandatory()) {
					return;
				}
				subContext = this.instance.addSubContext(location, clause);
			}

			subContext.applyCause(statements);
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope().is(reproducingScope)) {
				return this;
			}
			return null;
		}

	}

}
