/*
    Abstract Syntax Tree
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
package org.o42a.ast.file;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.util.io.SourcePosition;


public class SectionNode
		extends AbstractNode
		implements BlockNode<DoubleLine> {

	public static DeclaratorNode sectionDeclaratorFromTitle(
			SentenceNode title) {
		switch (title.getType()) {
		case ISSUE:
			return null;
		case PROPOSITION:
		case CLAIM:
			break;
		}

		final AlternativeNode[] disjunction = title.getDisjunction();

		if (disjunction.length != 1) {
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			return null;
		}

		final StatementNode statement = conjunction[0].getStatement();

		if (statement instanceof DeclaratorNode) {
			return (DeclaratorNode) statement;
		}

		return null;
	}

	private final SentenceNode title;
	private final SubTitleNode subTitle;
	private final SectionTypeDefinitionNode typeDefinition;
	private final SentenceNode[] content;

	public SectionNode(
			SentenceNode title,
			SubTitleNode subTitle,
			SectionTypeDefinitionNode typeDefinition,
			SentenceNode[] content) {
		super(
				start(title, subTitle, typeDefinition, firstNode(content)),
				end(title, subTitle, typeDefinition, lastNode(content)));
		this.title = title;
		this.subTitle = subTitle;
		this.typeDefinition = typeDefinition;
		this.content = content;
	}

	public SectionNode(SourcePosition start, SourcePosition end) {
		super(start, end);
		this.title = null;
		this.subTitle = null;
		this.typeDefinition = null;
		this.content = new SentenceNode[0];
	}

	public final SentenceNode getTitle() {
		return this.title;
	}

	public final DeclaratorNode getDeclarator() {
		if (this.title == null) {
			return null;
		}
		return sectionDeclaratorFromTitle(this.title);
	}

	public final SubTitleNode getSubTitle() {
		return this.subTitle;
	}

	public final MemberRefNode getLabel() {
		return this.subTitle != null ? this.subTitle.getTag() : null;
	}

	public final SectionTypeDefinitionNode getTypeDefinition() {
		return this.typeDefinition;
	}

	@Override
	public final SignNode<DoubleLine> getOpening() {
		if (this.subTitle == null) {
			return null;
		}

		final SignNode<DoubleLine> suffix = this.subTitle.getSuffix();

		if (suffix != null) {
			return suffix;
		}

		return this.subTitle.getPrefix();
	}

	@Override
	public final SentenceNode[] getContent() {
		return this.content;
	}

	@Override
	public final SignNode<DoubleLine> getClosing() {
		return null;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitSection(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.subTitle == null) {
			if (this.content.length <= 1) {
				if (this.content.length == 0) {
					out.append("===");
				} else {
					this.content[0].printContent(out);
				}
				return;
			}
		} else {
			if (this.title != null) {
				this.title.printContent(out);
				out.append('\n');
			}
			this.subTitle.printContent(out);
			out.append('\n');
		}
		if (this.typeDefinition != null) {
			this.typeDefinition.printContent(out);
		}
		for (SentenceNode sentence : this.content) {
			sentence.printContent(out);
			out.append('\n');
		}
	}

}
