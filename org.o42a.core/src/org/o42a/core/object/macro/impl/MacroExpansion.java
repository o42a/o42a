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

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public class MacroExpansion extends PathFragment {

	private final Ref macroRef;
	private final BoundPath path;
	private Scope origin;
	private Path initialExpansion;
	private IdentityHashMap<Scope, Path> expansions;
	private final boolean reexpansion;
	private byte init;

	public MacroExpansion(Ref macroRef, boolean reexpansion) {
		this.macroRef = macroRef;
		this.reexpansion = reexpansion;
		this.path = toPath().bind(macroRef, macroRef.getScope());
	}

	public final Ref getMacroRef() {
		return this.macroRef;
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final Scope getOrigin() {
		if (this.origin == null) {
			// Initiate the expansion in the original scope.
			this.init = -1;
			getPath().rebuild();
			// This may be not updated due to resolution errors.
			this.init = 1;
		}
		return this.origin;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		if (this.init > 0) {
			// Initially expanded.
			return expandInScope(expander, start);
		}
		if (this.init == 0) {
			// Expansion not initiated yet.
			if (expander.getPath() != getPath()) {
				// Build an initial expansion
				// and return the (re-)expansion in the given one.
				return expandInScope(expander, start);
			}
			this.init = -1;
		}

		// Expansion in the original scope.
		this.origin = start;

		final Macro macro = macro(expander, start);

		if (macro != null) {
			this.initialExpansion = expand(expander, macro);
		}
		this.init = 1;

		return this.initialExpansion;
	}

	@Override
	public String toString() {
		if (this.macroRef == null) {
			return super.toString();
		}
		return '#' + this.macroRef.toString();
	}

	final Ref expandMacro(Consumer consumer) {

		final Ref macroRef = getMacroRef();
		final Ref macroExpansion = getPath().target(macroRef.distribute());
		final Ref consumption = consumer.expandMacro(macroRef, macroExpansion);

		if (consumption == null) {
			return null;
		}

		consumption.assertSameScope(macroRef);

		return consumption;
	}

	private Path expandInScope(PathExpander expander, Scope start) {

		final Scope origin = getOrigin();

		if (start.is(origin) || origin == null) {
			return this.initialExpansion;
		}
		if (this.initialExpansion == null) {
			// Initial expansion error.
			return null;
		}

		// Find a cached expansion.
		if (this.expansions == null) {
			this.expansions = new IdentityHashMap<Scope, Path>(4);
		} else if (this.expansions.containsKey(start)) {
			return this.expansions.get(start);
		}

		final Path reexpansion;
		final Macro macro = macro(expander, start);

		if (macro == null) {
			// Macro not found.
			reexpansion = null;
		} else {
			// Re-expand the macro in the given scope.
			reexpansion = reexpand(expander, macro);
		}

		this.expansions.put(start, reexpansion);

		return reexpansion;
	}

	private Path expand(PathExpander expander, Macro macro) {
		if (this.reexpansion) {
			return reexpand(expander, macro);
		}

		final MacroExpanderImpl macroExpander =
				new MacroExpanderImpl(this, expander);

		return macro.expand(macroExpander);
	}

	private Path reexpand(PathExpander expander, Macro macro) {

		final MacroExpanderImpl macroExpander =
				new MacroExpanderImpl(this, expander);

		return macro.reexpand(macroExpander);
	}

	private Macro macro(PathExpander expander, Scope start) {

		final Resolution resolution = getMacroRef().resolve(start.resolver());

		if (!resolution.isResolved()) {
			if (resolution.isError()) {
				return null;
			}
			return notMacro(expander);
		}

		final Obj object = resolution.toObject();

		if (object == null) {
			return notMacro(expander);
		}
		if (object.value().getValueType() != ValueType.MACRO) {
			return notMacro(expander);
		}

		final Value<Macro> macroValue = ValueStruct.MACRO.cast(
				object.value().getDefinitions().value(
						object.getScope().resolver()));

		if (!macroValue.getKnowledge().isKnown()
				|| macroValue.getKnowledge().isFalse()) {
			return unresolvedMacro(expander);
		}

		return macroValue.getCompilerValue();
	}

	private Macro notMacro(PathExpander expander) {
		expander.getPath().getLogger().error(
				"not_macro",
				getMacroRef(),
				"Not a macro");
		return null;
	}

	private Macro unresolvedMacro(PathExpander expander) {
		expander.error(expander.getPath().getLogger().errorRecord(
				"unresolved_macro",
				getMacroRef(),
				"Macro can not be resolved"));
		return null;
	}

}
