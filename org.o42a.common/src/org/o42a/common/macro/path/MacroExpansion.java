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

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroConsumer;


public class MacroExpansion extends PathFragment {

	private final Ref macroRef;
	private ScopedLogger expansionLogger;
	private Scope origin;
	private Path initialExpansion;
	private IdentityHashMap<Scope, Path> expansions;
	private final boolean reexpansion;
	private byte init;

	public MacroExpansion(Ref macroRef, boolean reexpansion) {
		this.macroRef = macroRef;
		this.reexpansion = reexpansion;
	}

	public final Ref getMacroRef() {
		return this.macroRef;
	}

	public final Scope getOrigin() {
		if (this.origin == null) {
			// Initiate the expansion in the original scope.
			this.init = -1;
			path(getMacroRef().getScope()).rebuild();
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
			if (!start.is(getMacroRef().getScope())) {
				// Build an initial expansion
				// and return the (re-)expansion in the given one.
				return expandInScope(expander, start);
			}
			this.init = -1;
		}

		// Expansion in the original scope.
		this.origin = start;

		final MacroObject macro = macro(expander, start);

		if (macro != null) {
			this.initialExpansion = expand(expander, start, macro);
		}
		this.init = 1;

		return this.initialExpansion;
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return defaultInterface(ref);
	}

	@Override
	public String toString() {
		if (this.macroRef == null) {
			return super.toString();
		}
		return '#' + this.macroRef.toString();
	}

	final ScopedLogger getExpansionLogger() {
		return this.expansionLogger;
	}

	final Ref expandMacro(MacroConsumer consumer, Scope scope) {
		if (this.expansionLogger == null) {
			this.expansionLogger = consumer.getExpansionLogger();
		}

		final Ref macroRef = getMacroRef();
		final Ref macroExpansion = path(scope).target(macroRef.distribute());
		final Ref consumption = consumer.expandMacro(macroExpansion);

		if (consumption == null) {
			return null;
		}

		consumption.assertSameScope(macroRef);

		return consumption;
	}

	private BoundPath path(Scope scope) {
		return toPath().bind(getMacroRef(), scope);
	}

	private Path expandInScope(PathExpander expander, Scope start) {

		final Scope origin = getOrigin();

		if (start.is(origin) || origin == null) {
			return this.initialExpansion;
		}

		// Find a cached expansion.
		if (this.expansions == null) {
			this.expansions = new IdentityHashMap<>(4);
		} else if (this.expansions.containsKey(start)) {
			return this.expansions.get(start);
		}

		final Path reexpansion;
		final MacroObject macro = macro(expander, start);

		if (macro == null) {
			// Macro not found.
			reexpansion = null;
		} else {
			// Re-expand the macro in the given scope.
			reexpansion = reexpand(expander, start, macro);
		}

		this.expansions.put(start, reexpansion);

		return reexpansion;
	}

	private Path expand(PathExpander expander, Scope scope, MacroObject macro) {
		if (this.reexpansion) {
			return reexpand(expander, scope, macro);
		}

		final MacroExpanderImpl macroExpander =
				new MacroExpanderImpl(this, expander, scope, macro.getObject());

		return prefixExpansion(macro.getMacro().expand(macroExpander));
	}

	private Path reexpand(
			PathExpander expander,
			Scope scope,
			MacroObject macro) {

		final MacroExpanderImpl macroExpander =
				new MacroExpanderImpl(this, expander, scope, macro.getObject());

		return prefixExpansion(macro.getMacro().reexpand(macroExpander));
	}

	private Path prefixExpansion(Path path) {
		if (path == null) {
			return null;
		}
		return getMacroRef().getPath().getPath().append(path);
	}

	private MacroObject macro(PathExpander expander, Scope start) {

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
		if (!object.type().getValueType().is(ValueType.MACRO)) {
			return notMacro(expander);
		}

		final Value<Macro> macroValue =
				ValueType.MACRO.cast(object.value().getValue());

		if (!macroValue.getKnowledge().isKnown()
				|| macroValue.getKnowledge().isFalse()) {
			return unresolvedMacro(expander);
		}

		return new MacroObject(object, macroValue.getCompilerValue());
	}

	private MacroObject notMacro(PathExpander expander) {
		expander.getLogger().error(
				"not_macro",
				getMacroRef(),
				"Not a macro");
		return null;
	}

	private MacroObject unresolvedMacro(PathExpander expander) {
		expander.getLogger().error(
				"unresolved_macro",
				getMacroRef(),
				"Macro can not be resolved at compile time");
		return null;
	}

	private static final class MacroObject {

		private final Obj object;
		private final Macro macro;

		MacroObject(Obj object, Macro macro) {
			this.object = object;
			this.macro = macro;
		}

		public final Obj getObject() {
			return this.object;
		}

		public final Macro getMacro() {
			return this.macro;
		}

		@Override
		public String toString() {
			if (this.macro == null) {
				return super.toString();
			}
			return this.macro.toString();
		}

	}

}
