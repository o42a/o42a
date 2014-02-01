/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundFragment;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.link.Link;


public class AncestorFragment extends BoundFragment {

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Obj object = start.toObject();

		assert object != null :
			"Only object may have an ancestor: " + start;

		final Link dereferencedLink = object.getDereferencedLink();

		if (dereferencedLink != null) {
			return start.getEnclosingScopePath().append(
					dereferencedLink.getInterfaceRef().getPath().getPath());
		}

		final Intrinsics intrinsics = start.getContext().getIntrinsics();
		final Obj valueTypeObject =
				object.type().getValueType().typeObject(intrinsics);

		if (valueTypeObject.getScope().is(start)) {
			// Ancestor of value type object is a reference to this object.
			return SELF_PATH;
		}

		final TypeRef ancestor = object.type().getAncestor();
		final Path ancestorPath = ancestor.getPath().getPath();

		if (ancestor.isStatic()) {
			return ancestorPath;
		}

		return start.getEnclosingScopePath().append(ancestorPath);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return defaultInterface(ref);
	}

	@Override
	public String toString() {
		return "^^";
	}

}
