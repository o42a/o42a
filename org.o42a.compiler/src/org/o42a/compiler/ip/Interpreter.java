/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.ref.RefInterpreter.*;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.field.DefinitionVisitor;
import org.o42a.compiler.ip.phrase.PhraseInterpreter;
import org.o42a.compiler.ip.ref.ExpressionVisitor;
import org.o42a.compiler.ip.ref.RefInterpreter;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.TypeInterpreter;
import org.o42a.core.ScopeInfo;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;
import org.o42a.util.log.LogInfo;


public enum Interpreter {

	PLAIN_IP(PLAIN_REF_IP),
	PATH_COMPILER_IP(PATH_COMPILER_REF_IP),
	CLAUSE_DEF_IP(CLAUSE_DEF_REF_IP),
	CLAUSE_DECL_IP(CLAUSE_DECL_REF_IP);

	public static <S extends SignType> S signType(SignNode<S> node) {
		return node != null ? node.getType() : null;
	}

	private final RefInterpreter refIp;
	private final TypeInterpreter typeIp = new TypeInterpreter(this);
	private final PhraseInterpreter phraseIp = new PhraseInterpreter(this);
	private final ExpressionNodeVisitor<Ref, AccessDistributor> targetExVisitor;
	private final ExpressionVisitor bodyExVisitor;

	Interpreter(RefInterpreter refInterpreter) {
		this.refIp = refInterpreter;
		this.targetExVisitor = new ExpressionVisitor(this, TARGET_REFERRAL);
		this.bodyExVisitor = new ExpressionVisitor(this, BODY_REFERRAL);
	}

	public final RefInterpreter refIp() {
		return this.refIp;
	}

	public final TypeInterpreter typeIp() {
		return this.typeIp;
	}

	public final PhraseInterpreter phraseIp() {
		return this.phraseIp;
	}

	public final RefNodeVisitor<Ref, AccessDistributor> targetRefVisitor() {
		return refIp().targetRefVisitor();
	}

	public final RefNodeVisitor<Ref, AccessDistributor> bodyRefVisitor() {
		return refIp().bodyRefVisitor();
	}

	public final
	ExpressionNodeVisitor<Ref, AccessDistributor> targetExVisitor() {
		return this.targetExVisitor;
	}

	public final ExpressionNodeVisitor<Ref, AccessDistributor> targetExVisitor(
			TypeConsumer typeConsumer) {
		if (typeConsumer == NO_TYPE_CONSUMER) {
			return targetExVisitor();
		}
		return new ExpressionVisitor(this, TARGET_REFERRAL, typeConsumer);
	}

	public final ExpressionNodeVisitor<Ref, AccessDistributor> bodyExVisitor() {
		return this.bodyExVisitor;
	}

	public final ExpressionNodeVisitor<Ref, AccessDistributor> bodyExVisitor(
			TypeConsumer typeConsumer) {
		if (typeConsumer == NO_TYPE_CONSUMER) {
			return bodyExVisitor();
		}
		return new ExpressionVisitor(this, BODY_REFERRAL, typeConsumer);
	}

	public final ExpressionNodeVisitor<
			FieldDefinition,
			FieldDeclaration> definitionVisitor(TypeConsumer typeConsumer) {
		return new DefinitionVisitor(this, typeConsumer);
	}

	public static Location location(ScopeInfo p, LogInfo node) {
		return new Location(p.getLocation().getContext(), node);
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

		final StatementNode statement = conjunction[0].getStatement();

		if (statement == null) {
			return null;
		}

		return statement.toExpression();
	}

}
