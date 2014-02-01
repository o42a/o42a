/*
    Compiler
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
package org.o42a.compiler.ip.file;

import org.o42a.ast.file.FileNode;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.DefinitionCompiler;
import org.o42a.core.source.DefinitionSource;
import org.o42a.util.io.SourceFileName;


public abstract class AbstractDefinitionCompiler<S extends DefinitionSource>
		implements DefinitionCompiler {

	private final S source;
	private final FileNode node;

	public AbstractDefinitionCompiler(S source, FileNode node) {
		this.source = source;
		this.node = node;
	}

	public final S getSource() {
		return this.source;
	}

	public final SourceFileName getFileName() {
		return getSource().getFileName();
	}

	public final FileNode getNode() {
		return this.node;
	}

	public final CompilerLogger getLogger() {
		return getSource().getLogger();
	}

}
