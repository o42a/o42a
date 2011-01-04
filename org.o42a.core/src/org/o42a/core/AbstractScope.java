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

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.ast.Node;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldScope;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableVisitor;


public abstract class AbstractScope extends FieldScope implements Scope {

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
		for (;;) {
			if (other == scope) {
				return true;
			}

			final Container otherContainer = other.getEnclosingContainer();

			if (otherContainer == null) {
				return false;
			}
			other = otherContainer.getScope();
		}
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
	public Field<?> toField() {
		return null;
	}

	@Override
	public LocalScope toLocal() {
		return null;
	}

	@Override
	public boolean isRuntime() {

		final Container container = getContainer();

		if (container.toLocal() != null) {
			return true;
		}

		final Obj object = container.toObject();

		return object != null && object.isRuntime();
	}

	@Override
	public Loggable getLoggable() {

		final Node node = getNode();

		return node != null ? node : this;
	}

	@Override
	public Object getLoggableData() {
		return this;
	}

	@Override
	public LogInfo getPreviousLogInfo() {
		return null;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {

		final Node node = getNode();

		if (node != null) {
			return node.accept(visitor, p);
		}

		return visitor.visitData(this, p);
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
	public final boolean contains(Scope other) {
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
	public final void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeSpec other) {
		Scoped.assertCompatibleScope(this, other);
	}

}
