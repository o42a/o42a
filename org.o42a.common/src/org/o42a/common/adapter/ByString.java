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
import static org.o42a.util.use.User.dummyUser;

import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ByString<T> extends IntrinsicBuiltin {

	private Ref input;

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

		final Value<?> inputValue = input().value(resolver);

		if (inputValue.isFalse()) {
			return getValueType().falseValue();
		}
		if (!inputValue.isDefinite()) {
			return getValueType().runtimeValue();
		}

		final String input =
			ValueType.STRING.cast(inputValue).getDefiniteValue();
		final T result = byString(input(), resolver, input);

		if (result == null) {
			return getValueType().falseValue();
		}

		@SuppressWarnings("unchecked")
		final ValueType<T> valueType = (ValueType<T>) getValueType();

		return valueType.constantValue(result);
	}

	@Override
	public void resolveBuiltin(Obj object) {

		final Resolver resolver = object.value(dummyUser()).valueResolver();

		input().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs inputDirs = dirs.dirs().value(ValueType.STRING, "input");
		final ValOp inputValue = input().op(host).writeValue(inputDirs);

		final ValDirs parseDirs = inputDirs.dirs().value(dirs);

		final ValOp result = parse(parseDirs, inputValue);

		parseDirs.done();
		inputDirs.done();

		return result;
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

	protected abstract ValOp parse(ValDirs dirs, ValOp inputVal);

	protected final Ref input() {
		if (this.input != null) {
			return this.input;
		}

		final Member member =
			member(memberName("input"), Accessor.DECLARATION);
		final Path path = member.getKey().toPath();

		return this.input = path.target(this, distribute());
	}

}
