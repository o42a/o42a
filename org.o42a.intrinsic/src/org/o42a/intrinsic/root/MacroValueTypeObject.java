/*
    Intrinsics
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = Root.class, value = "macro.o42a")
public class MacroValueTypeObject extends ValueTypeObject implements Macro {

	public MacroValueTypeObject(
			MemberOwner owner,
			AnnotatedSources sources) {
		super(owner, sources, ValueStruct.MACRO);
	}

	@Override
	public Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value().getExplicitDefinitions().upgradeScope(scope);

		if (ascendantDefinitions == null) {
			return explicitDefinitions;
		}

		return ascendantDefinitions.override(explicitDefinitions);
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref ref =
				ValueType.MACRO.constantRef(this, distribute(), this);

		return ref.toDefinitions(definitionEnv());
	}

}
