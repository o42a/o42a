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
package org.o42a.parser.grammar.file;

import static org.o42a.ast.file.SectionNode.sectionDeclaratorFromTitle;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.grammar.file.SubTitleParser.SUB_TITLE;

import java.util.ArrayList;

import org.o42a.ast.file.SubTitleNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class SectionContentParser implements Parser<ContentWithNextTitle> {

	public static final SectionContentParser SECTION_CONTENT =
			new SectionContentParser();

	private SectionContentParser() {
	}

	@Override
	public ContentWithNextTitle parse(ParserContext context) {

		final ArrayList<SentenceNode> sentences = new ArrayList<SentenceNode>();

		for (;;) {

			final SentenceNode sentence =
					context.parse(DECLARATIVE.sentence());

			if (sentence == null) {
				return createResult(sentences, null, null);
			}

			final SubTitleNode subTitle = context.parse(SUB_TITLE);

			if (subTitle == null) {
				// No subtitle after last sentence.
				sentences.add(sentence);
				continue;
			}

			final SourcePosition sentenceEnd = sentence.getEnd();
			final SentenceNode title;

			if (context.current().line() - sentenceEnd.line() > 1) {
				// No empty or pure-comment lines between title
				// and sub-title.
				sentences.add(sentence);
				title = null;
			} else if (sectionDeclaratorFromTitle(sentence) == null) {
				// Preceding sentence is not a valid title.
				context.getLogger().invalidSectionTitle(sentence);
				sentences.add(sentence);
				title = null;
			} else {
				title = sentence;
			}

			return createResult(sentences, title, subTitle);
		}
	}

	private ContentWithNextTitle createResult(
			ArrayList<SentenceNode> sentences,
			SentenceNode nextTitle,
			SubTitleNode nextSubTitle) {

		final int size = sentences.size();

		if (size == 0 && nextSubTitle == null) {
			return null;
		}

		return new ContentWithNextTitle(
				sentences.toArray(new SentenceNode[size]),
				nextTitle,
				nextSubTitle);
	}

}
