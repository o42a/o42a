/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.ast.expression.*;
import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Lambda;


final class ArraySpecVisitor
		extends AbstractAscendantSpecVisitor<
				Lambda<ValueStruct<?, ?>, Ref>,
				Interpreter> {

	private static final ArraySpecVisitor ARRAY_SPEC_VISITOR =
			new ArraySpecVisitor();

	public static Lambda<ValueStruct<?, ?>, Ref> arrayStructFinder(
			Interpreter ip,
			AscendantsNode ascendantsNode,
			CompilerLogger logger) {

		final AscendantNode[] ascendantNodes = ascendantsNode.getAscendants();
		Lambda<ValueStruct<?, ?>, Ref> arrayStructFinder = null;

		for (int i = 1; i < ascendantNodes.length; ++i) {

			final AscendantSpecNode specNode = ascendantNodes[i].getSpec();

			if (specNode == null) {
				continue;
			}

			final Lambda<ValueStruct<?, ?>, Ref> finder =
					specNode.accept(ARRAY_SPEC_VISITOR, ip);

			if (finder == null) {
				continue;
			}
			if (arrayStructFinder != null) {
				logger.error(
						"redundant_array_spec",
						specNode,
						"At most one array specifier allowed"
						+ " within ascendants expression");
				continue;
			}

			arrayStructFinder = finder;
		}

		return arrayStructFinder;
	}

	private ArraySpecVisitor() {
	}

	@Override
	public Lambda<ValueStruct<?, ?>, Ref> visitArrayType(
			ArrayTypeNode arrayType,
			Interpreter p) {
		return p.arrayValueStruct(arrayType);
	}

	@Override
	protected Lambda<ValueStruct<?, ?>, Ref> visitAscendantSpec(
			AscendantSpecNode spec,
			Interpreter p) {
		return null;
	}

}
