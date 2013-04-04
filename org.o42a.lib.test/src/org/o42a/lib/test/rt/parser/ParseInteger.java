/*
    Test Framework
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;
import org.o42a.root.adapter.IntegerByString;


@SourcePath(relativeTo = TestModule.class, value = "parser/integer.o42a")
public final class ParseInteger extends IntegerByString {

	public ParseInteger(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = super.calculateBuiltin(resolver);

		if (!value.getKnowledge().isKnown()) {
			return value;
		}

		return type().getParameters().runtimeValue();
	}

	@Override
	protected ValOp parse(ValDirs dirs, ValOp inputVal) {
		dirs.code().debug("Run-time integer");
		return super.parse(dirs, inputVal);
	}

}
