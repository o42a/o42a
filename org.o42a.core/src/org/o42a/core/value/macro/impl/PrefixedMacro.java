/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.macro.impl;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroExpander;


final class PrefixedMacro implements Macro {

	static PrefixedMacro prefixMacro(PrefixPath prefix, Macro macro) {
		if (macro.getClass() != PrefixedMacro.class) {
			return new PrefixedMacro(prefix, macro);
		}

		final PrefixedMacro prefixed = (PrefixedMacro) macro;
		final PrefixPath oldPrefix = prefixed.prefix;
		final PrefixPath newPrefix = oldPrefix.and(prefix);

		if (newPrefix == oldPrefix) {
			return prefixed;
		}

		return new PrefixedMacro(newPrefix, prefixed.macro);
	}

	private final PrefixPath prefix;
	private final Macro macro;

	private PrefixedMacro(PrefixPath prefix, Macro macro) {
		this.prefix = prefix;
		this.macro = macro;
	}

	@Override
	public Path expand(MacroExpander expander) {

		final PrefixedMacroExpander prefixedExpander =
				prefixedExpander(expander);

		if (prefixedExpander == null) {
			return null;
		}

		final Path expansion = this.macro.expand(prefixedExpander);

		return prefixExpansion(expansion);
	}

	@Override
	public Path reexpand(MacroExpander expander) {

		final PrefixedMacroExpander prefixedExpander =
				prefixedExpander(expander);

		if (prefixedExpander == null) {
			return null;
		}

		final Path expansion = this.macro.reexpand(prefixedExpander);

		return prefixExpansion(expansion);
	}

	@Override
	public String toString() {
		if (this.macro == null) {
			return super.toString();
		}
		return this.macro.toString();
	}

	private PrefixedMacroExpander prefixedExpander(MacroExpander expander) {

		final Scope macroScope = expander.getMacroObject().getScope();
		final Ref finalRef =
				this.prefix.getPrefix()
				.bind(expander.getMacroRef(), macroScope)
				.target(macroScope.distribute());
		final Resolver resolver = macroScope.resolver();
		final Resolution resolution = finalRef.resolve(resolver);

		if (!resolution.isResolved()) {
			assert resolution.isError() :
				"Not macro: " + resolution;
			return null;
		}

		final Obj macroObject = resolution.toObject();

		assert macroObject != null :
			"Not macro: " + resolution;

		return new PrefixedMacroExpander(expander, finalRef, macroObject);
	}

	private Path prefixExpansion(Path expansion) {
		if (expansion == null) {
			return null;
		}
		return expansion.prefixWith(this.prefix);
	}

	private static final class PrefixedMacroExpander implements MacroExpander {

		private final MacroExpander expander;
		private final Ref macroRef;
		private final Obj macroObject;

		PrefixedMacroExpander(
				MacroExpander expander,
				Ref macroRef,
				Obj macroObject) {
			this.expander = expander;
			this.macroRef = macroRef;
			this.macroObject = macroObject;
		}

		@Override
		public Location getLocation() {
			return this.expander.getLocation();
		}

		@Override
		public Scope getScope() {
			return this.macroRef.getScope();
		}

		@Override
		public Ref getMacroRef() {
			return this.macroRef;
		}

		@Override
		public Obj getMacroObject() {
			return this.macroObject;
		}

		@Override
		public CompilerLogger getLogger() {
			return this.expander.getLogger();
		}

		@Override
		public ScopedLogger getExpansionLogger() {
			return this.expander.getExpansionLogger();
		}

		@Override
		public CompilerLogger getExplicitLogger() {
			return this.expander.getExplicitLogger();
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
