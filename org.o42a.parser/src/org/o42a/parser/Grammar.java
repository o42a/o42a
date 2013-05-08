/*
    Parser
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.parser;

import org.o42a.ast.atom.*;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.InclusionNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.*;
import org.o42a.ast.type.*;
import org.o42a.parser.grammar.atom.*;
import org.o42a.parser.grammar.clause.ClauseDeclaratorParser;
import org.o42a.parser.grammar.expression.*;
import org.o42a.parser.grammar.field.DeclarableAdapterParser;
import org.o42a.parser.grammar.field.DeclaratorParser;
import org.o42a.parser.grammar.field.InitializerParser;
import org.o42a.parser.grammar.file.FileParser;
import org.o42a.parser.grammar.file.InclusionParser;
import org.o42a.parser.grammar.phrase.IntervalParser;
import org.o42a.parser.grammar.phrase.PhraseParser;
import org.o42a.parser.grammar.ref.*;
import org.o42a.parser.grammar.sentence.*;
import org.o42a.parser.grammar.statement.*;
import org.o42a.parser.grammar.type.AscendantsParser;
import org.o42a.parser.grammar.type.TypeArgumentsParser;
import org.o42a.parser.grammar.type.TypeParser;


public class Grammar {

	public static final Grammar DECLARATIVE = new DeclarativeGrammar();
	public static final Grammar IMPERATIVE = new ImperativeGrammar();

	public static Parser<FileNode> file() {
		return FileParser.FILE;
	}

	public static Parser<Object> whitespace(boolean allowNewLine) {
		if (!allowNewLine) {
			return WhitespaceParser.WHITESPACE;
		}
		return WhitespaceNlParser.WHITESPACE_NL;
	}

	public static Parser<CommentNode> comment(boolean allowNewLine) {
		if (!allowNewLine) {
			return InlineCommentParser.INLINE_COMMENT;
		}
		return NewLineCommentParser.NL_COMMENT;
	}

	public static Parser<SeparatorNodes> separator(boolean allowNewLine) {
		if (!allowNewLine) {
			return SeparatorParser.SEPARATOR;
		}
		return SeparatorParser.SEPARATOR_NL;
	}

	public static Parser<NameNode> name() {
		return NameParser.NAME;
	}

	public static Parser<StringNode> string() {
		return StringParser.STRING;
	}

	public static Parser<NumberNode> number() {
		return NumberParser.NUMBER;
	}

	public static Parser<ScopeRefNode> scopeRef() {
		return ScopeRefParser.SCOPE_REF;
	}

	public static Parser<ParentRefNode> parentRef() {
		return ParentRefParser.PARENT_REF;
	}

	public static Parser<MemberRefNode> memberRef(
			ExpressionNode owner,
			boolean qualifierExpected) {
		return new MemberRefParser(owner, qualifierExpected);
	}

	public static Parser<AdapterRefNode> adapterRef(ExpressionNode owner) {
		return new AdapterRefParser(owner);
	}

	public static Parser<DerefNode> deref(ExpressionNode owner) {
		return new DerefParser(owner);
	}

	public static Parser<RefNode> ref() {
		return RefParser.REF;
	}

	public static Parser<AscendantsNode> samples() {
		return AscendantsParser.SAMPLES;
	}

	public static Parser<AscendantsNode> ascendants(RefNode ancestor) {
		return new AscendantsParser(ancestor);
	}

	public static Parser<DeclarableAdapterNode> declarableAdapter() {
		return DeclarableAdapterParser.DECLARABLE_ADAPTER;
	}

	public static Parser<BracesNode> braces() {
		return BracesParser.BRACES;
	}

	public static Parser<LocalNode> local() {
		return LocalParser.LOCAL;
	}

	public static Parser<LocalNode> local(ExpressionNode expression) {
		return new LocalParser(expression);
	}

	public static Parser<AssignmentNode> assignment(
			AssignableNode destination) {
		return new AssignmentParser(destination);
	}

	public static Parser<NamedBlockNode> namedBlock(NameNode name) {
		return new NamedBlockParser(name);
	}

	public static Parser<EllipsisNode> ellipsis() {
		return EllipsisParser.ELLIPSIS;
	}

	public static Parser<TextNode> text() {
		return TextParser.TEXT;
	}

	public static Parser<InclusionNode> inclusion() {
		return InclusionParser.INCLUSION;
	}

	public static final Parser<ExpressionNode> expression() {
		return ExpressionParser.EXPRESSION;
	}

	public static final Parser<ExpressionNode> expression(ExpressionNode base) {
		return new ExpressionParser(base);
	}

	public static final Parser<ExpressionNode> simpleExpression() {
		return SimpleExpressionParser.SIMPLE_EXPRESSION;
	}

	public static final Parser<ExpressionNode> simpleExpression(
			ExpressionNode base) {
		return new SimpleExpressionParser(base);
	}

	public static final Parser<PhraseNode> phrase(ExpressionNode prefix) {
		return new PhraseParser(prefix);
	}

	public static final Parser<IntervalNode> interval() {
		return IntervalParser.INTERVAL;
	}

	public static final Parser<UnaryNode> unary() {
		return UnaryParser.UNARY;
	}

	public static final Parser<TypeNode> type() {
		return TypeParser.TYPE;
	}

	public static final Parser<TypeArgumentsNode> typeArguments(
			TypeArgumentNode argument) {
		return new TypeArgumentsParser(argument);
	}

	public static final Parser<BinaryNode> binary(ExpressionNode leftOperand) {
		return new BinaryParser(leftOperand);
	}

	public static final Parser<BracketsNode> brackets() {
		return BracketsParser.BRACKETS;
	}

	public static final Parser<SelfAssignmentNode> selfAssignment() {
		return SelfAssignmentParser.SELF_ASSIGNMENT;
	}

	public static final Parser<DeclaratorNode> declarator(
			DeclarableNode declarable) {
		return new DeclaratorParser(declarable);
	}

	public static final Parser<ExpressionNode> initializer() {
		return InitializerParser.INITIALIZER;
	}

	private final Parser<StatementNode> statement;
	private final Parser<StatementNode> localStatement;
	private final Parser<LocalScopeNode> localScope;
	private final Parser<ClauseDeclaratorNode> clauseDeclarator;
	private final Parser<ParenthesesNode> parentheses;
	private final Parser<SentenceNode[]> content;
	private final Parser<SentenceNode> sentence;
	private final Parser<AlternativeNode[]> disjunction;
	private final Parser<SerialNode[]> conjunction;

	private Grammar() {
		this.clauseDeclarator = new ClauseDeclaratorParser(this);
		this.statement = new StatementParser(this, false);
		this.localStatement = new StatementParser(this, true);
		this.localScope = new LocalScopeParser(this);
		this.parentheses = new ParenthesesParser(this);
		this.content = new ContentParser(this);
		this.sentence = new SentenceParser(this);
		this.disjunction = new DisjunctionParser(this);
		this.conjunction = new ConjunctionParser(this);
	}

	public final boolean isDeclarative() {
		return this == DECLARATIVE;
	}

	public final boolean isImperative() {
		return this == IMPERATIVE;
	}

	public final Parser<StatementNode> statement() {
		return this.statement;
	}

	public final Parser<StatementNode> localStatement() {
		return this.localStatement;
	}

	public final Parser<LocalScopeNode> localScope() {
		return this.localScope;
	}

	public final Parser<LocalScopeNode> localScope(ExpressionNode expression) {
		return new LocalScopeParser(this, expression);
	}

	public final Parser<LocalScopeNode> localScope(LocalNode local) {
		return new LocalScopeParser(this, local);
	}

	public final Parser<ClauseDeclaratorNode> clauseDeclarator() {
		return this.clauseDeclarator;
	}

	public final Parser<ParenthesesNode> parentheses() {
		return this.parentheses;
	}

	public final Parser<SentenceNode[]> content() {
		return this.content;
	}

	public final Parser<SentenceNode> sentence() {
		return this.sentence;
	}

	public final Parser<AlternativeNode[]> disjunction() {
		return this.disjunction;
	}

	public final Parser<SerialNode[]> conjunction() {
		return this.conjunction;
	}

	private static final class DeclarativeGrammar extends Grammar {

		@Override
		public String toString() {
			return "DECLARATIVE";
		}

	}

	private static final class ImperativeGrammar extends Grammar {

		@Override
		public String toString() {
			return "IMPERATIVE";
		}

	}

}
