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
package org.o42a.core;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static org.o42a.core.object.ConstructionMode.FULL_CONSTRUCTION;
import static org.o42a.core.object.ConstructionMode.RUNTIME_CONSTRUCTION;
import static org.o42a.core.object.ConstructionMode.STRICT_CONSTRUCTION;

import java.util.HashSet;
import java.util.Set;

import org.o42a.core.member.field.Field;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.util.string.ID;


public abstract class AbstractScope implements Scope {

	public static boolean assertDerivedFrom(Scope scope, Scope other) {
		assert scope.derivedFrom(other) :
			scope + " is not derived from " + other;
		return true;
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
				new HashSet<>(enclosingScopes.size() + 1);

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
		final Obj object = container.toObject();

		if (object != null) {
			return object.getConstructionMode();
		}

		return STRICT_CONSTRUCTION;
	}

	public static PrefixPath pathTo(Scope fromScope, Scope toScope) {
		if (fromScope.is(toScope)) {
			return Path.SELF_PATH.toPrefix(fromScope);
		}

		final Path pathToEnclosing = pathToEnclosing(fromScope, toScope);

		assert pathToEnclosing != null :
			"Can not build path from " + fromScope + " to " + toScope;

		return pathToEnclosing.toPrefix(fromScope);
	}

	public static boolean contains(Scope scope, Scope other) {
		if (other.is(scope)) {
			return true;
		}
		return other.getEnclosingScopes().contains(scope);
	}

	private static Path pathToEnclosing(Scope scope, Scope targetScope) {

		Container enclosing = scope.getEnclosingContainer();
		Path enclosingPath = scope.getEnclosingScopePath();

		while (enclosing != null) {
			if (enclosing.getScope().is(targetScope)) {
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

	private Set<Scope> enclosingScopes;
	private int anonymousSeq;

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
	public boolean isClone() {
		return getLastDefinition() != this;
	}

	@Override
	public final Resolver resolver() {
		return new Resolver(this);
	}

	@Override
	public final Resolver walkingResolver(Resolver user) {
		return walkingResolver(user.getWalker());
	}

	@Override
	public final Resolver walkingResolver(PathWalker walker) {
		return new Resolver(this, walker);
	}

	@Override
	public Field toField() {
		return null;
	}

	@Override
	public Obj toObject() {
		return getContainer().toObject();
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return constructionMode(this);
	}

	@Override
	public final PrefixPath pathTo(Scope targetScope) {
		return pathTo(this, targetScope);
	}

	@Override
	public final boolean is(Scope scope) {
		return this == scope;
	}

	@Override
	public boolean contains(Scope other) {
		return contains(this, other);
	}

	@Override
	public final ID nextAnonymousId() {
		return getId().anonymous(++this.anonymousSeq);
	}

	@Override
	public final boolean assertDerivedFrom(Scope other) {
		return assertDerivedFrom(this, other);
	}

}
