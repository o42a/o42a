/*
    Modules Commons
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
package org.o42a.common.adapter;

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.st.StatementEnv.defaultEnv;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ByString<T> extends IntrinsicObject {

	private MemberKey inputKey;

	public ByString(
			MemberOwner owner,
			ValueType<T> valueType,
			String name,
			String sourcePath) {
		this(
				owner,
				sourcedDeclaration(owner, name, sourcePath).prototype(),
				valueType);
	}

	public ByString(
			MemberOwner owner,
			FieldDeclaration declaration,
			ValueType<T> valueType) {
		super(owner, declaration);
		setValueType(valueType);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref self = selfRef();

		self.setEnv(defaultEnv(this));

		return self.define(getScope());
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {

		final Obj inputObject =
			resolver.getScope().getContainer()
			.member(inputKey())
			.substance(resolver)
			.toArtifact()
			.materialize();
		final Value<?> inputValue =
			inputObject.value().useBy(resolver).getValue();

		if (!inputValue.isDefinite()) {
			return getValueType().runtimeValue();
		}
		if (inputValue.isFalse()) {
			return getValueType().falseValue();
		}

		final String input =
			ValueType.STRING.cast(inputValue).getDefiniteValue();
		final T result = byString(inputObject, resolver, input);

		if (result == null) {
			return getValueType().falseValue();
		}

		@SuppressWarnings("unchecked")
		final ValueType<T> valueType = (ValueType<T>) getValueType();

		return valueType.constantValue(result);
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	protected abstract T byString(
			LocationInfo location,
			Resolver resolver,
			String input);

	protected abstract void parse(Code code, ValOp result, ObjectOp input);

	private final MemberKey inputKey() {
		if (this.inputKey != null) {
			return this.inputKey;
		}

		final Member operandMember =
			member(memberName("input"), Accessor.DECLARATION);

		return this.inputKey = operandMember.getKey();
	}

	private final class ValueIR extends ProposedValueIR {

		public ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final CodeBlk cantParse = code.addBlock("cant_parse");
			final CodeDirs dirs = falseWhenUnknown(code, cantParse.head());
			final ObjectOp input =
				host.field(dirs, inputKey()).materialize(dirs);

			parse(code, result, input);
			if (cantParse.exists()) {
				result.storeFalse(cantParse);
				cantParse.go(code.tail());
			}
		}

	}

}
