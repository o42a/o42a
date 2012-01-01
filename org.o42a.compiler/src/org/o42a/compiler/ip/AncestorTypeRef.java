/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import org.o42a.core.ref.type.TypeRef;


public final class AncestorTypeRef {

	public static AncestorTypeRef ancestorTypeRef(TypeRef ancestor) {
		if (ancestor == null) {
			return null;
		}
		return new AncestorTypeRef(ancestor);
	}

	public static AncestorTypeRef impliedAncestorTypeRef() {
		return new AncestorTypeRef(null);
	}

	private final TypeRef ancestor;

	private AncestorTypeRef(TypeRef ancestor) {
		this.ancestor = ancestor;
	}

	public final boolean isImplied() {
		return this.ancestor == null;
	}

	public final TypeRef getAncestor() {
		return this.ancestor;
	}

	@Override
	public String toString() {
		return this.ancestor != null ? this.ancestor.toString() : "*";
	}

}
