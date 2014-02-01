/*
    Standard Macros
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.lib.macros.cmp;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.core.object.Obj;
import org.o42a.lib.macros.MacrosModule;
import org.o42a.util.string.Name;


@SourcePath(relativeTo = MacrosModule.class, value = "GE.o42a")
public final class GeMacro extends AbstractComparisonMacro {

	public GeMacro(Obj owner, AnnotatedSources sources) {
		super(owner, sources, BinaryPhraseOperator.GREATER_OR_EQUAL);
	}

	@Override
	protected Name leftName() {
		return WHAT;
	}

	@Override
	protected Name rightName() {
		return THAN;
	}

}
