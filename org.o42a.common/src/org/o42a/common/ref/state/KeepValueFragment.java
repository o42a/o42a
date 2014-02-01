/*
    Compiler Commons
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
package org.o42a.common.ref.state;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundFragment;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.type.TypeRef;


public class KeepValueFragment extends BoundFragment {

	private final Ref value;

	public KeepValueFragment(Ref value) {
		this.value = value;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		return this.value.toStateful().getPath().getPath();
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new StatefulFieldDefinition(ref, this.value);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return this.value.getInterface();
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "\\\\" + this.value;
	}

}
