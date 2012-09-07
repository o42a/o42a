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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.source.LocationInfo;


public class KeptLocalValue extends PathFragment {

	private final Ref value;
	private final LocationInfo location;

	public KeptLocalValue(LocationInfo location, Ref value) {
		assert value.getScope().toLocal() != null :
			value.getScope() + " is not a local scope";
		this.location = location;
		this.value = value;
	}

	public final LocationInfo getLocation() {
		return this.location;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new KeptLocalValueDefinition(this);
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		expander.getLogger().error(
				"redundant_local_keeper",
				getLocation(),
				"Local value can not be kept inside an imperative block."
				+ " Consider to use a local field instead");
		return null;
	}

	public final Ref toRef() {
		return toPath()
				.bind(getLocation(), getValue().getScope())
				.target(getValue().distribute());
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "//" + this.value;
	}

}
