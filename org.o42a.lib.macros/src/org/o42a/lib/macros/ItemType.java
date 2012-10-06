/*
    Standard Macros
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
package org.o42a.lib.macros;

import org.o42a.common.macro.AnnotatedMacro;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.macro.MacroExpander;
import org.o42a.util.log.LogInfo;


@SourcePath(relativeTo = MacrosModule.class, value = "item_type.o42a")
final class ItemType extends AnnotatedMacro {

	private Ref array;

	ItemType(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Path expand(MacroExpander expander) {
		return itemType(expander);
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return itemType(expander);
	}

	private Path itemType(MacroExpander expander) {

		final ArrayValueStruct arrayStruct = arrayStruct(expander);

		if (arrayStruct == null) {
			return null;
		}

		return arrayTypeRef(expander, arrayStruct).getPath().getPath();
	}

	private ArrayValueStruct arrayStruct(MacroExpander expander) {

		final Scope scope = expander.getMacroObject().getScope();
		final Obj target =
				array()
				.upgradeScope(scope)
				.resolve(scope.resolver())
				.toObject();

		if (target == null) {
			// Log the error unconditionally.
			notArray(expander, expander.getExplicitLogger());
			return null;
		}

		final ArrayValueStruct arrayStruct =
				target.value().getValueStruct().toArrayStruct();

		if (arrayStruct == null) {
			// Conditionally report the error.
			notArray(expander, expander.getLogger());
			return null;
		}

		return arrayStruct;
	}

	private TypeRef arrayTypeRef(
			MacroExpander expander,
			ArrayValueStruct arrayStruct) {

		final Scope scope = expander.getMacroObject().getScope();
		final PrefixPath prefix =
				array().getPath().rebuildIn(scope).toPrefix(scope);

		return arrayStruct.getItemTypeRef().prefixWith(prefix);
	}

	private Ref array() {
		if (this.array != null) {
			return this.array;
		}
		return this.array = ArraySubjectDep.arrayRef(this);
	}

	private void notArray(LogInfo location, CompilerLogger logger) {
		logger.error(
				"not_array_item_type",
				location,
				"Can only obtain item type from array or row");
	}

}
