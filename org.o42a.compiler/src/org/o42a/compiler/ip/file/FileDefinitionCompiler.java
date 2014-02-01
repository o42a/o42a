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

import java.util.HashMap;

import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.core.source.DefinitionSource;
import org.o42a.core.source.Location;
import org.o42a.core.source.SectionTag;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class FileDefinitionCompiler
		extends AbstractDefinitionCompiler<DefinitionSource> {

	private HashMap<SectionTag, Section> sections;

	public FileDefinitionCompiler(DefinitionSource source, FileNode node) {
		super(source, node);
	}

	@Override
	public void define(DeclarativeBlock definition, SectionTag tag) {

		final Section section = section(tag);

		if (section == null) {
			// No section with the given tag in the source.
			return;
		}

		section.declareField(definition);
	}

	@Override
	public void done() {
		for (Section section : getSections().values()) {
			section.done();
		}
		this.sections = null;
	}

	private Section section(SectionTag tag) {

		final Section section = getSections().get(tag);

		if (section != null) {
			section.use();
		}

		return section;
	}

	private HashMap<SectionTag, Section> getSections() {
		if (this.sections != null) {
			return this.sections;
		}

		final SectionNode[] sectionNodes = getNode().getSections();
		final HashMap<SectionTag, Section> sections =
				new HashMap<>(sectionNodes.length);
		SectionTitle aboveTitle = null;

		for (SectionNode sectionNode : sectionNodes) {

			final Section section = new Section(this, sectionNode, aboveTitle);

			aboveTitle = section.getTitle();
			if (!section.isValid()) {
				continue;
			}

			final SectionTag tag = section.getTag();
			final Section existing = sections.put(tag, section);

			if (existing == null) {
				continue;
			}

			sections.put(tag, existing);

			final Location location =
					section.getLocation().addAnother(existing);

			if (tag.isImplicit()) {
				getLogger().error(
						"duplicate_implicit_section",
						location,
						"Section without tag already present in this file");
			} else {
				getLogger().error(
						"duplicate_section",
						location,
						"Section '%s' already present in this file",
						tag);
			}
		}

		return this.sections = sections;
	}

}
