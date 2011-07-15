/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.io.Source;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Logger;


public class TreeCompilerContext<S extends Source>
		extends CompilerContext {

	private final SourceTree<S> sourceTree;
	private final SectionTag sectionTag;

	public TreeCompilerContext(
			CompilerContext parentContext,
			SourceTree<S> sourceTree,
			Logger logger) {
		super(parentContext, logger);
		this.sectionTag = IMPLICIT_SECTION_TAG;
		this.sourceTree = sourceTree;
	}

	protected TreeCompilerContext(
			TreeCompilerContext<S> parentContext,
			SourceTree<S> sourceTree,
			SectionTag sectionTag) {
		super(parentContext, null);
		this.sectionTag = sectionTag;
		this.sourceTree = sourceTree;
	}

	public final SourceTree<S> getSourceTree() {
		return this.sourceTree;
	}

	@Override
	public S getSource() {
		return getSourceTree().getSource();
	}

	@Override
	public final SectionTag getSectionTag() {
		return this.sectionTag;
	}

	public ObjectCompiler compileObject() {
		return getCompiler().compileObject(new TreeObjectSource<S>(this));
	}

	@Override
	public void include(DeclarativeBlock block, SectionTag tag) {

		final SourceCompiler compiler = getCompiler();

		for (SourceTree<S> tree : getSourceTree().childTrees()) {

			final TreeDefinitionSource<S> definitionSource =
					new TreeDefinitionSource<S>(this, tree);
			final DefinitionCompiler definitionCompiler =
					compiler.compileDefinition(definitionSource);

			definitionCompiler.define(block, tag);
		}
	}

	@Override
	@Deprecated
	public CompilerContext contextFor(String path) throws Exception {
		throw new UnsupportedOperationException();
	}

	protected TreeCompilerContext<S> sectionContext(
			LogInfo location,
			SectionTag tag,
			SourceTree<S> sourceTree) {
		return new TreeCompilerContext<S>(this, sourceTree, tag);
	}

}
