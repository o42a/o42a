/*
    Abstract Syntax Tree
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.clause.ReusedClauseNode;
import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarableNodeVisitor;
import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.ast.file.SectionTypeDefinitionNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;
import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.ref.TypeRefNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.type.AscendantNode;
import org.o42a.ast.type.TypeArgNode;
import org.o42a.ast.type.TypeArgumentNodeVisitor;


public abstract class NodeVisitor<R, P>
		extends AbstractStatementVisitor<R, P>
		implements TypeArgumentNodeVisitor<R, P>,
				AtomNodeVisitor<R, P>,
				DeclarableNodeVisitor<R, P>,
				ClauseIdNodeVisitor<R, P>,
				PhrasePartNodeVisitor<R, P> {

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
	public R visitString(StringNode string, P p) {
		return visitAtom(string, p);
	}

	public R visitDigits(DigitsNode digits, P p) {
		return visitPart(digits, p);
	}

	public R visitFractionalPart(FractionalPartNode fractional, P p) {
		return visitPart(fractional, p);
	}

	public R visitExponent(ExponentNode exponent, P p) {
		return visitPart(exponent, p);
	}

	public R visitTypeRef(TypeRefNode<?> typeRef, P p) {
		return visitPart(typeRef, p);
	}

	public R visitArgument(ArgumentNode argument, P p) {
		return visitPart(argument, p);
	}

	@Override
	public R visitInterval(IntervalNode interval, P p) {
		return visitPart(interval, p);
	}

	@Override
	public R visitTypeDefinition(TypeDefinitionNode definition, P p) {
		return visitPart(definition, p);
	}

	public R visitSample(AscendantNode sample, P p) {
		return visitPart(sample, p);
	}

	public R visitTypeArg(TypeArgNode argument, P p) {
		return visitPart(argument, p);
	}

	@Override
	public R visitDeclarableAdapter(DeclarableAdapterNode adapter, P p) {
		return visitDeclarable(adapter, p);
	}

	public R visitLocal(LocalNode local, P p) {
		return visitPart(local, p);
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

	public R visitSectionTypeDefinition(
			SectionTypeDefinitionNode definition,
			P p) {
		return visitPart(definition, p);
	}

	public R visitSection(SectionNode section, P p) {
		return visitPart(section, p);
	}

	public R visitFile(FileNode file, P p) {
		return visitAny(file, p);
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
		return visitAny(declarable, p);
	}

	protected abstract R visitAny(Node any, P p);

}
