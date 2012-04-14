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

import static org.o42a.compiler.ip.UnwrapVisitor.UNWRAP_VISITOR;
import static org.o42a.compiler.ip.ref.RefInterpreter.*;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.core.value.TypeParameters.typeMutability;
import static org.o42a.core.value.ValueType.INTEGER;

import org.o42a.ast.Node;
import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeNodeVisitor;
import org.o42a.compiler.ip.member.DefinitionVisitor;
import org.o42a.compiler.ip.ref.RefInterpreter;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.core.Distributor;
import org.o42a.core.ScopeInfo;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStructFinder;


public enum Interpreter {

	PLAIN_IP(PLAIN_REF_IP),
	PATH_COMPILER_IP(PATH_COMPILER_REF_IP),
	CLAUSE_DEF_IP(CLAUSE_DEF_REF_IP),
	CLAUSE_DECL_IP(CLAUSE_DECL_REF_IP);

	public static Ref integer(DecimalNode decimal, Distributor distributor) {

		final long value;

		try {
			value = Long.parseLong(decimal.getNumber());
		} catch (NumberFormatException e) {
			distributor.getContext().getLogger().notInteger(
					decimal,
					decimal.getNumber());
			return integer(distributor, 0L, decimal);
		}

		return integer(distributor, value, decimal);
	}

	private final RefInterpreter refInterpreter;
	private final ExpressionNodeVisitor<Ref, Distributor> targetExVisitor;
	private final ExpressionVisitor bodyExVisitor;
	private final ExpressionNodeVisitor<
			FieldDefinition,
			FieldDeclaration> definitionVisitor;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> ancestorVisitor;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> staticAncestorVisitor;
	private final TypeNodeVisitor<TypeRef, Distributor> typeVisitor;

	Interpreter(RefInterpreter refInterpreter) {
		this.refInterpreter = refInterpreter;
		this.targetExVisitor = new ExpressionVisitor(this, TARGET_REFERRAL);
		this.bodyExVisitor = new ExpressionVisitor(this, BODY_REFERRAL);
		this.definitionVisitor = new DefinitionVisitor(this);
		this.ancestorVisitor = new AncestorVisitor(this, null, TARGET_REFERRAL);
		this.staticAncestorVisitor =
				new StaticAncestorVisitor(this, null, TARGET_REFERRAL);
		this.typeVisitor = new TypeVisitor(this, null);
	}

	public final RefInterpreter refIp() {
		return this.refInterpreter;
	}

	public final RefNodeVisitor<Ref, Distributor> targetRefVisitor() {
		return refIp().targetRefVisitor();
	}

	public final RefNodeVisitor<Ref, Distributor> bodyRefVisitor() {
		return refIp().bodyRefVisitor();
	}

	public final ExpressionNodeVisitor<Ref, Distributor> targetExVisitor() {
		return this.targetExVisitor;
	}

	public final ExpressionNodeVisitor<Ref, Distributor> bodyExVisitor() {
		return this.bodyExVisitor;
	}

	public final ExpressionNodeVisitor<
			FieldDefinition,
			FieldDeclaration> definitionVisitor() {
		return this.definitionVisitor;
	}

	public final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> ancestorVisitor(
					ValueStructFinder valueStructFinder,
					Referral referral) {
		if (valueStructFinder == null && referral == TARGET_REFERRAL) {
			return this.ancestorVisitor;
		}
		return new AncestorVisitor(this, valueStructFinder, referral);
	}

	public final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> staticAncestorVisitor(
					ValueStructFinder valueStructFinder,
					Referral referral) {
		if (valueStructFinder == null && referral == TARGET_REFERRAL) {
			return this.staticAncestorVisitor;
		}
		return new StaticAncestorVisitor(this, valueStructFinder, referral);
	}

	public final TypeNodeVisitor<TypeRef, Distributor> typeVisitor() {
		return this.typeVisitor;
	}

	public static LinkValueType definitionLinkType(
			DefinitionKind definitionKind) {
		switch (definitionKind) {
		case LINK:
			return LinkValueType.LINK;
		case VARIABLE:
			return LinkValueType.VARIABLE;
		case GETTER:
			return LinkValueType.GETTER;
		}
		throw new IllegalArgumentException(
				"Unknwon definition kind: " + definitionKind);
	}

	public final TypeParameters typeParameters(
			InterfaceNode ifaceNode,
			Distributor p) {

		final TypeRef paramTypeRef =
				ifaceNode.getType().accept(typeVisitor(), p);

		if (paramTypeRef == null) {
			return null;
		}

		final TypeParameters.Mutability mutability = typeMutability(
				location(p, ifaceNode),
				p,
				definitionLinkType(ifaceNode.getKind().getType()));

		return mutability.setTypeRef(paramTypeRef);
	}

	public static BlockBuilder contentBuilder(
			StatementVisitor statementVisitor,
			BlockNode<?> node) {
		return new ContentBuilder(statementVisitor, node);
	}

	public static void addContent(
			StatementVisitor statementVisitor,
			Block<?, ?> block,
			BlockNode<?> blockNode) {
		for (SentenceNode sentence : blockNode.getContent()) {
			addSentence(statementVisitor, block, sentence, sentence.getType());
		}
	}

	public static Sentence<?, ?> addSentence(
			StatementVisitor statementVisitor,
			Block<?, ?> block,
			SentenceNode node,
			SentenceType type) {

		final Location location =
				new Location(statementVisitor.getContext(), node);
		final Sentence<?, ?> sentence;

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

		if (sentence != null) {
			fillSentence(statementVisitor, sentence, node);
		}

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

	private static final Ref integer(Distributor p, long value, Node node) {

		final Location location = location(p, node);

		return INTEGER.constantRef(location, p, value);
	}

	private static void fillSentence(
			final StatementVisitor statementVisitor,
			final Sentence<?, ?> sentence,
			final SentenceNode node) {

		int i = 0;
		final AlternativeNode[] disjunction = node.getDisjunction();

		while (i < disjunction.length) {

			final int next = i + 1;
			final AlternativeNode altNode = disjunction[i];
			final Location location =
					new Location(statementVisitor.getContext(), altNode);
			final Statements<?, ?> alt;

			if (next < disjunction.length) {

				final AlternativeNode nextAltNode = disjunction[next];

				if (nextAltNode.isOpposite()) {
					alt = sentence.inhibit(location);
				} else {
					alt = sentence.alternative(location);
				}
			} else {
				alt = sentence.alternative(location);
			}
			if (alt != null) {
				fillStatements(statementVisitor, altNode, alt);
			}
			i = next;
		}
	}

	private static void fillStatements(
			final StatementVisitor statementVisitor,
			final AlternativeNode altNode,
			final Statements<?, ?> alt) {
		for (SerialNode stat : altNode.getConjunction()) {

			final StatementNode st = stat.getStatement();

			if (st != null) {
				st.accept(statementVisitor, alt);
			}
		}
	}

}
