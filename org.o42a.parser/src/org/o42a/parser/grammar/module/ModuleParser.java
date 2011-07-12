/*
    Parser
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
package org.o42a.parser.grammar.module;

import static org.o42a.parser.grammar.module.SectionContentParser.SECTION_CONTENT;
import static org.o42a.parser.grammar.module.SubTitleParser.SUB_TITLE;

import java.util.ArrayList;

import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.module.SubTitleNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public final class ModuleParser implements Parser<ModuleNode> {

	public static final ModuleParser MODULE = new ModuleParser();

	private ModuleParser() {
	}

	@Override
	public ModuleNode parse(ParserContext context) {

		final ArrayList<SectionNode> sections = new ArrayList<SectionNode>();
		SentenceNode title = null;
		SubTitleNode subTitle = context.parse(SUB_TITLE);

		for (;;) {

			final ContentWithNextTitle contentWithNextTitle =
					context.parse(SECTION_CONTENT);

			if (contentWithNextTitle == null) {
				break;
			}

			final SentenceNode[] content = contentWithNextTitle.getContent();

			if (content.length == 0 && subTitle == null && sections.isEmpty()) {
				// Do not create a very first empty section.
			} else {
				sections.add(new SectionNode(
						title,
						subTitle,
						content));
			}

			title = contentWithNextTitle.getNextTitle();
			subTitle = contentWithNextTitle.getNextSubTitle();
		}

		if (subTitle != null) {
			sections.add(new SectionNode(title, subTitle, new SentenceNode[0]));
		} else if (sections.isEmpty()) {
			return null;
		}

		final int numSections = sections.size();

		if (numSections > 1) {

			final SectionNode first = sections.get(0);

			if (first.getSubTitle() == null) {
				return new ModuleNode(
						first,
						sections.subList(1, numSections).toArray(
								new SectionNode[numSections - 1]));
			}
		}

		return new ModuleNode(
				null,
				sections.toArray(new SectionNode[numSections]));
	}

}
