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

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.member.field.FieldDefinition.fieldDefinition;
import static org.o42a.core.ref.path.Path.absolutePath;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMemberRegistry;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.member.*;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LogInfo;


abstract class CompareResult extends Result {

	private static final MemberId RESULT = memberName("_result");

	private final BinaryOp binaryOp;
	private final TypeRef operator;
	private MemberKey resultKey;

	CompareResult(BinaryOp binaryOp, Distributor distributor, Ref operator) {
		super(
				new Location(
						binaryOp.getContext(),
						binaryOp.getNode().getSign()),
				distributor,
				ValueType.VOID);
		this.binaryOp = binaryOp;
		this.operator = operator.toTypeRef();
	}

	@Override
	public String toString() {
		if (this.binaryOp != null) {
			return this.binaryOp.toString();
		}
		return "CompareResult";
	}

	@Override
	protected void declareMembers(ObjectMembers members) {

		final ObjectMemberRegistry memberRegistry =
			new ObjectMemberRegistry(this);
		final DeclarativeBlock block =
			new DeclarativeBlock(this, this, memberRegistry);

		new ResultBuilder(getContext(), this).buildBlock(block);

		memberRegistry.registerMembers(members);
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Obj object = scope.getContainer().toObject();

		object.getMembers();// declare fields to initialize resultKey

		final Field<?> field =
			object.member(CompareResult.this.resultKey).toField();
		final Value<?> val = field.getArtifact().toObject().getValue();

		if (val.getLogicalValue().isFalse()) {
			return falseValue();
		}

		final Long compareRescult = ValueType.INTEGER.definiteValue(val);

		if (compareRescult == null) {
			// value not defined or could be determined at run-time only -
			// use runtime condition
			return ValueType.VOID.runtimeValue();
		}

		// value is known at compile time -
		// build result
		if (result(compareRescult)) {
			return voidValue();
		}

		return falseValue();
	}

	protected abstract boolean result(Long value);

	static final class LessResult extends CompareResult {

		LessResult(BinaryOp binaryOp, Distributor distributor, Ref operator) {
			super(binaryOp, distributor, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value < 0;
		}

	}

	static final class LessOrEqualsResult extends CompareResult {

		LessOrEqualsResult(
				BinaryOp binaryOp,
				Distributor distributor,
				Ref operator) {
			super(binaryOp, distributor, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value <= 0;
		}

	}

	static final class GreaterResult extends CompareResult {

		GreaterResult(BinaryOp binaryOp, Distributor scope, Ref operator) {
			super(binaryOp, scope, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value > 0;
		}

	}

	static final class GreaterOrEqualsResult extends CompareResult {

		GreaterOrEqualsResult(
				BinaryOp binaryOp,
				Distributor scope,
				Ref operator) {
			super(binaryOp, scope, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value >= 0;
		}

	}

	static final class EqualsCompareResult extends CompareResult {

		EqualsCompareResult(
				BinaryOp binaryOp,
				Distributor scope,
				Ref operator) {
			super(binaryOp, scope, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value == 0;
		}

	}

	static final class NotEqualsCompareResult extends CompareResult {

		NotEqualsCompareResult(
				BinaryOp binaryOp,
				Distributor scope,
				Ref operator) {
			super(binaryOp, scope, operator);
		}

		@Override
		protected boolean result(Long value) {
			return value != 0;
		}

	}

	private final class ResultBuilder extends BlockBuilder {

		public ResultBuilder(CompilerContext context, LogInfo logInfo) {
			super(context, logInfo);
		}

		@Override
		public void buildBlock(Block<?> block) {

			final Statements<?> statements =
				block.propose(this).alternative(this);
			final FieldDeclaration declarator =
				fieldDeclaration(
						this,
						block.distribute(),
						RESULT)
				.setVisibility(Visibility.PRIVATE);

			final TypeRef operator = CompareResult.this.operator;
			final FieldBuilder builder = statements.field(
					declarator,
					fieldDefinition(
							this,
							new AscendantsDefinition(
									operator,
									block.distribute()).setAncestor(
											operator.rescope(block.getScope())),
							new RightCmpOperand(
									getContext(),
									CompareResult.this.binaryOp.getNode()
									.getRightOperand())));

			if (builder == null) {
				return;
			}

			final DeclarationStatement statement = builder.build();

			if (statement == null) {
				return;
			}

			statements.statement(statement);
			CompareResult.this.resultKey = statement.toMember().getKey();
		}

	}

	private static final class RightCmpOperand extends RightOperand {

		RightCmpOperand(CompilerContext context, ExpressionNode node) {
			super(context, node);
		}

		@Override
		protected FieldDeclaration declaration(Distributor distributor) {
			return fieldDeclaration(
					this,
					distributor,
					MemberId.memberName("with"))
					.setDeclaredIn(absolutePath(
							getContext(),
							"operators",
							"compare").target(getContext()).toStaticTypeRef());
		}

	}

}
