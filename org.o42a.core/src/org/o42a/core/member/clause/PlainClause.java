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
package org.o42a.core.member.clause;

import static org.o42a.core.AbstractScope.enclosingScopes;
import static org.o42a.core.object.ConstructionMode.FULL_CONSTRUCTION;
import static org.o42a.util.fn.Init.init;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.PrototypeMode;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public abstract class PlainClause
		extends Clause
		implements Scope, ClauseContainer {

	private final Init<Nesting> definitionNesting =
			init(() -> new ClauseDefinitionNesting(getKey()));
	private final Init<Path> enclosingScopePath =
			init(() -> getObject().ownerPath().toPath());
	private final Init<Set<Scope>> enclosingScopes =
			init(() -> enclosingScopes(this));
	private final AtomicInteger anonymusSeq = new AtomicInteger();

	public PlainClause(MemberClause member) {
		super(member);
		assert member.getDeclaration().getKind().isPlain() :
			"Plain clause expected";
	}

	protected PlainClause(MemberClause member, PlainClause propagatedFrom) {
		super(member);
		this.definitionNesting.set(propagatedFrom.getDefinitionNesting());
	}

	@Override
	public Path getEnclosingScopePath() {
		return this.enclosingScopePath.get();
	}

	@Override
	public ID getId() {
		return toMember().getId();
	}

	@Override
	public final Scope getScope() {
		return this;
	}

	@Override
	public final boolean isTopScope() {
		return false;
	}

	public final Nesting getDefinitionNesting() {
		return this.definitionNesting.get();
	}

	public abstract Obj getObject();

	@Override
	public final MemberContainer getContainer() {
		return getObject();
	}

	@Override
	public final Container getEnclosingContainer() {
		return toMember().getContainer();
	}

	@Override
	public final Scope getEnclosingScope() {
		return getEnclosingContainer().getScope();
	}

	@Override
	public final Set<? extends Scope> getEnclosingScopes() {
		return this.enclosingScopes.get();
	}

	@Override
	public PlainClause getFirstDeclaration() {
		return toMember()
				.getFirstDeclaration()
				.toClause()
				.clause()
				.toPlainClause();
	}

	@Override
	public PlainClause getLastDefinition() {
		return getFirstDeclaration();
	}

	@Override
	public boolean isClone() {
		return getLastDefinition() != this;
	}

	@Override
	public final Resolver resolver() {
		return new Resolver(this);
	}

	@Override
	public final Resolver walkingResolver(Resolver resolver) {
		return walkingResolver(resolver.getWalker());
	}

	@Override
	public final Resolver walkingResolver(PathWalker walker) {
		return new Resolver(this, walker);
	}

	@Override
	public boolean hasSubClauses() {
		return getSubClauses().length != 0;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {

		final MemberClause clause = getObject().clause(memberId, declaredIn);

		if (clause == null) {
			return null;
		}

		if (!toMember().isOverride()) {
			return clause;
		}

		final MemberKey key = toMember().getMemberKey();
		final PlainClause original =
				key.getOrigin()
				.getContainer()
				.member(key)
				.toClause()
				.clause()
				.toPlainClause();

		if (original.getObject().getScope() != clause.getMemberKey().getOrigin()) {
			return null;
		}

		return clause;
	}

	@Override
	public Clause toClause() {
		return this;
	}

	@Override
	public final ClauseContainer getClauseContainer() {
		return this;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return FULL_CONSTRUCTION;
	}

	@Override
	public final boolean requiresInstance() {
		if (getKind() != ClauseKind.EXPRESSION) {
			return true;
		}
		return isAssignment();
	}

	public abstract boolean isAssignment();

	public abstract AscendantsDefinition getAscendants();

	public abstract MemberKey getOverridden();

	public abstract PrototypeMode getPrototypeMode();

	@Override
	public MemberClause[] getSubClauses() {
		return getObject().getExplicitClauses();
	}

	@Override
	public final Field toField() {
		return null;
	}

	@Override
	public final Obj toObject() {
		return getObject();
	}

	@Override
	public final PlainClause toPlainClause() {
		return this;
	}

	@Override
	public final GroupClause toGroupClause() {
		return null;
	}

	@Override
	public Prediction predict(Prediction enclosing) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (is(other)) {
			return true;
		}

		final Obj object1 = getObject();

		if (object1 != null) {

			final Obj object2 = other.toObject();

			if (object2 != null) {
				return object1.type().derivedFrom(object2.type());
			}
		}

		final Clause clause2 = other.getContainer().toClause();

		if (clause2 == null) {
			return false;
		}

		return getKey().equals(clause2.getKey());
	}

	@Override
	public final ID nextAnonymousId() {
		return ID.id().anonymous(this.anonymusSeq.incrementAndGet());
	}

	@Override
	public final ScopeIR ir(Generator generator) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve() {
		getObject().resolveAll();
	}

	private static final class ClauseDefinitionNesting implements Nesting {

		private final MemberKey clauseKey;

		ClauseDefinitionNesting(MemberKey clauseKey) {
			this.clauseKey = clauseKey;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {

			final MemberClause clause =
					enclosing.getContainer().member(this.clauseKey).toClause();

			return clause.clause().toPlainClause().toObject();
		}

		@Override
		public String toString() {
			if (this.clauseKey == null) {
				return super.toString();
			}
			return this.clauseKey.toString();
		}

	}

}
