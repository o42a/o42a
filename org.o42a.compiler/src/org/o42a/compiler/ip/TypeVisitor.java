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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.ref.Ref.falseRef;

import org.o42a.ast.Node;
import org.o42a.ast.expression.AscendantNode;
import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.ref.AbstractTypeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class TypeVisitor
		extends AbstractTypeVisitor<TypeRef, Distributor> {

	private final Interpreter ip;

	TypeVisitor(Interpreter ip) {
		this.ip = ip;
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

		final AscendantNode first = ascendantNodes[0];

		if (first.getSeparator() == null) {
			return super.visitAscendants(ascendants, p);
		}

		final RefNode ancestor = first.getAscendant();
		final Ref ref = ancestor.accept(ip().refVisitor(), p);

		if (ref != null) {
			return ref.toStaticTypeRef();
		}

		return falseRef(location(p, ancestor), p).toTypeRef();
	}

	@Override
	protected TypeRef visitRef(RefNode node, Distributor p) {

		final Ref ref = node.accept(ip().refVisitor(), p);

		if (ref != null) {
			return ref.toTypeRef();
		}

		return falseRef(location(p, node), p).toTypeRef();
	}

	@Override
	protected TypeRef visitType(Node type, Distributor p) {
		p.getContext().getLogger().invalidType(type);
		return null;
	}

}
