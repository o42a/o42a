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

import org.o42a.common.adapter.ByString;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;


@SourcePath(relativeTo = TestModule.class, value = "parser/string.o42a")
public final class ParseString extends ByString<String> {

	public ParseString(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = super.calculateBuiltin(resolver);

		if (!value.getLogicalValue().isTrue()) {
			return value;
		}

		return value().getValueType().runtimeValue();
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return parse(dirs, input().op(host).writeValue(dirs));
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
