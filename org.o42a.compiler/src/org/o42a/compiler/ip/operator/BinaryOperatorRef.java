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
package org.o42a.compiler.ip.operator;

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.*;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;


public class BinaryOperatorRef extends Wrap {

	public static Ref binaryOp(
			CompilerContext context,
			BinaryNode node,
			Distributor distributor) {
		switch (node.getOperator()) {
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			return new BinaryOperatorRef(context, node, distributor);
		case LESS:
			return new ComparisonRef.LessOp(context, node, distributor);
		case LESS_OR_EQUAL:
			return new ComparisonRef.LessOrEqual(context, node, distributor);
		case GREATER:
			return new ComparisonRef.Greater(context, node, distributor);
		case GREATER_OR_EQUAL:
			return new ComparisonRef.GreaterOrEqual(context, node, distributor);
		case EQUAL:
		case NOT_EQUAL:
			return new EqualRef(context, node, distributor);
		}

		throw new IllegalArgumentException(
				"Unsupported binary operator: "
				+ node.getOperator().getSign());
	}

	private final BinaryNode node;
	private final Ref leftOperand;
	private final Ref rightOperand;

	BinaryOperatorRef(
			CompilerContext context,
			BinaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.leftOperand = node.getLeftOperand().accept(
				EXPRESSION_VISITOR,
				distributor);
		this.rightOperand = node.getRightOperand().accept(
				EXPRESSION_VISITOR,
				distributor);
	}

	BinaryOperatorRef(
			LocationInfo location,
			Distributor distributor,
			BinaryNode node,
			Ref leftOperand,
			Ref rightOperand) {
		super(location, distributor);
		this.node = node;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public final BinaryNode getNode() {
		return this.node;
	}

	@Override
	public String toString() {
		if (this.rightOperand == null) {
			return super.toString();
		}

		return this.leftOperand
		+ this.node.getOperator().getSign()
		+ this.rightOperand;
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution leftResolution = this.leftOperand.getResolution();

		if (leftResolution.isError()) {
			return errorRef(leftResolution);
		}

		final Obj left = leftResolution.materialize();
		final Location operatorLocation =
			new Location(getContext(), this.node.getSign());
		final BinaryOperatorInfo info = getInfo();
		final AdapterId adapterId =
			info.getPath().toAdapterId(operatorLocation, distribute());
		final Member adapter = left.member(adapterId);

		if (adapter == null) {
			getLogger().error(
					"unsupported_binary_operator",
					operatorLocation,
					"Binary operator '%s' is not supported, "
					+ "because left operand doesn't have an '%s' adapter",
					this.node.getOperator().getSign(),
					adapterId);
			return errorRef(operatorLocation);
		}

		return new BinaryRef(
				this,
				info,
				this.node.getOperator().getSign(),
				adapter.getKey());
	}

	protected BinaryOperatorInfo getInfo() {
		return BinaryOperatorInfo.bySign(this.node.getOperator().getSign());
	}

	private static final class BinaryRef extends ObjectConstructor {

		private final Ref leftOperand;
		private final Ref rightOperand;
		private final BinaryOperatorInfo info;
		private final String sign;
		private final MemberKey adapterKey;

		BinaryRef(
				BinaryOperatorRef operator,
				BinaryOperatorInfo info,
				String sign,
				MemberKey adapterKey) {
			super(operator, operator.distribute());
			this.info = info;
			this.leftOperand = operator.leftOperand;
			this.rightOperand = operator.rightOperand;
			this.sign = sign;
			this.adapterKey = adapterKey;
		}

		BinaryRef(
				BinaryRef reproducing,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(reproducing, reproducer.distribute());
			this.leftOperand = leftOperand;
			this.rightOperand = rightOperand;
			this.info = reproducing.info;
			this.sign = reproducing.sign;
			this.adapterKey = reproducing.adapterKey;
		}

		@Override
		public TypeRef ancestor(LocationInfo location) {

			final Ref adapterRef = this.adapterKey.toPath().target(
					location,
					distribute(),
					this.leftOperand.materialize());

			return adapterRef.toTypeRef();
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {

			final Ref leftOperand = this.leftOperand.reproduce(reproducer);

			if (leftOperand == null) {
				return null;
			}

			final Ref rightOperand = this.rightOperand.reproduce(reproducer);

			if (rightOperand == null) {
				return null;
			}

			return new BinaryRef(this, reproducer, leftOperand, rightOperand);
		}

		@Override
		public String toString() {
			if (this.sign == null) {
				return super.toString();
			}
			return this.leftOperand + this.sign + this.rightOperand;
		}

		@Override
		protected Obj createObject() {
			return new BinaryResult(this);
		}

	}

	private static final class BinaryResult extends Obj {

		private final BinaryRef operator;

		public BinaryResult(BinaryRef operator) {
			super(operator, operator.distribute());
			this.operator = operator;
		}

		@Override
		public String toString() {
			if (this.operator == null) {
				return super.toString();
			}
			return this.operator.toString();
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).setAncestor(
					this.operator.ancestor(this));
		}

		@Override
		protected void declareMembers(ObjectMembers members) {

			final ObjectMemberRegistry registry =
				new ObjectMemberRegistry(this);
			final Distributor distributor = distribute();
			final FieldDeclaration declaration =
				fieldDeclaration(
						this,
						distributor,
						this.operator.info.getRightOperand().memberId(
								getContext()))
				.override();
			final FieldBuilder builder = registry.newField(
					declaration,
					this.operator.rightOperand.rescope(getScope())
					.toFieldDefinition());

			if (builder == null) {
				return;
			}

			final DeclarationStatement statement = builder.build();

			if (statement == null) {
				return;
			}

			statement.setEnv(defaultEnv(this));

			registry.registerMembers(members);
		}

		@Override
		protected Definitions overrideDefinitions(
				Scope scope,
				Definitions ascendantDefinitions) {
			return ascendantDefinitions;
		}

	}

}
