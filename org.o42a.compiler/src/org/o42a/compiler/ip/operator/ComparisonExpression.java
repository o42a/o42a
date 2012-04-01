/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import static org.o42a.core.member.Inclusions.noInclusions;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.InlineValue.inlineFalse;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.common.object.BuiltinObject;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public final class ComparisonExpression extends ObjectConstructor {

	private static final MemberId COMPARISON = fieldName("_cmp");

	private final Interpreter ip;
	private final BinaryNode node;
	private final ComparisonExpression prototype;
	private final Reproducer reproducer;
	private ComparisonOperator operator;
	private Ref phrase;
	private byte error;

	public ComparisonExpression(
			Interpreter ip,
			BinaryNode node,
			Distributor distributor) {
		super(location(distributor, node), distributor);
		this.ip = ip;
		this.node = node;
		this.prototype = null;
		this.reproducer = null;
	}

	private ComparisonExpression(
			ComparisonExpression prototype,
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
	public ComparisonExpression reproduce(PathReproducer reproducer) {
		return new ComparisonExpression(this, reproducer.getReproducer());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		this.node.printContent(out);

		return out.toString();
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
		this.prototype.getConstructed().resolveMembers(false);
		// Reproduce prototype`s phrase.
		return this.phrase = this.prototype.getPhrase().reproduce(
				this.reproducer.distributeBy(distributor));
	}

	private ValOp write(
			ValDirs dirs,
			HostOp host,
			RefOp cmp,
			InlineValue inlineCmp) {

		final ComparisonOperator operator = getOperator();
		final ValDirs cmpDirs = dirs.dirs().falseWhenUnknown().value(
				operator.getValueStruct(),
				"cmp");
		final ValOp cmpVal;

		if (inlineCmp != null) {
			cmpVal = operator.inlineComparison(cmpDirs, host, inlineCmp);
		} else {
			cmpVal = operator.writeComparison(cmpDirs, cmp);
		}

		final ValDirs resultDirs = cmpDirs.dirs().value(dirs);
		final ValOp result = operator.write(resultDirs, cmpVal);

		resultDirs.done();
		cmpDirs.done();

		return result;
	}

	private static final class ComparisonResult extends BuiltinObject {

		private final ComparisonExpression ref;
		private Ref cmp;

		ComparisonResult(ComparisonExpression ref) {
			super(ref, ref.distribute(), ValueStruct.VOID);
			this.ref = ref;
		}

		@Override
		public Value<?> calculateBuiltin(Resolver resolver) {
			resolveMembers(false);// Initialize comparisonKey.

			if (this.ref.hasError()) {
				return falseValue();
			}

			final Value<?> value = this.cmp.value(resolver);

			if (!value.getKnowledge().isKnown()) {
				// Value could not be determined at compile-time.
				// Result will be determined at run time.
				return ValueType.VOID.runtimeValue();
			}

			final boolean result = this.ref.getOperator().result(value);

			return result ? voidValue() : falseValue();
		}

		@Override
		public void resolveBuiltin(Resolver resolver) {
			this.cmp.resolve(resolver).resolveValue();
		}

		@Override
		public InlineValue inlineBuiltin(
				Normalizer normalizer,
				ValueStruct<?, ?> valueStruct,
				Scope origin) {
			if (this.ref.hasError()) {
				return inlineFalse(valueStruct);
			}

			final InlineValue cmpValue = this.cmp.inline(normalizer, origin);

			if (cmpValue == null) {
				return null;
			}

			return new Inline(valueStruct, this.ref, cmpValue);
		}

		@Override
		public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
			if (this.ref.hasError()) {
				dirs.code().go(dirs.falseDir());
				return falseValue().op(dirs.getBuilder(), dirs.code());
			}
			return this.ref.write(dirs, host, this.cmp.op(host), null);
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
					new ObjectMemberRegistry(noInclusions(), this);
			final Distributor distributor = distribute();
			final Ref phrase = this.ref.phrase(distributor);
			final FieldBuilder builder = memberRegistry.newField(
					fieldDeclaration(this, distributor, COMPARISON)
					.setVisibility(Visibility.PRIVATE)
					.setLinkType(LinkValueType.LINK),
					phrase.toFieldDefinition());

			if (builder == null) {
				return;
			}

			final DeclarationStatement statement = builder.build();

			if (statement == null) {
				return;
			}

			statement.define(defaultEnv(this));

			this.cmp = statement
					.toMember()
					.getKey()
					.toPath()
					.dereference()
					.bind(this, getScope())
					.target(distribute());

			memberRegistry.registerMembers(members);
		}

		@Override
		protected Obj findObjectIn(Scope enclosing) {
			return this.ref.resolve(enclosing);
		}

	}

	private static final class Inline extends InlineValue {

		private final ComparisonExpression ref;
		private final InlineValue cmpValue;

		Inline(
				ValueStruct<?, ?> valueStruct,
				ComparisonExpression ref,
				InlineValue cmpValue) {
			super(valueStruct);
			this.ref = ref;
			this.cmpValue = cmpValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.ref.write(dirs, host, null, this.cmpValue);
		}

		@Override
		public void cancel() {
			this.cmpValue.cancel();
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}
