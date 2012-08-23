/*
    Compiler Core
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
package org.o42a.core.object.macro.impl;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.object.macro.MacroExpander;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Loggable;


final class PrefixedMacro implements Macro {

	static PrefixedMacro prefixMacro(PrefixPath prefix, Macro macro) {
		if (macro.getClass() != PrefixedMacro.class) {
			return new PrefixedMacro(prefix, macro);
		}

		final PrefixedMacro prefixed = (PrefixedMacro) macro;
		final PrefixPath oldPrefix = prefixed.prefix;
		final PrefixPath newPrefix = prefix.and(oldPrefix);

		if (newPrefix == oldPrefix) {
			return prefixed;
		}

		return new PrefixedMacro(newPrefix, prefixed.macro);
	}

	private final PrefixPath prefix;
	private final Macro macro;

	PrefixedMacro(PrefixPath prefix, Macro macro) {
		this.prefix = prefix;
		this.macro = macro;
	}

	@Override
	public Path expand(MacroExpander expander) {

		final Path expansion = this.macro.expand(
				new PrefixedMacroExpander(this.prefix, expander));

		return prefixExpansion(expansion);
	}

	@Override
	public Path reexpand(MacroExpander expander) {

		final Path expansion = this.macro.reexpand(
				new PrefixedMacroExpander(this.prefix, expander));

		return prefixExpansion(expansion);
	}

	@Override
	public String toString() {
		if (this.macro == null) {
			return super.toString();
		}
		return this.macro.toString();
	}

	private Path prefixExpansion(Path expansion) {
		if (expansion == null) {
			return null;
		}
		return expansion.prefixWith(this.prefix);
	}

	private static final class PrefixedMacroExpander implements MacroExpander {

		private final PrefixPath prefix;
		private final MacroExpander expander;

		PrefixedMacroExpander(PrefixPath prefix, MacroExpander expander) {
			this.prefix = prefix;
			this.expander = expander;
		}

		@Override
		public CompilerContext getContext() {
			return this.expander.getContext();
		}

		@Override
		public Loggable getLoggable() {
			return this.expander.getLoggable();
		}

		@Override
		public Scope getScope() {
			return this.prefix.getStart();
		}

		@Override
		public Ref getMacroRef() {
			return this.expander.getMacroRef().prefixWith(this.prefix);
		}

		@Override
		public Obj getMacroObject() {
			return this.expander.getMacroObject();
		}

		@Override
		public CompilerLogger getLogger() {
			return this.expander.getLogger();
		}

		@Override
		public void error(LogRecord message) {
			this.expander.error(message);
		}

		@Override
		public String toString() {
			if (this.expander == null) {
				return super.toString();
			}
			return this.expander.toString();
		}

	}

}