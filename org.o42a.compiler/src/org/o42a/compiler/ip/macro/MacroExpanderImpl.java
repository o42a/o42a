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
package org.o42a.compiler.ip.macro;

import org.o42a.core.object.macro.MacroExpander;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Loggable;


final class MacroExpanderImpl implements MacroExpander {

	private final MacroExpansion expansion;
	private final PathExpander pathExpander;

	MacroExpanderImpl(
			MacroExpansion expansion,
			PathExpander pathExpander) {
		this.expansion = expansion;
		this.pathExpander = pathExpander;
	}

	@Override
	public final CompilerContext getContext() {
		return this.pathExpander.getPath().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.pathExpander.getPath().getLoggable();
	}

	@Override
	public final Ref getMacroRef() {
		return this.expansion.getMacroRef();
	}

	@Override
	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public void error(LogRecord message) {
		this.pathExpander.error(message);
	}

}