/*
    Root Object Definition
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.directive.Directive;


@SourcePath(relativeTo = Root.class, value = "directive.o42a")
public class DirectiveValueTypeObject
		extends ValueTypeObject
		implements Directive {

	public DirectiveValueTypeObject(Obj owner, AnnotatedSources sources) {
		super(owner, sources, ValueType.DIRECTIVE);
	}

	@Override
	public Definitions overrideDefinitions(Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value()
				.getExplicitDefinitions()
				.upgradeScope(ascendantDefinitions.getScope());

		return ascendantDefinitions.override(explicitDefinitions);
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref ref =
				ValueType.DIRECTIVE.constantRef(this, distribute(), this);

		return ref.toDefinitions(definitionEnv());
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {
		context.getResolver().getLogger().error(
				"indefinite_directive",
				directive,
				"Indefinite directive");
	}

}
