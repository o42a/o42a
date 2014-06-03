/*
    Compiler Core
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
package org.o42a.core.member.clause.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.st.CommandEnv.defaultEnv;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.*;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.impl.imperative.ImperativeMemberRegistry;
import org.o42a.core.st.sentence.*;
import org.o42a.util.string.Name;


public final class DeclaredGroupClause
		extends GroupClause
		implements ClauseContainer {

	public static DeclaredGroupClause declaredGroupClause(
			ClauseBuilder builder) {
		return new DeclaredGroupClauseMember(builder).clause();
	}

	private final ClauseBuilder builder;
	private ReusedClause[] reused;
	private Block definition;
	private ImperativeBlock imperative;

	DeclaredGroupClause(
			DeclaredGroupClauseMember member,
			ClauseBuilder builder) {
		super(member);
		this.builder = builder;
	}

	DeclaredGroupClause(
			MemberClause clause,
			DeclaredGroupClause propagatedFrom) {
		super(clause);
		this.builder = propagatedFrom.builder;
		this.definition = propagatedFrom.definition;
		this.imperative = propagatedFrom.imperative;
	}

	@Override
	public final boolean isMandatory() {
		return getBuilder().isMandatory();
	}

	@Override
	public final ClauseSubstitution getSubstitution() {
		return getBuilder().getSubstitution();
	}

	@Override
	public final boolean isImperative() {
		return this.imperative != null;
	}

	public final boolean isTopLevelImperative() {
		if (this.imperative == null) {
			return false;
		}
		return this.imperative.getEnclosing()
				.getSentenceFactory()
				.isDeclarative();
	}

	@Override
	public final boolean hasSubClauses() {
		return getSubClauses().length != 0;
	}

	@Override
	public ClauseContainer getClauseContainer() {
		return this;
	}

	@Override
	public ReusedClause[] getReusedClauses() {
		if (this.reused != null) {
			return this.reused;
		}
		return this.reused = this.builder.reuseClauses(this);
	}

	public Block parentheses(Group group) {
		if (isImperative()) {
			return braces(group, null);
		}

		final SentenceFactory sentenceFactory =
				group.getStatements().getSentenceFactory();
		final Block definition = sentenceFactory.groupParentheses(
				group,
				new BlockDistributor(group, this),
				new GroupRegistry(
						this,
						group.getStatements().getMemberRegistry()));

		return this.definition = definition;
	}

	public ImperativeBlock braces(Group group, Name name) {

		final SentenceFactory sentenceFactory =
				group.getStatements().getSentenceFactory();
		final ImperativeBlock definition;

		if (isImperative()) {
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
					new ImperativeGroupRegistry(
							this,
							group.getStatements().getMemberRegistry()));
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
		if (!isTopLevelImperative()) {

			final Statement reproduction =
					this.definition.reproduce(reproducer);

			if (reproduction != null) {

				final Statements statements = reproducer.getStatements();

				if (statements != null) {
					statements.statement(reproduction);
				}
			}

			return;
		}

		final ImperativeBlock reproduction = this.imperative.reproduce(
				new ImperativeReproducer(reproducer));

		if (reproduction == null) {
			return;
		}

		reproducer.getStatements().statement(reproduction);
	}

	protected void merge(Clause clause) {
		getContext().getLogger().ambiguousClause(
				clause.getLocation(),
				getDisplayName());
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		validate();
		if (isTopLevel()) {
			this.definition.command(defaultEnv(getContext().getLogger()))
			.resolveAll(
					getScope().resolver().fullResolver(
							dummyUser(),
							TARGET_REF_USAGE));
		}
	}

	final ClauseBuilder getBuilder() {
		return this.builder;
	}

	private static final class BlockDistributor extends Distributor {

		private final Distributor enclosingDistributor;
		private final DeclaredGroupClause clause;

		BlockDistributor(Group group, DeclaredGroupClause clause) {
			this.enclosingDistributor = group.distribute();
			this.clause = clause;
		}

		@Override
		public Location getLocation() {
			return this.clause.getLocation();
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
		private final ImperativeMemberRegistry registry;

		ImperativeReproducer(Reproducer reproducer) {
			super(
					reproducer.getReproducingScope(),
					reproducer.distribute());
			this.reproducer = reproducer;
			this.registry = new ImperativeMemberRegistry(
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
			return this.registry;
		}

		@Override
		public Statements getStatements() {
			return null;
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope().is(reproducingScope)) {
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
				Statements statements,
				Clause clause) {
			this.reproducer.applyClause(location, statements, clause);
		}

	}

}
