/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;


public final class TypeParameter implements ScopeInfo {

	private final MemberKey key;
	private final TypeRef typeRef;
	private final Obj origin;

	TypeParameter(MemberKey key, TypeRef typeRef, Obj origin) {
		assert typeRef != null :
			"Type parameter not specified";
		assert key != null :
			"Type parameter key not specified";
		this.key = key;
		this.typeRef = typeRef;
		this.origin = origin;
		if (toString().equals("<[hello world: each] (Location[hello_world.o42a]:hello_world.o42a:12,11(390)..12,12(391)[])///Indexed: item: S/(//Indexed: item type)///Indexed: (2): S/collections: list @@(//indexed): S/(collections: collection: element type)/collections: list: (2): S/hello world: each: a: S/->> (`)")) {
			System.err.println("(!)");
		}
	}

	@Override
	public final Location getLocation() {
		return getTypeRef().getLocation();
	}

	@Override
	public final Scope getScope() {
		return getTypeRef().getScope();
	}

	public final MemberKey getKey() {
		return this.key;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final Obj getOrigin() {
		return this.origin;
	}

	public final boolean isValid() {
		return getTypeRef().isValid();
	}

	public final boolean validateAll() {
		return getTypeRef().validateAll();
	}

	public final TypeParameter prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new TypeParameter(getKey(), newTypeRef, getOrigin());
	}

	public TypeParameter rebuildIn(Scope scope) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.rebuildIn(scope);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new TypeParameter(getKey(), newTypeRef, getOrigin());
	}

	public final TypeParameter reproduce(Reproducer reproducer) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TypeParameter(getKey(), typeRef, null);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return this.typeRef.toString();
	}

	final TypeParameter suggestOrigin(Obj origin) {
		if (this.origin != null) {
			return this;
		}
		return new TypeParameter(getKey(), getTypeRef(), origin);
	}

}
