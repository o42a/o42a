/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.value.link.impl;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;


final class LinkInterface extends PathFragment {

	static TypeRef linkInterfaceOf(LocationInfo location, Ref ref) {
		return new LinkInterface().create(location, ref);
	}

	private Ref origin;
	private Path expansion;

	private LinkInterface() {
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		if (this.expansion != null) {
			return this.expansion;
		}
		if (this.origin.getPath() != expander.getPath()) {
			this.origin.getResolution().resolve();
			return this.expansion;
		}

		final TypeParameters<?> typeParameters =
				start.toObject().type().getParameters();
		final LinkValueType linkType =
				typeParameters.getValueType().toLinkType();
		final TypeRef interfaceRef = linkType.interfaceRef(typeParameters);

		return this.expansion = interfaceRef.getPath().getPath();
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

	private TypeRef create(LocationInfo location, Ref ref) {

		final Ref origin = ref.getPath()
				.cut(1)
				.setLocation(location)
				.append(this)
				.target(ref.distribute());

		this.origin = origin;

		return origin.toTypeRef();
	}

}
