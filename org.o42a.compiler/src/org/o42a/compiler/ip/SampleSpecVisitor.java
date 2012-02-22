/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public class SampleSpecVisitor
		extends AbstractAscendantSpecVisitor<StaticTypeRef, Distributor> {

	private final Interpreter ip;

	public static AscendantsDefinition parseAscendants(
			Interpreter ip,
			AscendantsNode node,
			Distributor distributor) {

		final SampleSpecVisitor sampleSpecVisitor = new SampleSpecVisitor(ip);
		AscendantsDefinition ascendants = new AscendantsDefinition(
				location(distributor, node),
				distributor);
		final AscendantNode[] ascendantNodes = node.getAscendants();
		final AncestorTypeRef ancestor = parseAncestor(ip, node, distributor);

		if (!ancestor.isImplied()) {
			ascendants = ascendants.setAncestor(ancestor.getAncestor());
		}

		if (ascendantNodes.length <= 1) {
			return ascendants;
		}

		for (int i = 1; i < ascendantNodes.length; ++i) {

			final AscendantSpecNode specNode = ascendantNodes[i].getSpec();

			if (specNode != null) {

				final StaticTypeRef sample =
						specNode.accept(sampleSpecVisitor, distributor);

				if (sample != null) {
					ascendants = ascendants.addSample(sample);
				}
			}
		}

		return ascendants;
	}

	public SampleSpecVisitor(Interpreter ip) {
		this.ip = ip;
	}

	@Override
	protected StaticTypeRef visitRef(RefNode ref, Distributor p) {

		final Ref sampleRef = ref.accept(this.ip.refVisitor(), p);

		if (sampleRef == null) {
			return null;
		}

		return sampleRef.toStaticTypeRef();
	}

	@Override
	protected StaticTypeRef visitAscendantSpec(
			AscendantSpecNode spec,
			Distributor p) {
		return null;
	}

}
