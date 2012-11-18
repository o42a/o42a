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

import static org.o42a.compiler.ip.type.def.TypeParameterNameVisitor.typeParameterNameVisitor;

import org.o42a.ast.field.DeclaratorNode;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;


final class TypeParameterDeclaration extends Location {

	private final TypeDefinition typeDefinition;
	private final DeclaratorNode node;
	private final TypeRef definition;
	private MemberKey key;

	TypeParameterDeclaration(
			TypeDefinition typeDefinition,
			DeclaratorNode node,
			TypeRef definition) {
		super(typeDefinition.getContext(), node);
		this.typeDefinition = typeDefinition;
		this.node = node;
		this.definition = definition;
	}

	public final MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = this.node.getDeclarable().accept(
				typeParameterNameVisitor(isOverride()),
				this.typeDefinition.distribute());
	}

	public final TypeRef getDefinition() {
		return this.definition;
	}

	public TypeParameterDeclaration prefixWith(PrefixPath prefix) {
		return new TypeParameterDeclaration(
				this.typeDefinition,
				this.node,
				this.definition.prefixWith(prefix));
	}

	public TypeParameterDeclaration reproduce(Reproducer reproducer) {

		final MemberKey key = getKey();

		if (!key.isValid()) {
			return null;
		}
		if (!isOverride()) {
			reproducer.getLogger().notReproducible(this);
			return null;
		}

		final TypeRef definition = getDefinition().reproduce(reproducer);

		if (definition == null) {
			return null;
		}

		final TypeParameterDeclaration reproduction =
				new TypeParameterDeclaration(
						this.typeDefinition,
						this.node,
						definition);

		reproduction.key = key;

		return reproduction;
	}

	@Override
	public String toString() {
		if (this.definition == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		toString(out);

		return out.toString();
	}

	public void toString(StringBuilder out) {
		this.node.printContent(out);
	}

	private boolean isOverride() {
		return this.node.getTarget().isOverride();
	}

}
