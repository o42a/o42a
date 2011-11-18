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
package org.o42a.core.member.impl.clause;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.*;
import org.o42a.util.log.Loggable;


public final class DeclaredGroupClause
		extends GroupClause
		implements ClauseContainer {

	public static DeclaredGroupClause declaredGroupClause(
			ClauseBuilder builder) {
		return new DeclaredGroupClauseMember(builder).clause();
	}

	private final ClauseBuilder builder;
	private Path outcome;
	private ReusedClause[] reused;
	private Block<?> definition;
	private ImperativeBlock imperative;
	private LocalScope localScope;

	DeclaredGroupClause(
			DeclaredGroupClauseMember member,
			ClauseBuilder builder) {
		super(member);
		this.builder = builder;
	}

	DeclaredGroupClause(
			MemberClause clause,
			DeclaredGroupClause propagatedFrom) {
		super(clause, propagatedFrom);
		this.builder = propagatedFrom.builder;
		this.definition = propagatedFrom.definition;
		this.imperative = propagatedFrom.imperative;
		this.localScope = propagatedFrom.localScope;
	}

	@Override
	public boolean isMandatory() {
		return this.builder.isMandatory();
	}

	@Override
	public boolean isImperative() {
		return this.imperative != null;
	}

	@Override
	public LocalScope getLocalScope() {
		return this.localScope;
	}

	@Override
	public boolean hasSubClauses() {
		if (this.localScope != null) {
			return this.localScope.hasSubClauses();
		}
		return getSubClauses().length != 0;
	}

	@Override
	public ClauseContainer getClauseContainer() {
		return this.localScope != null ? this.localScope : this;
	}

	@Override
	public Path getOutcome() {
		if (this.outcome != null) {
			return this.outcome;
		}
		return this.outcome = this.builder.outcome(this);
	}

	@Override
	public ReusedClause[] getReusedClauses() {
		if (this.reused != null) {
			return this.reused;
		}
		return this.reused = this.builder.reuseClauses(this);
	}

	public Block<?> parentheses(Group group) {

		final SentenceFactory<?, ?, ?> sentenceFactory =
				group.getStatements().getSentenceFactory();
		final Block<?> definition = sentenceFactory.groupParentheses(
				group,
				new BlockDistributor(group, this),
				new GroupRegistry(
						this,
						group.getStatements().getMemberRegistry()));

		return this.definition = definition;
	}

	public ImperativeBlock braces(Group group, String name) {

		final Statements<?> statements = group.getStatements();
		final SentenceFactory<?, ?, ?> sentenceFactory =
				statements.getSentenceFactory();
		final ImperativeBlock definition;

		if (group.getScope().toLocal() != null) {
			definition = sentenceFactory.groupBraces(
					group,
					new BlockDistributor(group, this),
					name,
					new GroupRegistry(
							this,
							group.getStatements().getMemberRegistry()));
		} else {
			definition = sentenceFactory.groupBraces(
					group,
					new BlockDistributor(group, this),
					name,
					new ImperativeGroupRegistry.Builder(group));
			this.localScope = definition.getScope();

			final LocalScopeClauseBase local = this.localScope;

			local.setClause(this);
		}

		this.imperative = definition;
		this.definition = definition;

		return definition;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {
		return groupClause(memberId, declaredIn);
	}

	@Override
	public void define(Reproducer reproducer) {
		if (this.localScope == null) {

			final Statement reproduction =
					this.definition.reproduce(reproducer);

			if (reproduction != null) {

				final Statements<?> statements = reproducer.getStatements();

				if (statements != null) {
					statements.statement(reproduction);
				}
			}

			return;
		}

		final LocalScope reproducedScope =
				reproducer.getMemberRegistry().reproduceLocalScope(
						reproducer,
						this.localScope);

		if (reproducedScope == null) {
			return;
		}

		final ImperativeBlock reproduction = this.imperative.reproduce(
				new ImperativeReproducer(this, reproducedScope, reproducer));

		if (reproduction == null) {
			return;
		}

		reproducer.getStatements().statement(
				reproduction.wrap(reproducer.distribute()));
	}

	protected void merge(Clause clause) {
		getContext().getLogger().ambiguousClause(clause, getDisplayName());
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		validate();
	}

	final ClauseBuilder getBuilder() {
		return this.builder;
	}

	private static final class BlockDistributor extends Distributor {

		private final Distributor enclosingDistributor;
		private final DeclaredGroupClause clause;

		BlockDistributor(
				Group group,
				DeclaredGroupClause clause) {
			this.enclosingDistributor = group.distribute();
			this.clause = clause;
		}

		@Override
		public CompilerContext getContext() {
			return this.clause.getContext();
		}

		@Override
		public Loggable getLoggable() {
			return this.clause.getLoggable();
		}

		@Override
		public ScopePlace getPlace() {
			return this.enclosingDistributor.getPlace();
		}

		@Override
		public Container getContainer() {
			return this.clause;
		}

		@Override
		public Scope getScope() {
			return this.enclosingDistributor.getScope();
		}

	}

	private static final class ImperativeReproducer extends Reproducer {

		private final Reproducer reproducer;
		private final LocalRegistry localRegistry;

		ImperativeReproducer(
				DeclaredGroupClause reproducingClause,
				LocalScope reproducedScope,
				Reproducer reproducer) {
			super(
					reproducingClause.localScope,
					new ImperativeBlock.BlockDistributor(
							new Location(
									reproducedScope.getContext(),
									reproducingClause),
							reproducedScope));
			this.reproducer = reproducer;
			this.localRegistry = new LocalRegistry(
					reproducedScope,
					reproducer.getMemberRegistry());
		}

		@Override
		public boolean phraseCreatesObject() {
			return this.reproducer.phraseCreatesObject();
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.reproducer.getPhrasePrefix();
		}

		@Override
		public MemberRegistry getMemberRegistry() {
			return this.localRegistry;
		}

		@Override
		public Statements<?> getStatements() {
			return null;
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope() == reproducingScope) {
				return this;
			}
			return this.reproducer.reproducerOf(reproducingScope);
		}

		@Override
		public String toString() {
			return this.reproducer.toString() + '[' + getScope() + ']';
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements<?> statements,
				Clause clause) {
			this.reproducer.applyClause(location, statements, clause);
		}

	}

}
