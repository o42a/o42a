/*
    Compiler Commons
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
package org.o42a.common.source;

import org.o42a.core.source.*;
import org.o42a.util.io.Source;
import org.o42a.util.io.SourceFileName;
import org.o42a.util.log.LogInfo;


class TreeDefinitionSource<S extends Source>
		implements DefinitionSource, SectionFactory {

	private final TreeCompilerContext<S> parentContext;
	private final SourceTree<S> sourceTree;

	public TreeDefinitionSource(
			TreeCompilerContext<S> parentContext,
			SourceTree<S> sourceTree) {
		this.parentContext = parentContext;
		this.sourceTree = sourceTree;
	}

	public final TreeCompilerContext<S> getParentContext() {
		return this.parentContext;
	}

	@Override
	public final S getSource() {
		return this.sourceTree.getSource();
	}

	@Override
	public final SourceFileName getFileName() {
		return this.sourceTree.getFileName();
	}

	@Override
	public final SectionFactory getSectionFactory() {
		return this;
	}

	@Override
	public final CompilerLogger getLogger() {
		return getParentContext().getLogger();
	}

	@Override
	public TreeCompilerContext<S> sectionContext(
			LogInfo location,
			SectionTag tag) {
		return getParentContext().sectionContext(tag, this.sourceTree);
	}

	@Override
	public String toString() {

		final Source source = getSource();

		if (source == null) {
			return super.toString();
		}

		return source.toString();
	}

}
