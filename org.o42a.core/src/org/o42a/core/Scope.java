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

import java.util.Set;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.util.string.ID;


public interface Scope extends ContainerInfo {

	ID getId();

	boolean isTopScope();

	@Override
	MemberContainer getContainer();

	Scope getEnclosingScope();

	/**
	 * All scopes enclosing this one, including scope itself.
	 *
	 * @return a set of enclosing scopes.
	 */
	Set<? extends Scope> getEnclosingScopes();

	Container getEnclosingContainer();

    Path getEnclosingScopePath();

    /**
     * The first declaration of this scope.
     *
     * @return the scope, which is an origin of this one.
     */
	Scope getFirstDeclaration();

	/**
	 * The last definition of this scope.
	 *
	 * @return the last explicit definition of this scope.
	 */
	Scope getLastDefinition();

	/**
	 * Checks whether this scope is a clone.
	 *
	 * <p>Scope is clone if it is not explicitly defined.</p>
	 *
	 * @return <code>true</code> if scope is a clone or <code>false</code>
	 * otherwise.
	 *
	 * @see #getLastDefinition()
	 */
	boolean isClone();

    Resolver resolver();

    Resolver walkingResolver(Resolver user);

    Resolver walkingResolver(PathWalker walker);

    Member toMember();

    Field toField();

    Obj toObject();

	ConstructionMode getConstructionMode();

	Prediction predict(Prediction enclosing);

	boolean derivedFrom(Scope other);

	default PrefixPath pathTo(Scope targetScope) {
		return AbstractScope.pathTo(this, targetScope);
	}

	default boolean is(Scope scope) {
		return this == scope;
	}

	default boolean contains(Scope other) {
		if (is(other)) {
			return true;
		}
		return other.getEnclosingScopes().contains(this);
	}

	ID nextAnonymousId();

	ScopeIR ir(Generator generator);

	default boolean assertDerivedFrom(Scope other) {
		// Don't place an assertion here. JDK-8025141
		AbstractScope.assertDerivedFrom(this, other);
		return true;
	}

}
