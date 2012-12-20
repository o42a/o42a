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
package org.o42a.core.member.local;

import static org.o42a.core.AbstractContainer.parentContainer;
import static org.o42a.core.ref.impl.prediction.LocalPrediction.predictLocal;

import java.util.Set;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.ClauseContainer;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.impl.ExplicitLocalScope;
import org.o42a.core.member.local.impl.LocalOwnerStep;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.SourceInfo;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.LocalScopeBase;
import org.o42a.util.log.Loggable;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public abstract class LocalScope
		extends LocalScopeBase
		implements MemberContainer, ClauseContainer, SourceInfo {

	protected static ExplicitLocalScope explicitLocal(LocalScope localScope) {
		return localScope.explicit();
	}

	private final MemberLocal member;
	private final Obj owner;
	private final OwningLocal owningLocal = new OwningLocal(this);
	private final Path ownerScopePath;
	private final
	ResolverFactory<LocalResolver, FullLocalResolver> resolverFactory;
	private Set<Scope> enclosingScopes;
	private ID id;
	private int anonymousSeq;
	private boolean allResolved;

	public LocalScope(MemberLocal member) {
		this.member = member;
		this.owner = member.getContainer().toObject();
		this.ownerScopePath = new LocalOwnerStep(this).toPath();
		this.resolverFactory = new LocalResolver.LocalResolverFactory(this);
	}

	@Override
	public final ID getId() {
		if (this.id != null) {
			return this.id;
		}
		return toMember().getMemberKey().toID();
	}

	public final Obj getOwner() {
		return this.owner;
	}

	public abstract LocalScope getPropagatedFrom();

	public abstract Name getName();

	public final boolean isExplicit() {
		return getOwner().is(getSource());
	}

	@Override
	public final CompilerContext getContext() {
		return this.member.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.member.getLoggable();
	}

	@Override
	public final CompilerLogger getLogger() {
		return this.member.getLogger();
	}

	@Override
	public final MemberContainer getContainer() {
		return this;
	}

	@Override
	public final ScopePlace getPlace() {
		return toMember().getPlace();
	}

	@Override
	public final Scope getScope() {
		return this;
	}

	@Override
	public boolean isTopScope() {
		return false;
	}

	@Override
	public final Container getParentContainer() {
		return parentContainer(this);
	}

	@Override
	public final Container getEnclosingContainer() {
		return toMember().getContainer();
	}

	@Override
	public final Scope getEnclosingScope() {
		return AbstractScope.enclosingScope(this);
	}

	@Override
	public final Set<Scope> getEnclosingScopes() {
		if (this.enclosingScopes != null) {
			return this.enclosingScopes;
		}
		return this.enclosingScopes = AbstractScope.enclosingScopes(this);
	}

	@Override
	public final Path getEnclosingScopePath() {
		return this.ownerScopePath;
	}

	@Override
	public final LocalScope getFirstDeclaration() {
		return explicit();
	}

	@Override
	public final LocalScope getLastDefinition() {
		return explicit();
	}

	@Override
	public final boolean isClone() {
		return explicit().is(this);
	}

	public abstract ImperativeBlock getBlock();

	@Override
	public final ConstructionMode getConstructionMode() {
		return AbstractScope.constructionMode(this);
	}

	@Override
	public final Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return member(user, accessor, memberId, declaredIn);
	}

	@Override
	public final LocalResolver resolver() {
		return resolverFactory().resolver();
	}

	@Override
	public final LocalResolver walkingResolver(Resolver user) {
		return walkingResolver(user.getWalker());
	}

	@Override
	public final LocalResolver walkingResolver(PathWalker walker) {
		return resolverFactory().walkingResolver(walker);
	}

	@Override
	public final MemberLocal toMember() {
		return this.member;
	}

	@Override
	public final Obj toObject() {
		return null;
	}

	@Override
	public final LocalScope toLocal() {
		return this;
	}

	@Override
	public final Namespace toNamespace() {
		return null;
	}

	@Override
	public final Field toField() {
		return null;
	}

	public final LocalPlace placeOf(PlaceInfo other) {

		Scope scope = other.getScope();

		if (is(scope)) {
			return other.getPlace().toLocal();
		}
		for (;;) {

			final Container enclosingContainer = scope.getEnclosingContainer();

			if (enclosingContainer == null) {
				return null;
			}

			final Scope enclosingScope = enclosingContainer.getScope();

			if (is(enclosingScope)) {

				final LocalPlace result = scope.getPlace().toLocal();

				assert result != null :
					scope.getPlace()
					+ " is not local place, despite it belongs to " + this;
				assert is(result.getAppearedIn()) :
					scope + " belongs to local place " + this
					+ ", but it's place appeared in " + result.getAppearedIn();

				return result;
			}

			scope = enclosingScope;
		}
	}

	@Override
	public final Prediction predict(Prediction enclosing) {
		return predictLocal(enclosing, this);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (is(other)) {
			return true;
		}

		final LocalScope otherLocal = other.toLocal();

		if (otherLocal == null) {
			return false;
		}

		return getOwner().type().derivedFrom(otherLocal.getOwner().type());
	}

	@Override
	public final PrefixPath pathTo(Scope targetScope) {
		return AbstractScope.pathTo(this, targetScope);
	}

	@Override
	public final boolean is(Scope scope) {
		return this == scope;
	}

	@Override
	public final boolean contains(Scope other) {
		return AbstractScope.contains(this, other);
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	public void resolveAll() {
		if (this.allResolved) {
			return;
		}
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			for (Member member : getMembers()) {
				member.resolveAll();
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public final ID nextAnonymousId() {
		return getId().anonymous(++this.anonymousSeq);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public final boolean assertDerivedFrom(Scope other) {
		return AbstractScope.assertDerivedFrom(this, other);
	}

	@Override
	public abstract LocalIR ir(Generator generator);

	public final void assertExplicit() {
		assert isExplicit() :
			this + " is propagated";
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return this.member.toString();
	}

	protected final
	ResolverFactory<LocalResolver, FullLocalResolver> resolverFactory() {
		return this.resolverFactory;
	}

	protected final OwningLocal toOwner() {
		return this.owningLocal;
	}

	protected abstract boolean addMember(Member member);

	protected abstract ExplicitLocalScope explicit();

}
