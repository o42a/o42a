/*
    Compiler Commons
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.common.object;

import org.o42a.common.source.URLSourceTree;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;


public class AnnotatedObject extends CompiledObject {

	private final AnnotatedSources sources;

	public AnnotatedObject(Obj owner, AnnotatedSources sources) {
		super(compileField(owner, sources.getSourceTree()));
		this.sources = sources;
	}

	public final AnnotatedSources getSources() {
		return this.sources;
	}

	public final URLSourceTree getSourceTree() {
		return getSources().getSourceTree();
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		super.declareMembers(members);
		for (Field field : getSources().fields(this)) {
			members.addMember(field.toMember());
		}
	}

}
