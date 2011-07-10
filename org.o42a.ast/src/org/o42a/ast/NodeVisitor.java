/*
    Abstract Syntax Tree
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
package org.o42a.ast;

import org.o42a.ast.atom.*;
import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.expression.AscendantNode;
import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.*;


public abstract class NodeVisitor<R, P>
		extends AbstractStatementVisitor<R, P>
		implements AtomNodeVisitor<R, P>,
		ClauseKeyNodeVisitor<R, P> {

	@Override
	public <S extends SignType> R visitSign(SignNode<S> sign, P p) {
		return visitAtom(sign, p);
	}

	@Override
	public R visitComment(CommentNode comment, P p) {
		return visitAtom(comment, p);
	}

	@Override
	public R visitName(NameNode name, P p) {
		return visitAtom(name, p);
	}

	@Override
	public R visitStringLiteral(StringNode string, P p) {
		return visitAtom(string, p);
	}

	public R visitArgument(ArgumentNode argument, P p) {
		return visitPart(argument, p);
	}

	public R visitSample(AscendantNode sample, P p) {
		return visitPart(sample, p);
	}

	@Override
	public R visitDeclarableAdapter(DeclarableAdapterNode adapter, P p) {
		return visitDeclarable(adapter, p);
	}

	public R visitDeclarationCast(DefinitionCastNode cast, P p) {
		return visitPart(cast, p);
	}

	public R visitSerial(SerialNode statement, P p) {
		return visitPart(statement, p);
	}

	public R visitAlternative(AlternativeNode alternative, P p) {
		return visitPart(alternative, p);
	}

	public R visitSentence(SentenceNode sentence, P p) {
		return visitPart(sentence, p);
	}

	public R visitReusedClause(ReusedClauseNode reusedClause, P p) {
		return visitPart(reusedClause, p);
	}

	public R visitSection(SectionNode section, P p) {
		return visitAny(section, p);
	}

	public R visitModule(ModuleNode module, P p) {
		return visitPart(module, p);
	}

	public R visitEmpty(EmptyNode node, P p) {
		return visitAny(node, p);
	}

	protected R visitAtom(AtomNode atom, P p) {
		return visitAny(atom, p);
	}

	@Override
	protected R visitStatement(StatementNode statement, P p) {
		return visitAny(statement, p);
	}

	protected R visitPart(Node item, P p) {
		return visitAny(item, p);
	}

	protected R visitDeclarable(DeclarableNode declarable, P p) {
		return visitClauseKey(declarable, p);
	}

	protected R visitClauseKey(ClauseKeyNode clauseKey, P p) {
		return visitAny(clauseKey, p);
	}

	protected abstract R visitAny(Node any, P p);

}
