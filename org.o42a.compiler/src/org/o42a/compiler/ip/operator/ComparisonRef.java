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
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.common.object.BuiltinObject;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMemberRegistry;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.UserInfo;


public final class ComparisonRef extends ObjectConstructor {

	private static final MemberId COMPARISON = fieldName("_cmp");

	private final Interpreter ip;
	private final BinaryNode node;
	private final ComparisonRef prototype;
	private final Reproducer reproducer;
	private ComparisonOperator operator;
	private Ref phrase;
	private byte error;


	public ComparisonRef(
			Interpreter ip,
			BinaryNode node,
			Distributor distributor) {
		super(location(distributor, node), distributor);
		this.ip = ip;
		this.node = node;
		this.prototype = null;
		this.reproducer = null;
	}

	private ComparisonRef(
			ComparisonRef prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.ip = prototype.ip;
		this.node = prototype.node;
		this.prototype = prototype;
		this.reproducer = reproducer;
		this.operator = prototype.getOperator();
		this.error = prototype.error;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	public final boolean hasError() {
		if (this.error != 0) {
			return this.error > 0;
		}
		if (this.operator.checkError(this.phrase)) {
			this.error = 1;
			return true;
		}
		this.error = -1;
		return false;
	}

	@Override
	public ComparisonRef reproduce(Reproducer reproducer) {
		return new ComparisonRef(this, reproducer);
	}

	@Override
	public String toString() {
		return this.node.printContent();
	}

	@Override
	protected Obj createObject() {
		return new ComparisonResult(this);
	}

	protected final ComparisonOperator getOperator() {
		assert this.operator != null :
			"Phrase didn't built yet";
		return this.operator;
	}

	protected final Ref getPhrase() {
		assert this.phrase != null :
			"Phrase didn't built yet";
		return this.phrase;
	}

	private final Ref phrase(Distributor distributor) {
		if (this.phrase != null) {
			return this.phrase;
		}
		if (this.prototype == null) {

			final BinaryPhrasePart binary =
				binaryPhrase(this.ip, this.node, distributor);

			this.operator = binary.getComparisonOperator();

			if (this.operator == null) {
				this.error = 1;
			}

			return this.phrase = binary.getPhrase().toRef();
		}
		// Build prototype`s phrase.
		this.prototype.getResolution().toObject().resolveMembers(false);
		// Reproduce prototype`s phrase.
		return this.phrase = this.prototype.getPhrase().reproduce(
				this.reproducer.distributeBy(distributor));
	}

	private final class ComparisonResult extends BuiltinObject {

		private final ComparisonRef ref;
		private MemberKey comparisonKey;

		ComparisonResult(ComparisonRef ref) {
			super(ref, ref.distribute(), ValueType.VOID);
			this.ref = ref;
		}

		@Override
		public Value<?> calculateBuiltin(Resolver resolver) {
			resolveMembers(false);// Initialize comparisonKey.

			if (hasError()) {
				return falseValue();
			}

			final Field<?> field =
				resolver.getContainer()
				.member(this.comparisonKey)
				.toField(resolver);
			final Value<?> value =
				field.getArtifact().toObject().value(resolver).getValue();

			if (!value.isDefinite()) {
				// Value could not be determined at compile-time.
				// Result will be determined at run time.
				return ValueType.VOID.runtimeValue();
			}

			final boolean result = this.ref.getOperator().result(value);

			return result ? voidValue() : falseValue();
		}

		@Override
		public void resolveBuiltin(Obj object) {

			final UserInfo user = object.value(dummyUser());
			final Field<?> field =
				object.member(this.comparisonKey).toField(user);

			field.getArtifact().toObject().resolveDefinitions(user);
		}

		@Override
		public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
			if (this.ref.hasError()) {
				dirs.code().go(dirs.falseDir());
				return falseValue().op(dirs.getBuilder(), dirs.code());
			}

			final ComparisonOperator operator = this.ref.getOperator();
			final ValDirs cmpDirs = dirs.dirs().falseWhenUnknown().value(
					operator.getValueType(),
					"cmp");
			final ObjectOp comparison =
				host.field(cmpDirs.dirs(), this.comparisonKey)
				.materialize(cmpDirs.dirs());
			final ValOp comparisonVal =
				operator.writeComparison(cmpDirs, comparison);

			final ValDirs resultDirs = cmpDirs.dirs().value(dirs);
			final ValOp result = operator.write(resultDirs, comparisonVal);

			resultDirs.done();
			cmpDirs.done();

			return result;
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

	}

}
