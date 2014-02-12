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

import static org.o42a.ast.file.SectionNode.sectionDeclaratorFromTitle;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.grammar.file.SubTitleParser.SUB_TITLE;

import java.util.ArrayList;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.file.HashLine;
import org.o42a.ast.file.SectionTypeDefinitionNode;
import org.o42a.ast.file.SubTitleNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class SectionContentParser implements Parser<ContentWithNextTitle> {

	public static final SectionContentParser HEADER_CONTENT =
			new SectionContentParser(true);
	public static final SectionContentParser SECTION_CONTENT =
			new SectionContentParser(false);

	private static final HashLineParser HASH_LINE = new HashLineParser();

	private final boolean header;

	private SectionContentParser(boolean header) {
		this.header = header;
	}

	@Override
	public ContentWithNextTitle parse(ParserContext context) {

		boolean header = this.header;
		final ArrayList<SentenceNode> sentences = new ArrayList<>();
		SectionTypeDefinitionNode typeDefinition = null;

		for (;;) {
			if (header) {

				final ContentWithNextTitle result1 =
						parseSubTitle(context, sentences);

				if (result1 != null) {
					return result1;
				}

				typeDefinition = parseTypeDefinition(context, sentences);
				if (typeDefinition != null) {
					header = false;
				}
			}

			if (typeDefinition == null) {
				typeDefinition = parseTypeDefinition(context, sentences);
			}

			final SentenceNode sentence =
					context.parse(DECLARATIVE.sentence());

			if (sentence == null) {
				// No title.
				return createResult(typeDefinition, sentences, null, null);
			}

			sentences.add(sentence);
		}
	}

	private ContentWithNextTitle parseSubTitle(
			ParserContext context,
			ArrayList<SentenceNode> sentences) {

		final SubTitleNode subTitle = context.parse(SUB_TITLE);

		if (subTitle == null) {
			return null;
		}

		final int numSentences = sentences.size();

		if (numSentences == 0) {
			return createResult(null, sentences, null, subTitle);
		}

		final SentenceNode lastSentence = sentences.remove(numSentences - 1);
		final SentenceNode title;

		if (subTitle.getStart().getLine()
				- lastSentence.getEnd().getLine() > 1) {
			// Empty or pure-comment lines between title and sub-title.
			sentences.add(lastSentence);
			title = null;
		} else if (sectionDeclaratorFromTitle(lastSentence) == null) {
			// Preceding sentence is not a valid title.
			context.getLogger().error(
					"invalid_section_title",
					lastSentence,
					"Section title should be a declarative sentence"
					+ " with a single field declaration");
			sentences.add(lastSentence);
			title = null;
		} else {
			title = lastSentence;
		}

		return createResult(null, sentences, title, subTitle);
	}

	private SectionTypeDefinitionNode parseTypeDefinition(
			ParserContext context,
			ArrayList<SentenceNode> sentences) {

		final SignNode<HashLine> hashLine = context.parse(HASH_LINE);

		if (hashLine == null) {
			return null;
		}

		final SectionTypeDefinitionNode typeDefinition =
				new SectionTypeDefinitionNode(
						sentences.toArray(
								new SentenceNode[sentences.size()]),
						hashLine);

		sentences.clear();

		return typeDefinition;
	}

	private ContentWithNextTitle createResult(
			SectionTypeDefinitionNode typeDefinition,
			ArrayList<SentenceNode> sentences,
			SentenceNode nextTitle,
			SubTitleNode nextSubTitle) {

		final int size = sentences.size();

		if (size == 0 && nextSubTitle == null && typeDefinition == null) {
			return null;
		}

		return new ContentWithNextTitle(
				typeDefinition,
				sentences.toArray(new SentenceNode[size]),
				nextTitle,
				nextSubTitle);
	}

	private static final class HashLineParser extends LineParser<HashLine> {

		HashLineParser() {
			super('#', 3);
		}

		@Override
		protected HashLine createLine(int length) {
			return new HashLine(length);
		}

	}

}
