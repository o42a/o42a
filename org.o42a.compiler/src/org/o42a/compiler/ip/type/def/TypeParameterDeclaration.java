/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.compiler.ip.type.def.TypeParameterKeyVisitor.typeParameterKeyVisitor;

import org.o42a.ast.field.DeclaratorNode;
import org.o42a.common.macro.type.TypeParameterKey;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Located;
import org.o42a.core.value.TypeParameters;


final class TypeParameterDeclaration
		extends Located
		implements TypeParameterKey {

	private final TypeDefinitionBuilder builder;
	private final DeclaratorNode node;
	private final TypeRef definition;
	private MemberKey key;

	TypeParameterDeclaration(
			TypeDefinitionBuilder builder,
			DeclaratorNode node) {
		super(builder.getContext(), node);
		this.builder = builder;
		this.node = node;

		final TypeConsumer consumer =
				builder.getConsumer().paramConsumer(this);

		this.definition = node.getDefinition().accept(
				new TypeParameterDefinitionVisitor(consumer),
				builder.distributeAccess());
	}

	private TypeParameterDeclaration(
			TypeParameterDeclaration location,
			TypeRef definition) {
		super(location);
		this.builder = null;
		this.node = location.node;
		this.definition = definition;
		this.key = location.getKey();
	}

	public final MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = this.node.getDeclarable().accept(
				typeParameterKeyVisitor(isOverride()),
				this.builder);
	}

	public final TypeRef getDefinition() {
		return this.definition;
	}

	public final TypeParameterDeclaration prefixWith(PrefixPath prefix) {
		return new TypeParameterDeclaration(
				this,
				this.definition.prefixWith(prefix));
	}

	@Override
	public MemberKey typeParameterKey(TypeParameters<?> parameters) {
		return getKey();
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
