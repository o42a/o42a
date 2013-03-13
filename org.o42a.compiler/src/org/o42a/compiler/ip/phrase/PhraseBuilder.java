/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.IntervalInterpreter.invalidIntervalBracket;
import static org.o42a.compiler.ip.phrase.ArgumentVisitor.ARGUMENT_VISITOR;
import static org.o42a.compiler.ip.phrase.PhrasePartVisitor.PHRASE_PART_VISITOR;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.st.StInterpreter.contentBuilder;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.common.phrase.Phrase;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.common.phrase.part.UnaryPhraseOperator;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.st.DefaultStatementVisitor;
import org.o42a.compiler.ip.st.assignment.AssignmentStatement;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.compiler.ip.type.ascendant.SampleSpecVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ObjectTypeParameters;


public final class PhraseBuilder extends Placed {

	private final Interpreter ip;
	private final Phrase phrase;
	private final TypeConsumer typeConsumer;

	public PhraseBuilder(
			Interpreter ip,
			Phrase phrase,
			TypeConsumer typeConsumer) {
		super(phrase, phrase.distribute());
		this.ip = ip;
		this.phrase = phrase;
		if (typeConsumer != EXPRESSION_TYPE_CONSUMER) {
			this.typeConsumer = typeConsumer;
		} else {
			this.typeConsumer = TypeConsumer.typeConsumer(
					new StandalonePhraseNesting(this.phrase));
		}
	}

	public PhraseBuilder(
			Interpreter ip,
			LocationInfo location,
			Distributor distributor,
			TypeConsumer typeConsumer) {
		this(ip, new Phrase(location, distributor), typeConsumer);
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final Phrase phrase() {
		return this.phrase;
	}

	public final TypeConsumer typeConsumer() {
		return this.typeConsumer;
	}

	public final PhraseBuilder referBody() {
		phrase().referBody();
		return this;
	}

	public final PhraseBuilder expandMacro() {
		phrase().expandMacro();
		return this;
	}

	public final PhraseBuilder setAncestor(TypeRef ancestor) {
		phrase().setAncestor(ancestor);
		return this;
	}

	public final PhraseBuilder addSamples(StaticTypeRef... samples) {
		phrase().addSamples(samples);
		return this;
	}

	public final PhraseBuilder setImpliedAncestor(LocationInfo location) {
		phrase().setImpliedAncestor(location);
		return this;
	}

	public final PhraseBuilder setTypeParameters(
			ObjectTypeParameters typeParameters) {
		phrase().setTypeParameters(typeParameters);
		return this;
	}

	public PhraseBuilder prefixByAscendants(AscendantsNode node) {

		final Distributor distributor = distribute();
		final AncestorTypeRef ancestor =
				ip().typeIp().parseAncestor(node, distributor);

		if (ancestor.isImplied()) {
			setImpliedAncestor(location(this, node.getAncestor()));
		} else {
			if (ancestor.isBodyReferred()) {
				referBody();
			}
			ancestor.applyTo(phrase());
		}

		if (!node.hasSamples()) {
			return this;
		}

		final SampleSpecVisitor sampleSpecVisitor =
				new SampleSpecVisitor(ip());

		for (AscendantNode sampleNode : node.getSamples()) {

			final RefNode specNode = sampleNode.getSpec();

			if (specNode != null) {

				final StaticTypeRef sample =
						specNode.accept(sampleSpecVisitor, distributor);

				if (sample != null) {
					addSamples(sample);
				}
			}
		}

		return this;
	}

	public PhraseBuilder prefixByTypeParameters(TypeParametersNode node) {
		referBody();

		final TypeRefParameters typeParams =
				ip().typeIp().typeParameters(
						node.getParameters(),
						distribute(),
						typeConsumer());
		final TypeNode ascendantNode = node.getType();

		if (ascendantNode == null) {
			return setImpliedAncestor(location(this, node))
					.setTypeParameters(typeParams.toObjectTypeParameters());
		}

		return ascendantNode.accept(
				new PhrasePrefixVisitor(typeParams),
				this);
	}

	public PhraseBuilder expressionPhrase(
			ExpressionNode expression,
			TypeRefParameters typeParameters) {

		final AncestorTypeRef ancestor =
				expression.accept(
						ip().typeIp().ancestorVisitor(
								typeParameters,
								typeParameters == null
								? TARGET_REFERRAL : BODY_REFERRAL,
								typeConsumer()),
						distribute());

		if (ancestor == null || ancestor.isImplied()) {
			setImpliedAncestor(location(this, expression));

			if (typeParameters == null) {
				return this;
			}

			return setTypeParameters(typeParameters.toObjectTypeParameters());
		}

		if (typeParameters != null || ancestor.isBodyReferred()) {
			referBody();
		}

		ancestor.applyTo(phrase());
		if (ancestor.isMacroExpanding()) {
			return expandMacro();
		}

		return this;
	}

	public PhraseBuilder addParts(PhraseNode node) {

		PhraseBuilder result = this;

		for (PhrasePartNode part : node.getParts()) {
			result = part.accept(PHRASE_PART_VISITOR, result);
		}

		return result;
	}

	public final PhraseBuilder name(NameNode name) {
		phrase().name(location(this, name), name.getName());
		return this;
	}

	public final PhraseBuilder noDeclarations() {
		phrase().declarations(emptyBlock(this));
		return this;
	}

	public final PhraseBuilder declarations(ParenthesesNode node) {
		phrase().declarations(contentBuilder(
				new DefaultStatementVisitor(ip(), getContext()),
				node));
		return this;
	}

	public final PhraseBuilder imperative(BracesNode node) {
		phrase().imperative(contentBuilder(
				new DefaultStatementVisitor(ip(), getContext()),
				node));
		return this;
	}

	public final PhraseBuilder emptyArgument(LocationInfo location) {
		phrase().emptyArgument(location);
		return this;
	}

	public final PhraseBuilder argument(Ref value) {
		phrase().argument(value);
		return this;
	}

	public final PhraseBuilder arguments(BracketsNode node) {

		final ArgumentNode[] arguments = node.getArguments();

		if (arguments.length == 0) {
			return emptyArgument(location(this, node));
		}

		PhraseBuilder phrase = this;

		for (ArgumentNode arg : arguments) {

			final ExpressionNode value = arg.getValue();

			if (value != null) {
				phrase = value.accept(ARGUMENT_VISITOR, phrase);
				continue;
			}
			if (arguments.length == 1) {
				return phrase.emptyArgument(location(phrase, node));
			}
			phrase = phrase.emptyArgument(location(phrase, arg));
		}

		return phrase;
	}

	public PhraseBuilder string(TextNode text) {
		if (!text.isDoubleQuoted()) {
			phrase().string(location(this, text), text.getText());
			return this;
		}

		final Ref value = text.accept(ip().bodyExVisitor(), distribute());

		if (value != null) {
			return argument(value);
		}

		return emptyArgument(location(this, text));
	}

	public PhraseBuilder array(BracketsNode brackets) {

		final ArrayConstructor array = new ArrayConstructor(
				ip(),
				getContext(),
				brackets,
				distribute());

		phrase().array(array.toRef());

		return this;
	}

	public PhraseBuilder interval(IntervalNode interval) {

		final BoundNode leftBoundNode = interval.getLeftBound();
		final LocationInfo leftLocation;
		final Ref leftBound;
		final boolean leftOpen;

		if (!interval.isLeftBounded()) {
			leftBound = null;
			leftOpen = true;
			if (!interval.isLeftOpen()) {
				invalidIntervalBracket(
						getLogger(),
						interval.getLeftBracket());
			}
			if (leftBoundNode != null) {
				leftLocation = location(this, leftBoundNode);
			} else {
				leftLocation =
						location(this, interval.getEllipsis().getStart());
			}
		} else {
			leftOpen = interval.isLeftOpen();
			leftBound = leftBoundNode.toExpression().accept(
					ip().targetExVisitor(),
					distribute());
			if (leftBound != null) {
				leftLocation = leftBound;
			} else {
				leftLocation = location(this, leftBoundNode);
			}
		}

		final BoundNode rightBoundNode = interval.getRightBound();
		final LocationInfo rightLocation;
		final Ref rightBound;
		final boolean rightOpen;

		if (!interval.isRightBounded()) {
			rightBound = null;
			rightOpen = true;
			if (!interval.isRightOpen()) {
				invalidIntervalBracket(
						getLogger(),
						interval.getRightBracket());
			}
			if (rightBoundNode != null) {
				rightLocation = location(this, rightBoundNode);
			} else {
				rightLocation = location(this, interval.getEllipsis().getEnd());
			}
		} else {
			rightOpen = interval.isRightOpen();
			rightBound = rightBoundNode.toExpression().accept(
					ip().targetExVisitor(),
					distribute());
			if (rightBound != null) {
				rightLocation = rightBound;
			} else {
				rightLocation = location(this, rightBoundNode);
			}
		}

		if (!interval.isLeftBounded()) {
			if (!interval.isRightBounded()) {
				phrase().unboundedInterval(location(this, interval));
				return this;
			}
			phrase().halfBoundedInterval(
					location(this, interval),
					rightBound,
					rightOpen,
					false);
			return this;
		} else if (!interval.isRightBounded()) {
			phrase().halfBoundedInterval(
					location(this, interval),
					leftBound,
					leftOpen,
					true);
			return this;
		}

		phrase().interval(
				leftLocation,
				leftBound,
				leftOpen,
				rightLocation,
				rightBound,
				rightOpen);

		return this;
	}

	public PhraseBuilder unary(UnaryNode node) {

		final UnaryPhraseOperator operator = unaryPhraseOperator(node);

		if (operator == null) {
			return null;
		}

		final Ref operand = node.getOperand().accept(
				ip().targetExVisitor(typeConsumer().noConsumption()),
				distribute());

		if (operand == null) {
			return null;
		}

		setAncestor(operand.toTypeRef());
		phrase().unary(location(this, node), operator);

		return this;
	}

	public PhraseBuilder binary(BinaryNode node) {

		final BinaryPhraseOperator operator = binaryPhraseOperator(node);

		if (operator == null) {
			return null;
		}

		final Distributor distributor = distribute();
		final Ref left = node.getLeftOperand().accept(
				ip().targetExVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		setAncestor(left.toTypeRef());

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right =
				rightOperand.accept(ip().targetExVisitor(), distributor);

		if (right == null) {
			return null;
		}

		phrase().binary(
				location(distributor, node.getSign()),
				operator,
				right);

		return this;
	}

	public PhraseBuilder suffix(BinaryNode node) {

		final Ref prefix = node.getLeftOperand().accept(
				ip().targetExVisitor(),
				distribute());

		if (prefix == null) {
			return null;
		}

		phrase().suffix(location(this, node.getSign()), prefix);

		return this;
	}

	public PhraseBuilder assign(AssignmentStatement statement) {
		phrase().assign(
				location(this, statement.getNode().getOperator()),
				statement.getValue());
		return this;
	}

	public final Ref toRef() {
		return phrase().toRef();
	}

	@Override
	public String toString() {
		if (this.phrase == null) {
			return super.toString();
		}
		return this.phrase.toString();
	}

	private UnaryPhraseOperator unaryPhraseOperator(UnaryNode node) {
		switch (node.getOperator()) {
		case PLUS:
			return UnaryPhraseOperator.PLUS;
		case MINUS:
			return UnaryPhraseOperator.MINUS;
		case IS_TRUE:
		case NOT:
		case VALUE_OF:
		case KEEP_VALUE:
		case MACRO_EXPANSION:
		}

		getLogger().error(
				"unsupported_unary",
				node.getSign(),
				"Unary operator '%s' is not supported",
				node.getOperator().getSign());

		return null;
	}

	private BinaryPhraseOperator binaryPhraseOperator(BinaryNode node) {
		switch (node.getOperator()) {
		case ADD:
			return BinaryPhraseOperator.ADD;
		case SUBTRACT:
			return BinaryPhraseOperator.SUBTRACT;
		case MULTIPLY:
			return BinaryPhraseOperator.MULTIPLY;
		case DIVIDE:
			return BinaryPhraseOperator.DIVIDE;
		case COMPARE:
			return BinaryPhraseOperator.COMPARE;
		case EQUAL:
			return BinaryPhraseOperator.EQUALS;
		case NOT_EQUAL:
			return BinaryPhraseOperator.NOT_EQUALS;
		case LESS:
			return BinaryPhraseOperator.LESS;
		case LESS_OR_EQUAL:
			return BinaryPhraseOperator.LESS_OR_EQUAL;
		case GREATER:
			return BinaryPhraseOperator.GREATER;
		case GREATER_OR_EQUAL:
			return BinaryPhraseOperator.GREATER_OR_EQUAL;
		case SUFFIX:
		}

		getLogger().error(
				"unsupported_binary",
				node.getSign(),
				"Binary operator '%s' is not supported",
				node.getOperator().getSign());

		return null;
	}

	private static final class StandalonePhraseNesting implements Nesting {

		private final Phrase phrase;

		StandalonePhraseNesting(Phrase phrase) {
			this.phrase = phrase;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.phrase.toRef().resolve(enclosing.resolver()).toObject();
		}

		@Override
		public String toString() {
			if (this.phrase == null) {
				return super.toString();
			}
			return this.phrase.toString();
		}

	}

}