/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.core.artifact.object.ConstructionMode.FULL_CONSTRUCTION;

import java.util.Set;

import org.o42a.codegen.Generator;
import org.o42a.core.AbstractScope;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.util.use.UserInfo;


public abstract class PlainClause
		extends Clause
		implements Scope, ClauseContainer {

	private final ResolverFactory<Resolver> resolverFactory =
			Resolver.resolverFactory(this);
	private Obj clauseObject;
	private Path enclosingScopePath;
	private Set<Scope> enclosingScopes;

	public PlainClause(MemberClause member) {
		super(member);
		assert member.getDeclaration().getKind().isPlain() :
			"Plain clause expected";
	}

	protected PlainClause(MemberClause member, PlainClause propagatedFrom) {
		super(member);
		setClauseObject(propagateClauseObject(propagatedFrom));
	}

	@Override
	public Path getEnclosingScopePath() {
		if (this.enclosingScopePath != null) {
			return this.enclosingScopePath;
		}
		if (getEnclosingContainer().getScope().isTopScope()) {
			return null;
		}

		final Obj object = getClauseObject();

		if (object == null) {
			return null;
		}

		return this.enclosingScopePath = object.scopePath();
	}

	@Override
	public final Scope getScope() {
		return this;
	}

	@Override
	public final boolean isTopScope() {
		return false;
	}

	@Override
	public final Obj getArtifact() {
		return getObject();
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
		if (this.enclosingScopes != null) {
			return this.enclosingScopes;
		}
		return this.enclosingScopes = enclosingScopes(this);
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
	public final Resolver dummyResolver() {
		return this.resolverFactory.dummyResolver();
	}

	@Override
	public final Resolver newResolver(UserInfo user) {
		return this.resolverFactory.newResolver(user);
	}

	@Override
	public final Resolver walkingResolver(Resolver user) {
		return walkingResolver(user, user.getWalker());
	}

	@Override
	public final Resolver walkingResolver(
			UserInfo user,
			PathWalker walker) {
		return this.resolverFactory.walkingResolver(user, walker);
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

		final MemberKey key = toMember().getKey();
		final PlainClause original =
				key.getOrigin()
				.getContainer()
				.member(key)
				.toClause()
				.clause()
				.toPlainClause();

		if (original.getObject().getScope() != clause.getKey().getOrigin()) {
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

	public abstract boolean isSubstitution();

	public abstract AscendantsDefinition getAscendants();

	public abstract MemberKey getOverridden();

	public abstract boolean isPrototype();

	@Override
	public MemberClause[] getSubClauses() {
		return getObject().getExplicitClauses();
	}

	@Override
	public final Field<?> toField() {
		return null;
	}

	@Override
	public final Obj toObject() {
		return getObject();
	}

	@Override
	public final LocalScope toLocal() {
		return null;
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
		if (this == other) {
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
	public final ScopeIR ir(Generator generator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final PrefixPath pathTo(Scope targetScope) {
		return AbstractScope.pathTo(this, targetScope);
	}

	@Override
	public boolean contains(Scope other) {
		return AbstractScope.contains(this, other);
	}

	@Override
	public final void assertDerivedFrom(Scope other) {
		AbstractScope.assertDerivedFrom(this, other);
	}

	protected Obj getClauseObject() {
		return this.clauseObject;
	}

	protected Obj setClauseObject(Obj clauseObject) {
		return this.clauseObject = clauseObject;
	}

	@Override
	protected void fullyResolve() {
		getObject().resolveAll();
	}

	protected abstract Obj propagateClauseObject(PlainClause overridden);

}
