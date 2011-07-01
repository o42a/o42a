/*
    Test Framework
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
package org.o42a.lib.test.rt.parser;

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.common.adapter.ByString;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class ParseString extends ByString<String> {

	ParseString(Parser parser) {
		super(
				parser.toMemberOwner(),
				sourcedDeclaration(
						parser,
						"string",
						"test/parser/string.o42a")
				.prototype(),
				ValueType.STRING);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = super.calculateBuiltin(resolver);

		if (!value.getLogicalValue().isTrue()) {
			return value;
		}

		return getValueType().runtimeValue();
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return parse(dirs, input().op(host).writeValue(dirs));
	}

	@Override
	protected Ascendants createAscendants() {

		final Scope enclosingScope = getScope().getEnclosingScope();
		final Path ancestorPath = enclosingScope.getEnclosingScopePath().append(
				memberName("rt-string")
				.key(enclosingScope.getEnclosingScope()));

		return new Ascendants(this).setAncestor(
				ancestorPath.target(this, enclosingScope.distribute())
				.toTypeRef());
	}

	@Override
	protected String byString(
			LocationInfo location,
			Resolver resolver,
			String input) {
		return input;
	}

	@Override
	protected ValOp parse(ValDirs dirs, ValOp value) {
		dirs.code().debug("Run-time string");
		return value;
	}

}
