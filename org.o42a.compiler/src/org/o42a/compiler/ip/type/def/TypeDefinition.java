/*
    Compiler
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
package org.o42a.compiler.ip.type.def;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public final class TypeDefinition
		extends Scoped
		implements ObjectTypeParameters {

	private final TypeParameterDeclaration[] parameters;

	TypeDefinition(TypeDefinitionBuilder builder) {
		super(builder, builder.getScope());
		this.parameters = builder.getParameters();
	}

	private TypeDefinition(
			TypeDefinition location,
			Scope scope,
			TypeParameterDeclaration[] parameters) {
		super(location, scope);
		this.parameters = parameters;
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return toTypeParameters(defaultParameters.getValueType())
				.refine(object, defaultParameters);
	}

	@Override
	public TypeDefinition prefixWith(PrefixPath prefix) {

		final TypeParameterDeclaration[] newParameters =
				new TypeParameterDeclaration[this.parameters.length];

		for (int i = 0; i < newParameters.length; ++i) {
			newParameters[i] = this.parameters[i].prefixWith(prefix);
		}

		return new TypeDefinition(this, prefix.getStart(), newParameters);
	}

	@Override
	public TypeDefinition reproduce(Reproducer reproducer) {

		final TypeParameterDeclaration[] newParameters =
				new TypeParameterDeclaration[this.parameters.length];

		for (int i = 0; i < newParameters.length; ++i) {

			final TypeParameterDeclaration newParameter =
					this.parameters[i].reproduce(reproducer);

			if (newParameter == null) {
				return null;
			}
			newParameters[i] = newParameter;
		}

		return new TypeDefinition(this, reproducer.getScope(), newParameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("#(");
		if (this.parameters.length != 0) {
			this.parameters[0].toString(out);
			for (int i = 1; i < this.parameters.length; ++i) {
				out.append(". ");
				this.parameters[i].toString(out);
			}
		}
		out.append(')');

		return out.toString();
	}

	private TypeParameters<?> toTypeParameters(ValueType<?> valueType) {

		TypeParameters<?> parameters = typeParameters(this, valueType);

		for (TypeParameterDeclaration decl : this.parameters) {

			final MemberKey key = decl.getKey();

			if (!key.isValid()) {
				continue;
			}
			parameters = parameters.add(key, decl.getDefinition());
		}

		return parameters;
	}

}
