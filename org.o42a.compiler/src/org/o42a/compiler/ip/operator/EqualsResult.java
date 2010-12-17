/*
    Compiler
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.def.Def.voidDef;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.member.field.FieldDefinition.fieldDefinition;
import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Cond;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;


class EqualsResult extends PlainObject {

	private static final MemberId RESULT = memberName("_result");

	private final BinaryOp binaryOp;
	private final TypeRef operator;
	private MemberKey resultKey;

	EqualsResult(BinaryOp binaryOp, Distributor distributor, Ref operator) {
		super(
				new Location(
						binaryOp.getContext(),
						binaryOp.getNode().getSign()),
				distributor);
		this.binaryOp = binaryOp;
		this.operator = operator.toTypeRef();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(getScope())
		.setAncestor(ValueType.VOID.typeRef(
				this,
				getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {

		final ObjectMemberRegistry fieldRegistry =
			new ObjectMemberRegistry(this);
		final DeclarativeBlock block =
			new DeclarativeBlock(this, this, fieldRegistry);

		new ResultBuilder(getContext(), getNode()).buildBlock(block);

		fieldRegistry.registerMembers(members);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return voidDef(this, distribute()).and(condition()).toDefinitions();
	}

	protected Cond condition() {
		return new ResultCond();
	}

	static final class NotEqualsResult extends EqualsResult {

		NotEqualsResult(
				BinaryOp binaryOp,
				Distributor distributor,
				Ref operator) {
			super(binaryOp, distributor, operator);
		}

		@Override
		protected Cond condition() {
			return super.condition().negate();
		}

	}

	private final class ResultBuilder extends BlockBuilder {

		public ResultBuilder(CompilerContext context, Node node) {
			super(context, node);
		}

		@Override
		public void buildBlock(Block<?> block) {

			final Statements<?> statements =
				block.propose(this).alternative(this);
			final FieldDeclaration declarator =
				FieldDeclaration.fieldDeclaration(
						this,
						block.distribute(),
						RESULT)
				.setVisibility(Visibility.PRIVATE);

			final TypeRef operator = EqualsResult.this.operator;
			final FieldVariant<?> field = statements.field(
					declarator,
					fieldDefinition(
							this,
							new AscendantsDefinition(
									operator,
									block.distribute()).setAncestor(
											operator.rescope(block.getScope())),
							new RightEqOperand(
									getContext(),
									EqualsResult.this.binaryOp.getNode()
									.getRightOperand())));

			EqualsResult.this.resultKey = field.getField().getKey();
		}

	}

	private static final class RightEqOperand extends RightOperand {

		RightEqOperand(CompilerContext context, ExpressionNode node) {
			super(context, node);
		}

		@Override
		protected FieldDeclaration declaration(Distributor distributor) {
			return fieldDeclaration(
					this,
					distributor,
					memberName("to"))
					.setDeclaredIn(absolutePath(
							getContext(),
							"operators",
							"equals").target(getContext()).toStaticTypeRef());
		}

	}

	private final class ResultCond extends Cond {

		ResultCond() {
			super(EqualsResult.this, EqualsResult.this.getScope());
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {

			final Obj object = scope.getContainer().toObject();

			object.getMembers();// declare fields to initialize resultKey

			final Field<?> field =
				object.member(EqualsResult.this.resultKey).toField();

			return field.getArtifact().toObject().getValue().getLogicalValue();
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {

			final HostOp field =
				host.field(code, exit, EqualsResult.this.resultKey);

			field.toObject(code, exit).writeCondition(code, exit);
		}

		@Override
		public String toString() {
			return "(" + EqualsResult.this + ")?";
		}

	}

}
