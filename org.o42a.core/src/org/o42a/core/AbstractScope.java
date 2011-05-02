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
package org.o42a.core;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static org.o42a.core.artifact.object.ConstructionMode.FULL_CONSTRUCTION;
import static org.o42a.core.artifact.object.ConstructionMode.RUNTIME_CONSTRUCTION;
import static org.o42a.core.artifact.object.ConstructionMode.STRICT_CONSTRUCTION;
import static org.o42a.core.def.Rescoper.transparentRescoper;

import java.util.HashSet;
import java.util.Set;

import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.core.ref.path.Path;
import org.o42a.util.use.UserInfo;


public abstract class AbstractScope implements Scope {

	public static void assertDerivedFrom(Scope scope, Scope other) {
		assert scope.derivedFrom(other) :
			scope + " is not derived from " + other;
	}

	public static Scope enclosingScope(Scope scope) {

		final Container enclosingContainer = scope.getEnclosingContainer();

		if (enclosingContainer == null) {
			return null;
		}

		return enclosingContainer.getScope();
	}

	public static Set<Scope> enclosingScopes(Scope scope) {

		final Scope enclosingScope = scope.getEnclosingScope();

		if (enclosingScope == null) {
			return singleton(scope);
		}

		final Set<? extends Scope> enclosingScopes =
			enclosingScope.getEnclosingScopes();
		final HashSet<Scope> result =
			new HashSet<Scope>(enclosingScopes.size() + 1);

		result.addAll(enclosingScopes);
		result.add(scope);

		return unmodifiableSet(result);
	}

	public static ConstructionMode constructionMode(Scope scope) {

		final Scope enclosingScope = scope.getEnclosingScope();

		if (enclosingScope == null) {
			return FULL_CONSTRUCTION;
		}

		final ConstructionMode enclosingMode =
			enclosingScope.getConstructionMode();

		if (enclosingMode.isRuntime()) {
			return RUNTIME_CONSTRUCTION;
		}

		final Container container = scope.getContainer();

		if (container.toLocal() != null) {
			return STRICT_CONSTRUCTION;
		}

		final Obj object = container.toObject();

		if (object != null) {
			return object.getConstructionMode();
		}

		return STRICT_CONSTRUCTION;
	}

	public static Path pathTo(Scope scope, Scope targetScope) {
		if (scope == targetScope) {
			return Path.SELF_PATH;
		}

		final Path pathToEnclosing = pathToEnclosing(scope, targetScope);

		if (pathToEnclosing != null) {
			return pathToEnclosing;
		}

		return pathToMember(scope, targetScope);
	}

	public static Rescoper rescoperTo(Scope fromScope, Scope toScope) {
		if (fromScope == toScope) {
			return transparentRescoper(toScope);
		}

		final Path path = toScope.pathTo(fromScope);

		assert path != null :
				"Can not rescope from " + fromScope + " to " + toScope;

		return path.rescoper(toScope);
	}

	public static boolean contains(Scope scope, Scope other) {
		if (other == scope) {
			return true;
		}
		return other.getEnclosingScopes().contains(scope);
	}

	private static Path pathToEnclosing(Scope scope, Scope targetScope) {

		Container enclosing = scope.getEnclosingContainer();
		Path enclosingPath = scope.getEnclosingScopePath();

		while (enclosing != null) {
			if (enclosing.getScope() == targetScope) {
				return enclosingPath;
			}

			final Container enc = enclosing.getScope().getEnclosingContainer();

			if (enc == null) {
				return null;
			}

			final Scope enclosingScope = enc.getScope();

			if (enclosingScope.isTopScope()) {
				return null;
			}

			enclosingPath = enclosingPath.append(
					enclosing.getScope().getEnclosingScopePath());
			enclosing = enc;
		}

		return null;
	}

	private static Path pathToMember(Scope scope, Scope targetScope) {

		Member member = targetScope.getContainer().toMember();

		if (member == null) {
			return null;
		}

		Container enclosing = targetScope.getEnclosingContainer();

		if (enclosing == null) {
			return null;
		}

		final Scope enclosingScope = enclosing.getScope();

		if (enclosingScope == scope) {
			return member.getKey().toPath();
		}

		final Path pathToMember = pathToMember(scope, enclosingScope);

		if (pathToMember == null) {
			return null;
		}

		return pathToMember.append(member.getKey());
	}

	private final ResolverFactory<?> resolverFactory;
	private Set<Scope> enclosingScopes;

	public AbstractScope() {
		this.resolverFactory = createResolverFactory();
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
	public final Scope getEnclosingScope() {
		return enclosingScope(this);
	}

	@Override
	public final Set<Scope> getEnclosingScopes() {
		if (this.enclosingScopes != null) {
			return this.enclosingScopes;
		}
		return this.enclosingScopes = enclosingScopes(this);
	}

	@Override
	public Resolver newResolver(UserInfo user) {
		return this.resolverFactory.newResolver(user);
	}

	@Override
	public Field<?> toField() {
		return null;
	}

	@Override
	public LocalScope toLocal() {
		return null;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return constructionMode(this);
	}

	@Override
	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	@Override
	public Path pathTo(Scope targetScope) {
		return pathTo(this, targetScope);
	}

	@Override
	public final Rescoper rescoperTo(Scope toScope) {
		return rescoperTo(this, toScope);
	}

	@Override
	public boolean contains(Scope other) {
		return contains(this, other);
	}

	@Override
	public final void assertDerivedFrom(Scope other) {
		assertDerivedFrom(this, other);
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

	protected ResolverFactory<?> createResolverFactory() {
		return Resolver.resolverFactory(this);
	}

	protected final ResolverFactory<?> resolverFactory() {
		return this.resolverFactory;
	}

}
