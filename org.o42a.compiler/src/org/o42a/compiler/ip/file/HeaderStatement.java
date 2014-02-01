/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.*;
import org.o42a.util.log.LogInfo;


class HeaderStatement extends Statement {

	static void notDirective(CompilerLogger logger, LogInfo location) {
		logger.error(
				"not_header_directive",
				location,
				"Only directives allowed in file header");
	}

	private final Ref ref;

	HeaderStatement(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public boolean isValid() {
		return getRef().isValid();
	}

	@Override
	public Command command(CommandEnv env) {
		return new HeaderCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new HeaderStatement(ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

}
