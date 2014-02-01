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
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.macro.MacroConsumer;


final class MacroExpansionTemplate extends PathTemplate {

	private final MacroExpansion expansion;
	private MacroConsumer consumer;

	MacroExpansionTemplate(MacroExpansion expansion) {
		this.expansion = expansion;
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
	public Path expand(PathExpander expander, int index, Scope start) {
		this.expansion.getMacroRef().assertCompatible(start);

		final Ref consumption =
				this.expansion.expandMacro(this.consumer, start);

		if (consumption == null) {
			return null;
		}

		return consumption.getPath().getRawPath();
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		return this.expansion.toString();
	}

	final Ref toRef(Consumer consumer) {

		final Ref macroRef = this.expansion.getMacroRef();
		final Ref expansion = toPath()
				.bind(macroRef, macroRef.getScope())
				.target(macroRef.distribute());

		this.consumer = consumer.expandMacro(
				this.expansion.getMacroRef(),
				this,
				expansion);
		if (this.consumer == null) {
			return null;
		}

		return expansion;
	}

}
