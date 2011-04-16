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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.binaryPhrase;
import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMemberRegistry;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ComparisonConstructor extends ObjectConstructor {

	private static final MemberId COMPARISON = memberName("_cmp");

	private final BinaryNode node;
	private final ComparisonConstructor prototype;
	private final Reproducer reproducer;
	private BinaryPhrasePart binary;
	private Ref phrase;

	public ComparisonConstructor(BinaryNode node, Distributor distributor) {
		super(location(distributor, node), distributor);
		this.node = node;
		this.prototype = null;
		this.reproducer = null;
	}

	protected ComparisonConstructor(
			ComparisonConstructor prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.node = prototype.node;
		this.prototype = prototype;
		this.reproducer = reproducer;
		this.binary = prototype.getBinary();
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public abstract ComparisonConstructor reproduce(Reproducer reproducer);

	@Override
	public String toString() {
		return this.node.printContent();
	}

	@Override
	protected Obj createObject() {
		return new ComparisonResult(this);
	}

	protected final BinaryPhrasePart getBinary() {
		assert this.binary != null :
			"Phrase didn't built yet";
		return this.binary;
	}

	protected final Ref getPhrase() {
		assert this.phrase != null :
			"Phrase didn't built yet";
		return this.phrase;
	}

	protected abstract boolean result(Value<?> value);

	protected ValOp writeComparison(CodeDirs dirs, ObjectOp comparison) {
		return comparison.writeValue(dirs);
	}

	protected abstract void write(
			CodeDirs dirs,
			ValOp result,
			ValOp comparisonVal);

	private final Ref phrase(Distributor distributor) {
		if (this.phrase != null) {
			return this.phrase;
		}
		if (this.prototype == null) {
			this.binary = binaryPhrase(this.node, distributor);
			return this.phrase = this.binary.getPhrase().toRef();
		}
		// Build prototype`s phrase.
		this.prototype.getResolution().toObject().resolveMembers(false);
		// Reproduce prototype`s phrase.
		return this.phrase = this.prototype.getPhrase().reproduce(
				this.reproducer.distributeBy(distributor));
	}

	private final class ComparisonResult extends Result {

		private final ComparisonConstructor ref;
		private MemberKey comparisonKey;

		ComparisonResult(ComparisonConstructor ref) {
			super(ref, ref.distribute(), ValueType.VOID);
			this.ref = ref;
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return "ComparisonResult";
			}
			return this.ref.toString();
		}

		@Override
		protected void declareMembers(ObjectMembers members) {

			final ObjectMemberRegistry memberRegistry =
				new ObjectMemberRegistry(this);
			final Distributor distributor = distribute();
			final Ref phrase = this.ref.phrase(distributor);
			final FieldBuilder builder = memberRegistry.newField(
					fieldDeclaration(this, distributor, COMPARISON)
					.setVisibility(Visibility.PRIVATE),
					phrase.toFieldDefinition());

			if (builder == null) {
				return;
			}

			final DeclarationStatement statement = builder.build();

			if (statement == null) {
				return;
			}

			statement.setEnv(defaultEnv(this));

			this.comparisonKey = statement.toMember().getKey();

			memberRegistry.registerMembers(members);
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {
			resolveMembers(false);// Initialize comparisonKey.

			final Field<?> field =
				scope.getContainer().member(this.comparisonKey).toField();
			final Value<?> value = field.getArtifact().toObject().getValue();

			if (!value.isDefinite()) {
				// Value could not be determined at compile-time.
				// Result will be determined at run time.
				return ValueType.VOID.runtimeValue();
			}
			if (value.isFalse()) {
				return ValueType.VOID.falseValue();
			}

			final boolean result = this.ref.result(value);

			return result ? voidValue() : falseValue();
		}


		@Override
		protected ObjectValueIR createValueIR(ObjectIR objectIR) {
			return new ValueIR(objectIR);
		}

	}

	private static final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final ComparisonResult object =
				(ComparisonResult) getObjectIR().getObject();
			final ComparisonConstructor ref = object.ref;
			final CodeBlk failure = code.addBlock("comparison_failure");
			final CodeDirs dirs = falseWhenUnknown(code, failure.head());
			final ObjectOp comparison =
				host.field(dirs, object.comparisonKey).materialize(dirs);
			final ValOp comparisonVal = ref.writeComparison(dirs, comparison);

			ref.write(dirs, result, comparisonVal);

			if (failure.exists()) {
				result.storeFalse(failure);
				failure.go(code.tail());
			}
		}

	}

}
