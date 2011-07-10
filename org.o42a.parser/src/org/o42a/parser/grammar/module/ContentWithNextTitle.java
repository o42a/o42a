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

import org.o42a.ast.module.SubTitleNode;
import org.o42a.ast.sentence.SentenceNode;


final class ContentWithNextTitle {

	private final SentenceNode[] content;
	private final SentenceNode nextTitle;
	private final SubTitleNode nextSubTitle;

	public ContentWithNextTitle(
			SentenceNode[] content,
			SentenceNode nextTitle,
			SubTitleNode nextSubTitle) {
		this.content = content;
		this.nextTitle = nextTitle;
		this.nextSubTitle = nextSubTitle;
	}

	public final SentenceNode[] getContent() {
		return this.content;
	}

	public final SentenceNode getNextTitle() {
		return this.nextTitle;
	}

	public final SubTitleNode getNextSubTitle() {
		return this.nextSubTitle;
	}

	public final boolean hasContinuation() {
		return this.nextSubTitle != null;
	}

}
