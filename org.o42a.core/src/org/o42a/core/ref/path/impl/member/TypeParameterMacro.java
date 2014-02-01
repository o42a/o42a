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
package org.o42a.core.ref.path.impl.member;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroExpander;


final class TypeParameterMacro implements Macro {

	private final TypeParameterObject object;
	private Ref owner;

	TypeParameterMacro(TypeParameterObject object) {
		this.object = object;

		final Path ownerPath =
				this.object.getScope().getEnclosingScopePath();

		this.owner =
				ownerPath.bind(this.object, this.object.getScope())
				.target(object.distribute());
	}

	public final TypeParameterObject getObject() {
		return this.object;
	}

	@Override
	public Path expand(MacroExpander expander) {
		return typeParameter(expander);
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return typeParameter(expander);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return this.object.toString();
	}

	private Path typeParameter(MacroExpander expander) {

		final Ref ownerRef = owner();
		final Obj owner =
				ownerRef
				.resolve(expander.getMacroObject().getScope().resolver())
				.toObject();
		final TypeRef typeRef =
				owner.type()
				.getParameters()
				.typeRef(this.object.getParameterKey());

		return ownerRef.getPath().getPath().append(
				typeRef.getPath().getPath());
	}

	private Ref owner() {
		if (this.owner != null) {
			return this.owner;
		}
		return this.owner = OwnerRefDep.ownerRef(this);
	}

}
