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

import static org.o42a.ast.sentence.SentenceType.PROPOSITION;
import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.addContent;
import static org.o42a.compiler.ip.Interpreter.addSentence;
import static org.o42a.core.member.field.FieldDefinition.fieldDefinition;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.ast.Node;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.module.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.compiler.ip.DefaultStatementVisitor;
import org.o42a.compiler.ip.OtherContextDistributor;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;


final class Section {

	private final AbstractModuleCompiler<?> compiler;
	private final Node node;
	private final SectionNode sectionNode;
	private final SectionTitle title;
	private final SectionTag tag;
	private LocationInfo location;

	Section(
			AbstractModuleCompiler<?> compiler,
			SectionTitle title,
			SectionNode sectionNode) {
		this.compiler = compiler;
		this.node = sectionLocation(sectionNode);
		this.title = title;
		this.sectionNode = sectionNode;
		this.tag = sectionTag(sectionNode);
	}

	public final AbstractModuleCompiler<?> getCompiler() {
		return this.compiler;
	}

	public final DefinitionSource getSource() {
		return getCompiler().getSource();
	}

	public final Node getNode() {
		return this.node;
	}

	public final SectionNode getSectionNode() {
		return this.sectionNode;
	}

	public final SectionTitle getTitle() {
		return this.title;
	}

	public final SectionTag getTag() {
		return this.tag;
	}

	public final boolean isValid() {
		return this.tag != null;
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	public final LocationInfo getLocation() {
		assert isUsed() :
			"Section never used";
		return this.location;
	}

	public final boolean isUsed() {
		return this.location != null;
	}

	public final void use() {
		assert !isUsed() :
			"Section already used";

		final CompilerContext context =
				getSource().getSectionFactory().sectionContext(
						getNode(),
						getTag());

		this.location = new Location(context, getNode());
	}

	public AscendantsDefinition ascendants(Distributor distributor) {

		final Distributor ascendantsDistributor =
				new OtherContextDistributor(getContext(), distributor);
		final SectionTitle title = getTitle();

		if (title != null) {

			final AscendantsDefinition ascendants =
					title.ascendants(ascendantsDistributor);

			if (ascendants != null) {
				return ascendants;
			}
		}

		return new AscendantsDefinition(getLocation(), ascendantsDistributor);
	}

	public void define(DeclarativeBlock definition) {

		final SectionNode header = getCompiler().getNode().getHeader();

		if (header != null) {
			addHeader(definition, header);
		}

		addContent(
				new DefaultStatementVisitor(PLAIN_IP, getContext()),
				definition,
				getSectionNode());
	}

	public void done() {
		if (!isUsed()) {
			getSource().getLogger().error(
					"redundant_section",
					getNode(),
					"Section never included");
		}
	}

	public final CompilerLogger getLogger() {
		return getSource().getLogger();
	}

	public void declareField(Declaratives statements) {
		// TODO Validate declarable vs. source file name.

		final SectionTitle title = getTitle();

		if (title == null) {
			getLogger().error(
					"missing_section_title",
					getNode(),
					"Section title is missing");
			return;
		}

		final Distributor distributor = statements.nextDistributor();
		final FieldDeclaration declaration = title.declaration(distributor);

		final FieldDefinition definition = fieldDefinition(
				getLocation(),
				ascendants(distributor),
				new SectionDefinition(this));

		statements.field(declaration, definition);
	}

	@Override
	public String toString() {
		if (this.sectionNode == null) {
			return super.toString();
		}
		return this.sectionNode.toString();
	}

	private static Node sectionLocation(SectionNode node) {

		final SubTitleNode subTitle = node.getSubTitle();

		if (subTitle == null) {
			return node;
		}

		final SentenceNode title = node.getTitle();

		if (title != null) {
			return title;
		}

		return subTitle;
	}

	private SectionTag sectionTag(SectionNode node) {

		final SubTitleNode subTitle = node.getSubTitle();

		if (subTitle == null) {
			return IMPLICIT_SECTION_TAG;
		}

		final MemberRefNode tagNode = subTitle.getTag();

		if (tagNode == null) {
			return IMPLICIT_SECTION_TAG;
		}

		return tagNode.accept(
				new SectionTagVisitor(getLogger()),
				IMPLICIT_SECTION_TAG);
	}

	private void addHeader(DeclarativeBlock definition, SectionNode header) {

		final HeaderStatementVisitor visitor =
				new HeaderStatementVisitor(getContext());

		for (SentenceNode sentence : header.getContent()) {
			if (sentence.getType() != PROPOSITION) {
				getLogger().error(
						"not_header_proposition",
						sentence.getMark(),
						"Only propositions allowed in module header");
			}
			addSentence(visitor, definition, sentence, PROPOSITION);
		}
	}

}
