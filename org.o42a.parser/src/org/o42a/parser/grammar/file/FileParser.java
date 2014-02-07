/*
    Parser
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
package org.o42a.parser.grammar.file;

import static org.o42a.parser.grammar.file.SectionContentParser.HEADER_CONTENT;
import static org.o42a.parser.grammar.file.SectionContentParser.SECTION_CONTENT;

import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.ast.file.SubTitleNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public final class FileParser implements Parser<FileNode> {

	public static final FileParser FILE = new FileParser();

	private FileParser() {
	}

	@Override
	public FileNode parse(ParserContext context) {

		final ContentWithNextTitle header = context.parse(HEADER_CONTENT);

		if (header == null) {
			return null;
		}

		final SubTitleNode subTitle = header.getNextSubTitle();

		if (subTitle != null) {

			final ContentWithNextTitle content = context.parse(SECTION_CONTENT);

			if (content != null) {

				final SectionNode headerSection;

				if (header.getContent().length == 0) {
					headerSection = null;
				} else {
					headerSection = new SectionNode(
							null,
							null,
							null,
							header.getContent());
				}

				return new FileNode(
						headerSection,
						new SectionNode(
								header.getNextTitle(),
								subTitle,
								content.getTypeDefinition(),
								content.getContent()));
			}
		}

		return new FileNode(
				null,
				new SectionNode(
						header.getNextTitle(),
						subTitle,
						header.getTypeDefinition(),
						header.getContent()));
	}

}
