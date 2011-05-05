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
package org.o42a.core.member.local;

import static org.o42a.core.AbstractContainer.findContainerPath;
import static org.o42a.core.AbstractContainer.parentContainer;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.util.use.User.dummyUser;

import java.util.Collection;
import java.util.Set;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.SourceInfo;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.ClauseContainer;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.LocalScopeBase;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.UserInfo;


public abstract class LocalScope
		extends LocalScopeBase
		implements Container, ClauseContainer, SourceInfo {

	static ExplicitLocalScope explicitScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			String name) {
		return new ExplicitLocalScope(
				location,
				distributor,
				owner,
				name);
	}

	static ExplicitLocalScope reproducedScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			ExplicitLocalScope reproducedFrom) {
		return new ExplicitLocalScope(
				location,
				distributor,
				owner,
				reproducedFrom);
	}

	private final CompilerContext context;
	private final Loggable loggable;
	private final Obj owner;
	private final OwningLocal owningLocal = new OwningLocal(this);
	private final Path ownerScopePath;
	private final ResolverFactory<LocalResolver> resolverFactory;
	private Set<Scope> enclosingScopes;

	LocalScope(LocationInfo location, Obj owner) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.owner = owner;
		this.ownerScopePath = new OwnerPathFragment().toPath();
		this.resolverFactory = new LocalResolver.Factory(this);
	}
	
	public final Obj getOwner() {
		return this.owner;
	}

	public abstract String getName();

	public final boolean isExplicit() {
		return getOwner() == getSource();
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public final CompilerLogger getLogger() {
		return this.context.getLogger();
	}

	@Override
	public final Container getContainer() {
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

	public abstract ImperativeBlock getBlock();

	public abstract Collection<Member> getMembers();

	@Override
	public final ConstructionMode getConstructionMode() {
		return AbstractScope.constructionMode(this);
	}

	@Override
	public final LocalResolver dummyResolver() {
		return resolverFactory().dummyResolver();
	}

	@Override
	public final LocalResolver newResolver() {
		return resolverFactory().newResolver();
	}

	@Override
	public final LocalResolver newResolver(UserInfo user) {
		return resolverFactory().newResolver(user);
	}

	@Override
	public abstract MemberLocal toMember();

	@Override
	public final Artifact<?> toArtifact() {
		return null;
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
	public final Field<?> toField() {
		return null;
	}

	public final LocalPlace placeOf(ScopeInfo other) {

		Scope scope = other.getScope();

		if (scope == this) {
			return null;
		}
		for (;;) {

			final Container enclosingContainer = scope.getEnclosingContainer();

			if (enclosingContainer == null) {
				return null;
			}

			final Scope enclosingScope = enclosingContainer.getScope();

			if (enclosingScope == this) {

				final LocalPlace result = scope.getPlace().toLocal();

				assert result != null :
					scope.getPlace()
					+ " is not local place, despite it belongs to " + this;
				assert result.getAppearedIn() == this :
					scope + " belongs to local place " + this
					+ ", but it's place appeared in " + result.getAppearedIn();

				return result;
			}

			scope = enclosingScope;
		}
	}

	@Override
	public final Path findPath(
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {
		return findContainerPath(this, user, memberId, declaredIn);
	}

	@Override
	public final boolean contains(Scope other) {
		return AbstractScope.contains(this, other);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final LocalScope otherLocal = other.toLocal();

		if (otherLocal == null) {
			return false;
		}

		return getOwner().type().useBy(dummyUser()).derivedFrom(
				otherLocal.getOwner().type().useBy(dummyUser()));
	}

	@Override
	public final Path pathTo(Scope targetScope) {
		return AbstractScope.pathTo(this, targetScope);
	}

	@Override
	public final Rescoper rescoperTo(Scope toScope) {
		return AbstractScope.rescoperTo(this, toScope);
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	public abstract void resolveAll();

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
	public final void assertDerivedFrom(Scope other) {
		AbstractScope.assertDerivedFrom(this, other);
	}

	@Override
	public abstract LocalIR ir(Generator generator);

	public final void assertExplicit() {
		assert isExplicit() :
			this + " is propagated";
	}

	@Override
	protected final LocalScope propagateTo(Obj owner) {
		if (owner == getOwner()) {
			return this;
		}
		owner.assertDerivedFrom(getOwner());
		return new PropagatedLocalScope(this, owner);
	}

	protected final ResolverFactory<LocalResolver> resolverFactory() {
		return this.resolverFactory;
	}

	final OwningLocal toOwner() {
		return this.owningLocal;
	}

	abstract boolean addMember(Member member);

	abstract ExplicitLocalScope explicit();

	private final class OwnerPathFragment extends PathFragment {

		@Override
		public Container resolve(
				LocationInfo location,
				UserInfo user,
				Path path,
				int index,
				Scope start,
				PathWalker walker) {

			final LocalScope local = start.toLocal();

			local.assertDerivedFrom(LocalScope.this);

			final Obj owner = local.getOwner();

			walker.up(local, this, owner);

			return owner;
		}

		@Override
		public PathReproduction reproduce(
				LocationInfo location,
				Reproducer reproducer,
				Scope scope) {
			return reproducedPath(scope.toLocal().getEnclosingScopePath());
		}

		@Override
		public HostOp write(CodeDirs dirs, HostOp start) {

			final LocalOp local = start.toLocal();

			assert local != null :
				start + " is not local";

			return local.getBuilder().owner();
		}

		@Override
		public String toString() {
			return "Owner[" + LocalScope.this + ']';
		}

		@Override
		protected PathFragment rebuild(PathFragment prev) {
			return prev.combineWithLocalOwner(getOwner());
		}

	}

}
