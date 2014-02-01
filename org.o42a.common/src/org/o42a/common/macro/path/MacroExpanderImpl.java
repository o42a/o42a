/*
    Compiler Commons
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
package org.o42a.common.macro.path;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.macro.MacroExpander;


final class MacroExpanderImpl implements MacroExpander {

	private final MacroExpansion expansion;
	private final PathExpander pathExpander;
	private final Scope scope;
	private final Obj macroObject;
	private final CompilerLogger logger;

	MacroExpanderImpl(
			MacroExpansion expansion,
			PathExpander pathExpander,
			Scope scope,
			Obj macroObject) {
		this.expansion = expansion;
		this.pathExpander = pathExpander;
		this.scope = scope;
		this.macroObject = macroObject;
		this.logger = expansion.getExpansionLogger().compilerLogger(
				scope,
				getExplicitLogger());
	}

	public final MacroExpansion getExpansion() {
		return this.expansion;
	}

	@Override
	public final Location getLocation() {
		return this.pathExpander.getPath().getLocation();
	}

	@Override
	public final Scope getScope() {
		return this.scope;
	}

	@Override
	public final Ref getMacroRef() {
		return this.expansion.getMacroRef();
	}

	@Override
	public final Obj getMacroObject() {
		return this.macroObject;
	}

	@Override
	public final CompilerLogger getLogger() {
		return this.logger;
	}

	@Override
	public final ScopedLogger getExpansionLogger() {
		return this.expansion.getExpansionLogger();
	}

	@Override
	public CompilerLogger getExplicitLogger() {
		return this.pathExpander.getLogger();
	}

}
