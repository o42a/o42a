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

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.compiler.ip.phrase.ref.Phrase;
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

	private final Ref phrase;

	public ComparisonConstructor(Phrase phrase) {
		super(phrase, phrase.distribute());
		this.phrase = phrase.toRef();
	}

	protected ComparisonConstructor(
			ComparisonConstructor prototype,
			Reproducer reproducer,
			Ref phrase) {
		super(prototype, reproducer.distribute());
		this.phrase = prototype.phrase;
	}

	public final Ref getPhrase() {
		return this.phrase;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public final Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref phrase = this.phrase.reproduce(reproducer);

		if (phrase == null) {
			return null;
		}

		return reproduce(reproducer, phrase);
	}

	@Override
	public String toString() {
		return this.phrase.toString();
	}

	@Override
	protected Obj createObject() {
		return new ComparisonResult(this);
	}

	protected abstract boolean result(Value<?> value);

	protected abstract ComparisonConstructor reproduce(
			Reproducer reproducer,
			Ref phrase);

	protected ValOp writeComparison(CodeDirs dirs, ObjectOp comparison) {
		return comparison.writeValue(dirs);
	}

	protected abstract void write(
			CodeDirs dirs,
			ValOp result,
			ValOp comparisonVal);

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
			final FieldBuilder builder = memberRegistry.newField(
					fieldDeclaration(this, distributor, COMPARISON)
					.setVisibility(Visibility.PRIVATE),
					this.ref.phrase.toFieldDefinition());

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
