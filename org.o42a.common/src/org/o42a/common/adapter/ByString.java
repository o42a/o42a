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

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.UserInfo;


public abstract class ByString<T> extends IntrinsicBuiltin {

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
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Obj inputObject =
			resolver.getScope().getContainer()
			.member(inputKey())
			.substance(resolver)
			.toArtifact()
			.materialize();
		final Value<?> inputValue =
			inputObject.value().useBy(resolver).getValue();

		if (inputValue.isFalse()) {
			return getValueType().falseValue();
		}
		if (!inputValue.isDefinite()) {
			return getValueType().runtimeValue();
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
	public void resolveBuiltin(Obj object) {

		final UserInfo user = object.value();
		final Obj inputObject =
			object.member(inputKey())
			.substance(object.getScope().newResolver(user))
			.toArtifact()
			.materialize();

		inputObject.value().useBy(user);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ObjectOp input =
			host.field(dirs.dirs(), inputKey()).materialize(dirs.dirs());

		return parse(dirs, input);
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

	protected abstract T byString(
			LocationInfo location,
			Resolver resolver,
			String input);

	protected abstract ValOp parse(ValDirs dirs, ObjectOp input);

	private final MemberKey inputKey() {
		if (this.inputKey != null) {
			return this.inputKey;
		}

		final Member operandMember =
			member(memberName("input"), Accessor.DECLARATION);

		return this.inputKey = operandMember.getKey();
	}

}
