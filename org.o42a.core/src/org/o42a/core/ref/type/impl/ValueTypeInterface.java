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
package org.o42a.core.ref.type.impl;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.ObjectType;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParameters;


public class ValueTypeInterface extends BoundFragment {

	private static final TemplateValueType TEMPLATE_VALUE_TYPE =
			new TemplateValueType();

	public static TypeRef valueTypeInterfaceOf(Ref ref) {
		return ref.getPath()
				.append(new ValueTypeInterface())
				.typeRef(
						ref.distribute(),
						new ValueTypeInterfaceParameters(ref.typeParameters()));
	}

	private ValueTypeInterface() {
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final ObjectType type = start.toObject().type();

		if (type.getAncestor().getPath().getPath().getTemplate() != null) {
			return TEMPLATE_VALUE_TYPE.toPath();
		}

		return valueTypeRef(expander, start);
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
		return "/";
	}

	private static Path valueTypeRef(PathExpander expander, Scope start) {

		final TypeParameters<?> typeParameters =
				start.toObject().type().getParameters();
		final BoundPath path = expander.getPath();
		final StaticTypeRef valueTypeRef =
				typeParameters.getValueType().typeRef(
						path,
						start,
						typeParameters);

		return valueTypeRef.getPath().getPath();
	}

	private static final class TemplateValueType extends PathTemplate {

		@Override
		public Path expand(PathExpander expander, int index, Scope start) {
			return valueTypeRef(expander, start);
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
			return "/#";
		}

	}

}
