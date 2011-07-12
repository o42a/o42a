/*
    Compiler
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
package org.o42a.compiler.ip.module;

import org.o42a.ast.module.ModuleNode;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.DefinitionCompiler;
import org.o42a.core.source.DefinitionSource;


public abstract class AbstractModuleCompiler<S extends DefinitionSource>
		implements DefinitionCompiler {

	private final S source;
	private final SourceFileName fileName;
	private final ModuleNode node;

	public AbstractModuleCompiler(S source, ModuleNode node) {
		this.source = source;
		this.node = node;
		this.fileName = new SourceFileName(source.getSource());
	}

	public final S getSource() {
		return this.source;
	}

	public final SourceFileName getFileName() {
		return this.fileName;
	}

	public final ModuleNode getNode() {
		return this.node;
	}

	public final CompilerLogger getLogger() {
		return getSource().getLogger();
	}

}
