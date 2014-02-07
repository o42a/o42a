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
import org.o42a.ast.file.SectionNode;
import org.o42a.core.source.DefinitionSource;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class FileDefinitionCompiler
		extends AbstractDefinitionCompiler<DefinitionSource> {

	private Section section;

	public FileDefinitionCompiler(DefinitionSource source, FileNode node) {
		super(source, node);
	}

	public Section getSection() {
		if (this.section != null) {
			return this.section;
		}

		this.section = compile();

		return this.section;
	}

	@Override
	public void define(DeclarativeBlock definition) {

		final Section section = getSection();

		if (section == null) {
			// No section with the given tag in the source.
			return;
		}
		section.use();

		section.declareField(definition);
	}

	private Section compile() {

		final SectionNode[] sectionNodes = getNode().getSections();

		if (sectionNodes.length == 0) {

			final SectionNode sectionNode =
					new SectionNode(getNode().getStart(), getNode().getEnd());

			return new Section(this, sectionNode);
		}

		return new Section(this, sectionNodes[0]);
	}

}
