/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.core.*;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMemberRegistry;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class CompareOperatorRef extends ObjectConstructor {

	private static final MemberId RESULT = memberName("_result");

	private final BinaryNode node;
	private final Ref leftOperand;
	private final Ref rightOperand;

	public CompareOperatorRef(
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

	public CompareOperatorRef(
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

	protected CompareOperatorRef(
			CompareOperatorRef prototype,
			Reproducer reproducer,
			Ref leftOperand,
			Ref rightOperand) {
		super(prototype, reproducer.distribute());
		this.node = prototype.node;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public final BinaryNode getNode() {
		return this.node;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public final Ref reproduce(Reproducer reproducer) {

		final Ref leftOperand = this.leftOperand.reproduce(reproducer);

		if (leftOperand == null) {
			return null;
		}

		final Ref rightOperand = this.rightOperand.reproduce(reproducer);

		if (rightOperand == null) {
			return null;
		}

		return reproduce(reproducer, leftOperand, rightOperand);
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
	protected Obj createObject() {
		return new CompareResult(this);
	}

	protected abstract BinaryOperatorRef createOperator(
			Distributor distributor,
			Ref leftOperand,
			Ref rightOperand);

	protected abstract boolean result(Value<?> value);

	protected abstract CompareOperatorRef reproduce(
			Reproducer reproducer,
			final Ref leftOperand,
			final Ref rightOperand);

	private final class CompareResult extends Result {

		private final CompareOperatorRef compare;
		private MemberKey resultKey;

		CompareResult(CompareOperatorRef compare) {
			super(compare, compare.distribute(), ValueType.VOID);
			this.compare = compare;
		}

		@Override
		protected void declareMembers(ObjectMembers members) {

			final ObjectMemberRegistry memberRegistry =
				new ObjectMemberRegistry(this);

			final Distributor distributor = distribute();
			final FieldBuilder builder = memberRegistry.newField(
					fieldDeclaration(this, distributor, RESULT)
					.setVisibility(Visibility.PRIVATE),
					createOperator(
							distributor,
							this.compare.leftOperand.rescope(getScope()),
							this.compare.rightOperand.rescope(getScope()))
					.toFieldDefinition());

			if (builder == null) {
				return;
			}

			final DeclarationStatement statement = builder.build();

			if (statement == null) {
				return;
			}

			statement.setEnv(defaultEnv(this));

			this.resultKey = statement.toMember().getKey();

			memberRegistry.registerMembers(members);
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {

			final Obj object = scope.getContainer().toObject();

			object.getMembers();// declare fields to initialize resultKey

			final Field<?> field = object.member(this.resultKey).toField();
			final Value<?> value = field.getArtifact().toObject().getValue();

			if (!value.isDefinite()) {
				// Value could not be determined at compile-time.
				// Result will be determined at run time.
				return ValueType.VOID.runtimeValue();
			}

			final boolean result = this.compare.result(value);

			return result ? voidValue() : falseValue();
		}

		@Override
		public String toString() {
			if (this.compare == null) {
				return "CompareResult";
			}
			return this.compare.toString();
		}

	}

}
