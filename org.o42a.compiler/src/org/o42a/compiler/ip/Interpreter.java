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

import static org.o42a.compiler.ip.UnwrapVisitor.UNWRAP_VISITOR;
import static org.o42a.core.member.MemberId.clauseName;
import static org.o42a.core.member.MemberId.fieldName;

import org.o42a.ast.Node;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.ref.TypeNode;
import org.o42a.ast.ref.TypeNodeVisitor;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.member.DefinitionVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;


public enum Interpreter {

	PLAIN_IP(new RefVisitor()) {

		@Override
		public MemberId memberName(String name) {
			return fieldName(name);
		}

	},

	CLAUSE_DEF_IP(new ClauseRefVisitor(true)) {

		@Override
		public MemberId memberName(String name) {
			return fieldName(name);
		}

	},

	CLAUSE_DECL_IP(new ClauseRefVisitor(false)) {

		@Override
		public MemberId memberName(String name) {
			return clauseName(name);
		}

	};

	private final RefNodeVisitor<Ref, Distributor> refVisitor;
	private final ExpressionNodeVisitor<Ref, Distributor> expressionVisitor;
	private final
	ExpressionNodeVisitor<FieldDefinition, FieldDeclaration> definitionVisitor;
	private final ExpressionNodeVisitor<TypeRef, Distributor> ancestorVisitor;
	private final
	ExpressionNodeVisitor<TypeRef, Distributor> staticAnncestorVisitor;
	private final TypeNodeVisitor<TypeRef, Distributor> typeVisitor;

	Interpreter(RefVisitor refVisitor) {
		this.refVisitor = refVisitor;
		refVisitor.init(this);
		this.expressionVisitor = new ExpressionVisitor(this);
		this.definitionVisitor = new DefinitionVisitor(this);
		this.ancestorVisitor = new AncestorVisitor(this);
		this.staticAnncestorVisitor = new StaticAncestorVisitor(this);
		this.typeVisitor = new TypeVisitor(this);
	}

	public final RefNodeVisitor<Ref, Distributor> refVisitor() {
		return this.refVisitor;
	}

	public final ExpressionNodeVisitor<Ref, Distributor> expressionVisitor() {
		return this.expressionVisitor;
	}

	public final ExpressionNodeVisitor<FieldDefinition, FieldDeclaration>
	definitionVisitor() {
		return this.definitionVisitor;
	}

	public final ExpressionNodeVisitor<TypeRef, Distributor> ancestorVisitor() {
		return this.ancestorVisitor;
	}

	public final ExpressionNodeVisitor<TypeRef, Distributor>
	staticAnncestorVisitor() {
		return this.staticAnncestorVisitor;
	}

	public final TypeNodeVisitor<TypeRef, Distributor> typeVisitor() {
		return this.typeVisitor;
	}

	public static BlockBuilder contentBuilder(
			StatementVisitor statementVisitor,
			BlockNode<?> node) {
		return new ContentBuilder(statementVisitor, node);
	}

	public abstract MemberId memberName(String name);

	public ArrayInitializer arrayInitializer(
			CompilerContext context,
			ArrayNode node,
			FieldDeclaration declaration) {

		final TypeRef itemType;
		final TypeNode itemTypeNode = node.getItemType();

		if (itemTypeNode == null) {
			itemType = null;
		} else {
			itemType =
				itemTypeNode.accept(typeVisitor(), declaration.distribute());
		}

		return arrayInitializer(
				context,
				node,
				itemType,
				node.getItems(),
				declaration);
	}

	public ArrayInitializer arrayInitializer(
			CompilerContext context,
			BracketsNode node,
			FieldDeclaration declaration) {
		return arrayInitializer(context, node, null, node, declaration);
	}

	public static void addContent(
			StatementVisitor statementVisitor,
			Block<?> block,
			BlockNode<?> blockNode) {
		for (SentenceNode sentence : blockNode.getContent()) {
			addSentence(statementVisitor, block, sentence, sentence.getType());
		}
	}

	public static Sentence<?> addSentence(
			StatementVisitor statementVisitor,
			Block<?> block,
			SentenceNode node,
			SentenceType type) {

		final Location location =
			new Location(statementVisitor.getContext(), node);
		final Sentence<?> sentence;

		switch (type) {
		case PROPOSITION:
			sentence = block.propose(location);
			break;
		case CLAIM:
			sentence = block.claim(location);
			break;
		case ISSUE:
			sentence = block.issue(location);
			break;
		default:
			block.getLogger().invalidExpression(node);
			return null;
		}

		fillSentence(statementVisitor, sentence, node);

		return sentence;
	}

	public static Location location(ScopeInfo p, Node node) {
		return new Location(p.getContext(), node);
	}

	public static ExpressionNode unwrap(BlockNode<?> block) {

		final SentenceNode[] content = block.getContent();

		if (content.length != 1) {
			return null;
		}

		final SentenceNode sentence = content[0];

		if (sentence.getMark() != null) {
			return null;
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length != 1) {
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			return null;
		}

		return conjunction[0].getStatement().accept(UNWRAP_VISITOR, null);
	}

	private ArrayInitializer arrayInitializer(
			CompilerContext context,
			Node node,
			TypeRef itemType,
			BracketsNode brackets,
			FieldDeclaration declaration) {

		final Distributor distributor = declaration.distribute();
		boolean ok = true;
		final ArgumentNode[] arguments = brackets.getArguments();
		final Ref[] items = new Ref[arguments.length];

		for (int i = 0; i < arguments.length; ++i) {

			final ExpressionNode itemNode = arguments[i].getValue();

			if (itemNode == null) {
				ok = false;
				continue;
			}

			final Ref item = itemNode.accept(expressionVisitor(), distributor);

			if (item == null) {
				ok = false;
				continue;
			}

			items[i] = item;
		}

		if (!ok) {
			return null;
		}

		return ArrayInitializer.arrayInitializer(
				context,
				node,
				declaration.distribute(),
				itemType,
				items);
	}

	private static void fillSentence(
			final StatementVisitor statementVisitor,
			final Sentence<?> sentence,
			final SentenceNode node) {
		for (AlternativeNode alt : node.getDisjunction()) {

			final Statements<?> alternative = sentence.alternative(
					new Location(statementVisitor.getContext(), alt),
					alt.isOpposite());

			for (SerialNode stat : alt.getConjunction()) {

				final StatementNode st = stat.getStatement();

				if (st != null) {
					st.accept(statementVisitor, alternative);
				}
			}
		}
	}

	private static final class ContentBuilder extends BlockBuilder {

		private final StatementVisitor statementVisitor;
		private final BlockNode<?> block;

		ContentBuilder(StatementVisitor statementVisitor, BlockNode<?> block) {
			super(statementVisitor.getContext(), block);
			this.statementVisitor = statementVisitor;
			this.block = block;
		}

		@Override
		public void buildBlock(Block<?> block) {
			addContent(this.statementVisitor, block, this.block);
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			this.block.printContent(out);

			return out.toString();
		}

	}

}
