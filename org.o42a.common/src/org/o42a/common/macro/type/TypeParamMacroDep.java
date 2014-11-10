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
package org.o42a.common.macro.type;

import org.o42a.core.object.ObjectMeta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.value.macro.MacroConsumer;
import org.o42a.core.value.macro.MacroDep;


public final class TypeParamMacroDep
		implements MacroDep<TypeParamMetaDep>, Consumer {

	private final Nesting nesting;
	private final TypeParameterKey parameterKey;
	private final int depth;

	public TypeParamMacroDep(
			Nesting nesting,
			TypeParameterKey parameterKey,
			int depth) {
		this.nesting = nesting;
		this.parameterKey = parameterKey;
		this.depth = depth;
	}

	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template) {
		return new TypeParamMacroConsumer(this, macroRef, template);
	}

	@Override
	public final MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {
		return expandMacro(macroRef, template);
	}

	public final Nesting getNesting() {
		return this.nesting;
	}

	public final TypeParameterKey getParameterKey() {
		return this.parameterKey;
	}

	public final int getDepth() {
		return this.depth;
	}

	@Override
	public TypeParamMetaDep newDep(
			ObjectMeta meta,
			Ref macroRef,
			PathTemplate template) {
		return new TypeParamMetaDep(meta, this, macroRef, template);
	}

	@Override
	public void setParentDep(TypeParamMetaDep dep, MetaDep parentDep) {
		dep.setParentDep(parentDep);
	}

}
