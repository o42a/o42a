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
package org.o42a.compiler.ip.type.param;

import static org.o42a.core.value.macro.MacroConsumer.DEFAULT_MACRO_EXPANSION_LOGGER;

import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Scope;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.core.value.macro.MacroConsumer;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


public class TypeParamConsumer extends TypeConsumer implements Consumer {

	private final TypeParamMacroDep macroDep;

	public TypeParamConsumer(Nesting nesting, TypeParameterKey parameterKey) {
		super(nesting);
		this.macroDep = new TypeParamMacroDep(nesting, parameterKey, 0);
	}

	private TypeParamConsumer(TypeParamMacroDep macroDep) {
		super(macroDep.getNesting());
		this.macroDep = macroDep;
	}

	@Override
	public final TypeParamConsumer paramConsumer(
			TypeParameterKey parameterKey) {
		return new TypeParamConsumer(new TypeParamMacroDep(
				this.macroDep.getNesting(),
				parameterKey,
				this.macroDep.getDepth() + 1));
	}

	@Override
	public TypeRef consumeType(Ref ref, TypeParametersBuilder typeParameters) {

		final Ref consumption = ref.consume(this);

		if (consumption == null) {
			return null;
		}

		return consumption.toTypeRef(typeParameters);
	}

	@Override
	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {
		return new TypeParamMacroConsumer(this.macroDep, macroRef, template);
	}

	private static final class TypeParamMacroConsumer
			implements MacroConsumer {

		private final TypeParamMacroDep macroDep;
		private final Ref macroRef;
		private final PathTemplate template;
		private final TypeParamExpansionLogger expansionLogger;
		private boolean dependencyRegistered;

		TypeParamMacroConsumer(
				TypeParamMacroDep macroDep,
				Ref macroRef,
				PathTemplate template) {
			this.macroDep = macroDep;
			this.macroRef = macroRef;
			this.template = template;
			this.expansionLogger = new TypeParamExpansionLogger(macroDep);
		}

		@Override
		public ScopedLogger getExpansionLogger() {
			return this.expansionLogger;
		}

		@Override
		public Ref expandMacro(Ref macroExpansion) {
			if (this.dependencyRegistered) {
				return macroExpansion;
			}
			this.dependencyRegistered = true;

			final TypeParamMetaDep dep =
					this.macroDep.buildDep(this.macroRef, this.template);

			if (dep != null) {
				dep.register();
			}

			return macroExpansion;
		}

	}

	private static final class TypeParamExpansionLogger extends ScopedLogger {

		private final TypeParamMacroDep dep;

		TypeParamExpansionLogger(TypeParamMacroDep dep) {
			this.dep = dep;
		}

		@Override
		public void log(
				Scope scope,
				Logger logger,
				LogRecord message) {
			DEFAULT_MACRO_EXPANSION_LOGGER.log(
					reportScope(scope),
					logger,
					message);
		}

		private Scope reportScope(Scope scope) {

			final Nesting nesting = this.dep.getNesting();

			if (nesting == null) {
				return scope;
			}

			return nesting.findObjectIn(scope).getScope();
		}
	}

}
