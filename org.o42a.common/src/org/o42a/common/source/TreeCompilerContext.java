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

import java.util.Iterator;

import org.o42a.core.source.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.io.Source;
import org.o42a.util.log.Logger;


public class TreeCompilerContext<S extends Source>
		extends CompilerContext {

	private final SourceTree<S> sourceTree;

	public TreeCompilerContext(
			CompilerContext parentContext,
			SourceTree<S> sourceTree) {
		this(parentContext, sourceTree, null);
	}

	public TreeCompilerContext(
			CompilerContext parentContext,
			SourceTree<S> sourceTree,
			Logger logger) {
		super(parentContext, logger);
		this.sourceTree = sourceTree;
		if (!sourceTree.getFileName().isValid()) {
			getLogger().error(
					"invalid_file_name",
					sourceTree.getSource(),
					"Invalid file name: %s",
					sourceTree.getFileName());
		}
	}

	public final SourceTree<S> getSourceTree() {
		return this.sourceTree;
	}

	@Override
	public S getSource() {
		return getSourceTree().getSource();
	}

	@Override
	public ModuleCompiler compileModule() {
		return getCompiler().compileModule(new TreeObjectSource<>(this));
	}

	@Override
	public FieldCompiler compileField() {
		return getCompiler().compileField(new TreeObjectSource<>(this));
	}

	@Override
	public void include(Block block) {

		final SourceCompiler compiler = getCompiler();
		final Iterator<? extends SourceTree<S>> childTrees =
				getSourceTree().subTrees();

		while (childTrees.hasNext()) {

			final SourceTree<S> tree = childTrees.next();
			final TreeDefinitionSource<S> definitionSource =
					new TreeDefinitionSource<>(this, tree);
			final DefinitionCompiler definitionCompiler =
					compiler.compileDefinition(definitionSource);

			if (definitionCompiler != null) {
				definitionCompiler.define(block);
			}
		}
	}

	protected TreeCompilerContext<S> sectionContext(SourceTree<S> sourceTree) {
		return new TreeCompilerContext<>(this, sourceTree, null);
	}

}
