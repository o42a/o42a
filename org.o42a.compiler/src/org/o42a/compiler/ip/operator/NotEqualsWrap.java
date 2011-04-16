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
package org.o42a.compiler.ip.operator;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.binaryPhrase;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;


public class NotEqualsWrap extends Wrap {

	private final BinaryNode node;

	public NotEqualsWrap(BinaryNode node, Distributor distributor) {
		super(location(distributor, node), distributor);
		this.node = node;
	}

	@Override
	protected Ref resolveWrapped() {

		final Distributor distributor = distribute();
		final BinaryPhrasePart binary = binaryPhrase(this.node, distributor);

		if (binary == null) {
			return null;
		}

		final ClauseId clauseId = binary.getClauseId();

		if (clauseId == ClauseId.EQUALS) {
			return new NotEqualsRef(this.node, distributor);
		}
		if (clauseId == ClauseId.COMPARE) {
			return new CompareNotEqualRef(this.node, distributor);
		}

		return null;
	}

}
