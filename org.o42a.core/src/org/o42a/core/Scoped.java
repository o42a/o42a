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

import org.o42a.ast.Node;


public class Scoped extends Location implements ScopeSpec {

	public static final void assertScopeIs(ScopeSpec scoped, Scope scope) {
		assert scoped.getScope() == scope :
			scoped + " has scope " + scoped.getScope()
			+ ", but " + scope + " expected";
	}

	public static final void assertCompatible(ScopeSpec scoped, Scope scope) {
		assert scope.derivedFrom(scoped.getScope()) :
			"Scope " + scope + " is not compatible with "
			+ scoped + ": it's not derived from " + scoped.getScope();
	}

	public static final void assertSameScope(
			ScopeSpec scoped,
			ScopeSpec other) {
		assert scoped.getScope() == other.getScope() :
			scoped + " has scope " + scoped.getScope()
			+ ", which differs from scope " + other.getScope()
			+ " of " + other;
	}

	public static final void assertCompatibleScope(
			ScopeSpec scoped,
			ScopeSpec other) {
		assert other.getScope().derivedFrom(scoped.getScope()) :
			other + " scope " + other.getScope()
			+ " is not compatible with " + scoped
			+ ": it's not derived from " + scoped.getScope();
	}

	private final Scope scope;

	public Scoped(LocationSpec location, Scope scope) {
		super(location);
		assert scope != null :
			"Scope not specified";
		this.scope = scope;
	}

	public Scoped(CompilerContext context, Node node, Scope scope) {
		super(context, node);
		this.scope = scope;
	}

	Scoped(CompilerContext context) {
		super(context, null);
		this.scope = null;
	}

	@Override
	public Scope getScope() {
		return this.scope;
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeSpec other) {
		assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeSpec other) {
		assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(getScope()).append('@').append(getContext());

		final Node node = getNode();

		if (node != null) {
			out.append("]:[");
			node.printContent(out);
		}
		out.append(']');

		return out.toString();
	}

}
