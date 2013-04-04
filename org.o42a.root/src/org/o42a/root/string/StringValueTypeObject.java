/*
    Root Object Definition
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.root.string;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.root.Root;


@SourcePath(relativeTo = Root.class, value = "string.o42a")
public class StringValueTypeObject extends ValueTypeObject {

	public StringValueTypeObject(Obj owner, AnnotatedSources sources) {
		super(owner, sources, ValueType.STRING);
	}

}
