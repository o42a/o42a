/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.core.ir.def.InlineEval.falseInlineEval;
import static org.o42a.core.member.Inclusions.noInclusions;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.DefinerEnv.defaultEnv;
import static org.o42a.core.value.Value.falseValue;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.common.object.BuiltinObject;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


final class ComparisonResult extends BuiltinObject {

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
	public void resolveBuiltin(FullResolver resolver) {
		this.cmp.resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {
		if (this.ref.hasError()) {
			return falseInlineEval();
		}

		final InlineValue cmpValue = this.cmp.inline(normalizer, origin);

		if (cmpValue == null) {
			return null;
		}

		return new InlineComparison(this.ref, cmpValue);
	}

	@Override
	public Eval evalBuiltin() {
		if (this.ref.hasError()) {
			return Eval.FALSE_EVAL;
		}
		return new ComparisonEval(this.ref, this.cmp);
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
				fieldDeclaration(
						this,
						distributor,
						ComparisonExpression.COMPARISON)
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

		statement.define(defaultEnv());

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

	private static final class InlineComparison extends InlineEval {

		private final ComparisonExpression ref;
		private final InlineValue cmpValue;

		InlineComparison(ComparisonExpression ref, InlineValue cmpValue) {
			super(null);
			this.ref = ref;
			this.cmpValue = cmpValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.ref.write(
					dirs.valDirs(),
					host,
					null,
					this.cmpValue));
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class ComparisonEval implements Eval {

		private final ComparisonExpression ref;
		private final Ref cmp;

		ComparisonEval(ComparisonExpression ref, Ref cmp) {
			this.ref = ref;
			this.cmp = cmp;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.ref.write(
					dirs.valDirs(),
					host,
					this.cmp.op(host),
					null));
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
