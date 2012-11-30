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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.ClauseIdVisitor.extractName;
import static org.o42a.compiler.ip.member.ClauseIdVisitor.extractRow;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;

import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.AbstractPhrasePartVisitor;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseId;


final class PhraseClauseIdVisitor
		extends AbstractPhrasePartVisitor<ClauseDeclaration, Distributor> {

	private final PhraseNode phrase;

	PhraseClauseIdVisitor(PhraseNode phrase) {
		this.phrase = phrase;
	}

	@Override
	public ClauseDeclaration visitBraces(BracesNode braces, Distributor p) {
		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), braces),
				ClauseId.IMPERATIVE);
	}

	@Override
	public ClauseDeclaration visitBrackets(
			BracketsNode brackets,
			Distributor p) {

		final BracketsNode row = extractRow(brackets);

		if (row != null) {
			return clauseDeclaration(
					location(p, this.phrase),
					p,
					extractName(p.getContext(), row),
					ClauseId.ROW);
		}

		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), brackets),
				ClauseId.ARGUMENT);
	}

	@Override
	public ClauseDeclaration visitText(TextNode text, Distributor p) {
		if (text.isDoubleQuoted()) {
			return super.visitText(text, p);
		}
		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), text, text.getText()),
				ClauseId.STRING);
	}

	@Override
	protected ClauseDeclaration visitPhrasePart(
			PhrasePartNode part,
			Distributor p) {
		p.getLogger().invalidDeclaration(part);
		return null;
	}

}
