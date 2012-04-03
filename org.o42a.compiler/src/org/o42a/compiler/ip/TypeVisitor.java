/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;

import org.o42a.ast.Node;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


final class TypeVisitor extends AbstractTypeVisitor<TypeRef, Distributor> {

	private final Interpreter ip;
	private final ValueStructFinder valueStructFinder;

	TypeVisitor(Interpreter ip, ValueStructFinder valueStructFinder) {
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
				this.valueStructFinder,
				BODY_REFERRAL);

		if (ancestor.isImplied()) {
			return super.visitAscendants(ascendants, p);
		}

		return ancestor.getAncestor();
	}

	@Override
	public TypeRef visitValueType(ValueTypeNode valueType, Distributor p) {

		final TypeNode ascendantNode = valueType.getAscendant();

		if (ascendantNode == null) {
			return super.visitValueType(valueType, p);
		}

		final ValueStructFinder vsFinder;
		final InterfaceNode ifaceNode = valueType.getValueType();

		if (this.valueStructFinder != null) {
			p.getLogger().error(
					"redundant_value_type",
					ifaceNode,
					"Redundant value type");
			vsFinder = this.valueStructFinder;
		} else {
			vsFinder = ip().typeParameters(ifaceNode, p);
			if (vsFinder == null) {
				return null;
			}
		}

		return ascendantNode.accept(new TypeVisitor(ip(), vsFinder), p);
	}

	@Override
	protected TypeRef visitRef(RefNode node, Distributor p) {

		final Ref ref = node.accept(ip().bodyRefVisitor(), p);

		if (ref == null) {
			return null;
		}

		return ref.toTypeRef(this.valueStructFinder);
	}

	@Override
	protected TypeRef visitType(Node type, Distributor p) {
		p.getContext().getLogger().invalidType(type);
		return null;
	}

}
