/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.member.clause;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberRegistry;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.St;
import org.o42a.core.st.sentence.*;
import org.o42a.util.Lambda;


final class DeclaredGroupClause extends GroupClause implements ClauseContainer {

	static DeclaredGroupClause declaredGroupClause(ClauseBuilder builder) {
		return new GroupMember(builder).clause;
	}

	private final ClauseBuilder builder;
	private ReusedClause[] reused;
	private Block<?> definition;
	private ImperativeBlock imperative;
	private LocalScope localScope;

	private DeclaredGroupClause(MemberClause clause, ClauseBuilder builder) {
		super(clause);
		this.builder = builder;
	}

	private DeclaredGroupClause(
			Container enclosingContainer,
			DeclaredGroupClause overridden) {
		super(enclosingContainer, overridden);
		this.builder = overridden.builder;
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
	public ClauseContainer getClauseContainer() {
		return this.localScope != null ? this.localScope : this;
	}

	@Override
	public ReusedClause[] getReusedClauses() {
		if (this.reused != null) {
			return this.reused;
		}
		return this.reused = this.builder.reuseClauses(this);
	}

	@Override
	public Clause clause(MemberId memberId, Obj declaredIn) {
		return groupClause(memberId, declaredIn);
	}

	@Override
	public void define(Reproducer reproducer) {
		if (this.localScope == null) {

			final St reproduction = this.definition.reproduce(reproducer);

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
				new ImperativeReproducer(
						this.localScope,
						reproducedScope,
						reproducer));

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
	protected void doResolveAll() {
		super.doResolveAll();
		validate();
	}

	@Override
	protected GroupClause propagate(Scope enclosingScope) {
		return new DeclaredGroupClause(enclosingScope.getContainer(), this);
	}

	Block<?> parentheses(Group group) {

		final SentenceFactory<?, ?, ?> sentenceFactory =
			group.getStatements().getSentenceFactory();
		final Block<?> definition = sentenceFactory.groupParentheses(
				group,
				new BlockDistributor(group.distribute(), this),
				new GroupRegistry(
						this,
						group.getStatements().getMemberRegistry()));

		return this.definition = definition;
	}

	ImperativeBlock braces(Group group, String name) {

		final Statements<?> statements = group.getStatements();
		final SentenceFactory<?, ?, ?> sentenceFactory =
			statements.getSentenceFactory();
		final ImperativeBlock definition;

		if (group.getScope().toLocal() != null) {
			definition = sentenceFactory.groupBraces(
					group,
					new BlockDistributor(group.distribute(), this),
					name,
					new GroupRegistry(
							this,
							group.getStatements().getMemberRegistry()));
		} else {
			definition = sentenceFactory.groupBraces(
					group,
					new BlockDistributor(group.distribute(), this),
					name,
					new ImperativeRegistryBuilder(group));
			this.localScope = definition.getScope();

			final LocalScopeClauseBase local = this.localScope;

			local.setClause(this);
		}

		this.imperative = definition;
		this.definition = definition;

		return definition;
	}

	final ClauseBuilder getBuilder() {
		return this.builder;
	}

	private static final class GroupMember extends MemberClause {

		private final DeclaredGroupClause clause;

		GroupMember(ClauseBuilder builder) {
			super(builder.getDeclaration());
			this.clause = new DeclaredGroupClause(this, builder);
		}

		@Override
		public DeclaredGroupClause toClause() {
			return this.clause;
		}

		@Override
		protected void merge(Member member) {

			final Clause clause = member.toClause();

			if (clause == null) {
				getContext().getLogger().notClauseDeclaration(member);
				return;
			}

			this.clause.merge(clause);
		}

	}

	private static final class BlockDistributor extends Distributor {

		private final Distributor enclosingDistributor;
		private final DeclaredGroupClause clause;

		BlockDistributor(
				Distributor enclosingDistributor,
				DeclaredGroupClause clause) {
			this.enclosingDistributor = enclosingDistributor;
			this.clause = clause;
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

	private static final class GroupRegistry
			extends MemberRegistry
			implements Lambda<MemberRegistry, LocalScope> {

		private final DeclaredGroupClause group;
		private final MemberRegistry registry;

		GroupRegistry(DeclaredGroupClause group, MemberRegistry registry) {
			this.group = group;
			this.registry = registry;
		}

		@Override
		public Obj getOwner() {
			return this.registry.getOwner();
		}

		@Override
		public DeclaredField<?> declareField(FieldDeclaration declaration) {
			return this.registry.declareField(
					declaration.inGroup(getGroupId()));
		}

		@Override
		public ClauseBuilder newClause(ClauseDeclaration declaration) {
			return this.registry.newClause(declaration.inGroup(getGroupId()));
		}

		@Override
		public boolean declareBlock(LocationSpec location, String name) {
			return this.registry.declareBlock(location, name);
		}

		@Override
		public void declareMember(Member member) {
			this.registry.declareMember(member);
		}

		@Override
		public String anonymousBlockName() {
			return this.registry.anonymousBlockName();
		}

		@Override
		public MemberRegistry get(LocalScope arg) {
			return this;
		}

		private final MemberId getGroupId() {

			final MemberId memberId = this.group.getDeclaration().getMemberId();
			final MemberId[] ids = memberId.getIds();

			return ids[ids.length - 1];
		}

	}

	private static final class ImperativeRegistryBuilder
			implements Lambda<MemberRegistry, LocalScope> {

		private final Group group;

		ImperativeRegistryBuilder(Group group) {
			this.group = group;
		}

		@Override
		public MemberRegistry get(LocalScope arg) {
			return new ImperativeRegistry(
					arg,
					this.group.getStatements().getMemberRegistry());
		}


	}

	private static final class ImperativeRegistry extends LocalRegistry {

		ImperativeRegistry(LocalScope scope, MemberRegistry ownerRegistry) {
			super(scope, ownerRegistry);
		}

		@Override
		public ClauseBuilder newClause(ClauseDeclaration declaration) {
			if (declaration.getKind() == ClauseKind.OVERRIDER) {
				declaration.getLogger().prohibitedOverriderClause(declaration);
				return null;
			}

			final MemberRegistryClauseBase registry = this;

			return registry.createClause(declaration);
		}

	}

	private static final class ImperativeReproducer extends Reproducer {

		private final Reproducer reproducer;
		private final LocalRegistry localRegistry;

		ImperativeReproducer(
				LocalScope reproducingScope,
				LocalScope reproducedScope,
				Reproducer reproducer) {
			super(
					reproducingScope,
					new ImperativeBlock.BlockDistributor(reproducedScope));
			this.reproducer = reproducer;
			this.localRegistry = new LocalRegistry(
					reproducedScope,
					reproducer.getMemberRegistry());
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
				LocationSpec location,
				Statements<?> statements,
				Clause clause) {
			this.reproducer.applyClause(location, statements, clause);
		}

	}

}
