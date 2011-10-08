/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorSpecVisitor.parseAncestor;

import org.o42a.ast.Node;
import org.o42a.ast.expression.AscendantNode;
import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.field.AbstractTypeVisitor;
import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.ast.field.TypeNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Lambda;


final class TypeVisitor
		extends AbstractTypeVisitor<TypeRef, Distributor> {

	private final Interpreter ip;
	private final Lambda<ValueStruct<?, ?>, Ref> valueStructFinder;

	TypeVisitor(
			Interpreter ip,
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder) {
		this.ip = ip;
		this.valueStructFinder = valueStructFinder;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef visitAscendants(AscendantsNode ascendants, Distributor p) {

		final AscendantNode[] ascendantNodes = ascendants.getAscendants();

		if (ascendantNodes.length != 1) {
			return super.visitAscendants(ascendants, p);
		}

		final AncestorTypeRef ancestor = parseAncestor(
				ip(),
				p,
				ascendantNodes[0],
				this.valueStructFinder);

		if (ancestor.isImplied()) {
			return super.visitAscendants(ascendants, p);
		}

		return ancestor.getAncestor();
	}

	@Override
	protected TypeRef visitRef(RefNode node, Distributor p) {

		final Ref ref = node.accept(ip().refVisitor(), p);

		if (ref == null) {
			return null;
		}

		return ref.toTypeRef(this.valueStructFinder);
	}

	@Override
	public TypeRef visitArrayType(ArrayTypeNode arrayType, Distributor p) {

		final TypeNode ancestorNode = arrayType.getAncestor();

		if (ancestorNode == null) {
			return null;
		}

		final Lambda<ValueStruct<?, ?>, Ref> valueStructFinder =
				ip().arrayValueStruct(arrayType);
		final TypeVisitor typeVisitor =
				new TypeVisitor(ip(), valueStructFinder);

		return ancestorNode.accept(typeVisitor, p);
	}

	@Override
	protected TypeRef visitType(Node type, Distributor p) {
		p.getContext().getLogger().invalidType(type);
		return null;
	}

}
