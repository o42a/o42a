/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
import static org.o42a.compiler.ip.clause.IntervalInterpreter.invalidIntervalBracket;
import static org.o42a.compiler.ip.phrase.ArgumentVisitor.ARGUMENT_VISITOR;
import static org.o42a.compiler.ip.phrase.PhrasePartVisitor.PHRASE_PART_VISITOR;
import static org.o42a.compiler.ip.st.StInterpreter.contentBuilder;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.type.StaticRefNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.common.phrase.Phrase;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.common.phrase.part.UnaryPhraseOperator;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.st.DefaultStatementVisitor;
import org.o42a.compiler.ip.st.assignment.AssignmentStatement;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.core.Contained;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ObjectTypeParameters;


public final class PhraseBuilder extends Contained {

	private final Interpreter ip;
	private final Phrase phrase;
	private final AccessRules accessRules;
	private final TypeConsumer typeConsumer;

	public PhraseBuilder(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {
		super(location, distributor);
		this.ip = ip;
		this.accessRules = distributor.getAccessRules();
		this.phrase = new Phrase(location, distributor);
		if (typeConsumer != EXPRESSION_TYPE_CONSUMER) {
			this.typeConsumer = typeConsumer;
		} else {
			this.typeConsumer = TypeConsumer.typeConsumer(
					new StandalonePhraseNesting(this.phrase));
		}
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final AccessRules getAccessRules() {
		return this.accessRules;
	}

	public final Phrase phrase() {
		return this.phrase;
	}

	public final TypeConsumer typeConsumer() {
		return this.typeConsumer;
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

	public PhraseBuilder prefixByStaticRef(StaticRefNode node) {

		final AccessDistributor distributor = distributeAccess();
		final AncestorTypeRef ancestor =
				ip().typeIp().parseAncestor(node, distributor);

		if (ancestor.isImplied()) {
			setImpliedAncestor(location(this, node));
		} else {
			ancestor.applyTo(phrase());
		}

		return this;
	}

	public PhraseBuilder prefixByTypeArguments(TypeArgumentsNode node) {

		final TypeRefParameters typeArguments = ip().typeIp().typeArguments(
				node,
				distributeAccess().fromDeclaration(),
				typeConsumer());
		final ExpressionNode ascendantNode = node.getType();

		if (ascendantNode == null) {
			return setImpliedAncestor(location(this, node))
					.setTypeParameters(typeArguments.toObjectTypeParameters());
		}

		return ascendantNode.accept(
				new PhrasePrefixVisitor(typeArguments),
				this);
	}

	public PhraseBuilder expressionPhrase(
			ExpressionNode expression,
			TypeRefParameters typeParameters) {

		final AncestorTypeRef ancestor =
				expression.accept(
						ip().typeIp().ancestorVisitor(
								typeParameters,
								typeConsumer()),
						distributeAccess());

		if (ancestor == null || ancestor.isImplied()) {
			setImpliedAncestor(location(this, expression));

			if (typeParameters == null) {
				return this;
			}

			return setTypeParameters(typeParameters.toObjectTypeParameters());
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
				getAccessRules().contentRules(),
				new DefaultStatementVisitor(ip(), getContext()),
				node));
		return this;
	}

	public final PhraseBuilder imperative(BracesNode node) {
		phrase().imperative(contentBuilder(
				getAccessRules().contentRules(),
				new DefaultStatementVisitor(ip(), getContext()),
				node));
		return this;
	}

	public final PhraseBuilder emptyArgument(LocationInfo location) {
		phrase().emptyArgument(location);
		return this;
	}

	public final PhraseBuilder argument(RefBuilder value) {
		phrase().argument(value);
		return this;
	}

	public final PhraseBuilder emptyInitializer(LocationInfo location) {
		phrase().emptyInitializer(location);
		return this;
	}

	public final PhraseBuilder initializer(RefBuilder value) {
		phrase().initializer(value);
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

			if (arg.isInitializer()) {
				if (value != null) {
					phrase.initializer(
							value.accept(
									ip().refBuildVisitor(),
									distributeAccess()));
					continue;
				}
				phrase = phrase.emptyInitializer(location(phrase, arg));
				continue;
			}
			if (value != null) {
				phrase = value.accept(ARGUMENT_VISITOR, phrase);
				continue;
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

		final Ref value = text.accept(
				ip().expressionVisitor(),
				distributeAccess());

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
				distributeAccess(),
				false);

		phrase().array(array.toRef());

		return this;
	}

	public PhraseBuilder interval(IntervalNode interval) {

		final BoundNode leftBoundNode = interval.getLeftBound();
		final LocationInfo leftLocation;
		final RefBuilder leftBound;
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
					ip().refBuildVisitor(),
					distributeAccess());
			if (leftBound != null) {
				leftLocation = leftBound;
			} else {
				leftLocation = location(this, leftBoundNode);
			}
		}

		final BoundNode rightBoundNode = interval.getRightBound();
		final LocationInfo rightLocation;
		final RefBuilder rightBound;
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
					ip().refBuildVisitor(),
					distributeAccess());
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
				ip().expressionVisitor(typeConsumer().noConsumption()),
				distributeAccess());

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

		final AccessDistributor distributor = distributeAccess();
		final Ref left = node.getLeftOperand().accept(
				ip().expressionVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		setAncestor(left.toTypeRef());

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final RefBuilder right =
				rightOperand.accept(ip().refBuildVisitor(), distributor);

		if (right == null) {
			return null;
		}

		phrase().binary(
				location(this, node.getSign()),
				operator,
				right);

		return this;
	}

	public PhraseBuilder binary(Ref destination, AssignmentNode node) {

		final BinaryPhraseOperator operator = binaryPhraseOperator(node);

		if (operator == null) {
			return null;
		}

		final AccessDistributor distributor = distributeAccess();

		setAncestor(destination.toTypeRef());

		final ExpressionNode rightOperand = node.getValue();

		if (rightOperand == null) {
			return null;
		}

		final RefBuilder right =
				rightOperand.accept(ip().refBuildVisitor(), distributor);

		if (right == null) {
			return null;
		}

		phrase().binary(
				location(this, node.getOperator()),
				operator,
				right);

		return this;
	}

	public PhraseBuilder suffix(BinaryNode node) {

		final RefBuilder prefix = node.getLeftOperand().accept(
				ip().refBuildVisitor(),
				distributeAccess());

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

	public final AccessDistributor distributeAccess() {
		return getAccessRules().distribute(distribute());
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
		case LINK:
		case VARIABLE:
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

	private BinaryPhraseOperator binaryPhraseOperator(AssignmentNode node) {
		switch (node.getOperator().getType()) {
		case ADD_AND_ASSIGN:
			return BinaryPhraseOperator.ADD;
		case SUBTRACT_AND_ASSIGN:
			return BinaryPhraseOperator.SUBTRACT;
		case MULTIPLY_AND_ASSIGN:
			return BinaryPhraseOperator.MULTIPLY;
		case DIVIDE_AND_ASSIGN:
			return BinaryPhraseOperator.DIVIDE;
		case ASSIGN:
		case BIND:
		}

		getLogger().error(
				"unsupported_combined_assignment",
				node.getOperator(),
				"Combined assignment operator '%s' is not supported",
				node.getOperator().getType().getSign());

		return null;
	}

	private static final class StandalonePhraseNesting implements Nesting {

		private final Phrase phrase;

		StandalonePhraseNesting(Phrase phrase) {
			this.phrase = phrase;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.phrase.toRef()
					.resolve(enclosing.resolver())
					.toObject();
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
