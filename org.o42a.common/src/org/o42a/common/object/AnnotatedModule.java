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
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;


public abstract class AnnotatedModule extends Module {

	public static final String SOURCES_DESCRIPTOR_SUFFIX = "__SRC";

	public static AnnotatedSources moduleSources(Class<?> moduleClass) {
		try {

			final Class<?> descriptorClass =
					moduleClass.getClassLoader().loadClass(
							moduleClass.getName() + SOURCES_DESCRIPTOR_SUFFIX);

			return (AnnotatedSources) descriptorClass.newInstance();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final AnnotatedSources sources;

	public AnnotatedModule(
			CompilerContext parentContext,
			AnnotatedSources sources) {
		super(sources.getSourceTree().context(parentContext), null);
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
