/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.ast.sentence.SentenceType.PROPOSITION;
import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.file.OtherContextDistributor.distributeIn;
import static org.o42a.compiler.ip.st.StInterpreter.addContent;
import static org.o42a.compiler.ip.st.StInterpreter.addSentence;
import static org.o42a.compiler.ip.type.def.TypeDefinition.redundantTypeParameters;
import static org.o42a.compiler.ip.type.def.TypeDefinition.typeDefinition;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.ast.file.SectionNode;
import org.o42a.ast.file.SectionTypeDefinitionNode;
import org.o42a.ast.file.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.compiler.ip.field.FieldNesting;
import org.o42a.compiler.ip.st.DefaultStatementVisitor;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.def.TypeDefinition;
import org.o42a.core.Distributor;
import org.o42a.core.Namespace;
import org.o42a.core.member.field.*;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


final class Section implements LogInfo {

	private final AbstractDefinitionCompiler<?> compiler;
	private final Loggable loggable;
	private SectionNode sectionNode;
	private final SectionTitle title;
	private final SectionTag tag;
	private Location location;
	private DeclarativeBlock enclosingBlock;

	Section(
			AbstractDefinitionCompiler<?> compiler,
			SectionNode sectionNode) {
		this(compiler, sectionNode, null);
	}

	Section(
			AbstractDefinitionCompiler<?> compiler,
			SectionNode sectionNode,
			SectionTitle aboveTitle) {
		this.compiler = compiler;
		this.sectionNode = sectionNode;
		this.title = new SectionTitle(this, aboveTitle);
		this.loggable = sectionLoggable(sectionNode);
		this.tag = sectionTag(sectionNode);
	}

	public final AbstractDefinitionCompiler<?> getCompiler() {
		return this.compiler;
	}

	public final DefinitionSource getSource() {
		return getCompiler().getSource();
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
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
		return getTag() != null && getTitle().isValid();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	public final Location getLocation() {
		assert isUsed() :
			"Section never used";
		return this.location;
	}

	public final boolean isUsed() {
		return this.location != null;
	}

	public final Section use() {
		return useBy(getSource().getSectionFactory().sectionContext(
				this,
				getTag()));
	}

	public final Section useBy(CompilerContext context) {
		assert !isUsed() :
			"Section already used: " + this;

		this.location = new Location(context, this);

		return this;
	}

	public void encloseInto(DeclarativeBlock enclosingBlock) {
		assert this.enclosingBlock == null :
			"Section already enclosed into block";
		this.enclosingBlock = enclosingBlock;
		addHeader(enclosingBlock);
	}

	public AscendantsDefinition ascendants(TypeConsumer consumer) {

		final Distributor ascendantsDistributor =
				distributeIn(this.enclosingBlock.distribute(), getContext());
		final SectionTypeDefinitionNode typeDefinitionNode =
				getSectionNode().getTypeDefinition();
		final SectionTitle title = getTitle();
		AscendantsDefinition ascendants = null;

		if (title != null) {
			ascendants = title.ascendants(ascendantsDistributor, consumer);
			if (ascendants != null) {
				if (typeDefinitionNode != null
						&& ascendants.getTypeParameters() != null) {
					redundantTypeParameters(
							getLogger(),
							ascendants.getTypeParameters().getLocation());
				}
			}
		}
		if (ascendants == null) {
			ascendants = new AscendantsDefinition(
					getLocation(),
					ascendantsDistributor);
		}
		if (typeDefinitionNode != null) {

			final TypeDefinition typeDefinition = typeDefinition(
					typeDefinitionNode,
					this.enclosingBlock.getContext(),
					typeDefinitionNode.getContent());

			if (typeDefinition != null) {
				ascendants = ascendants.setTypeParameters(typeDefinition);
			}
		}

		return ascendants;
	}

	public void define(DeclarativeBlock definition) {
		addContent(
				new DefaultStatementVisitor(PLAIN_IP, getContext()),
				definition,
				getSectionNode());
		this.sectionNode = null;
	}

	public void done() {
		if (!isUsed()) {
			getSource().getLogger().error(
					"redundant_section",
					this,
					"Section never included");
		}
	}

	public final CompilerLogger getLogger() {
		return getSource().getLogger();
	}

	public void declareField(DeclarativeBlock definition) {

		final DeclarativeBlock enclosingBlock = enclosingBlock(definition);
		final LocationInfo location = getLocation();
		final Declaratives statements =
				enclosingBlock.propose(location).alternative(location);

		final Distributor distributor = statements.nextDistributor();
		final FieldDeclaration fieldDeclaration =
				getTitle().fieldDeclaration(distributor);

		if (fieldDeclaration == null) {
			return;
		}

		final FieldDefinition fieldDefinition =
				ascendants(new FieldNesting(fieldDeclaration).toTypeConsumer())
				.fieldDefinition(
						getLocation(),
						new SectionDefinition(this));

		final FieldBuilder fieldBuilder =
				statements.field(fieldDeclaration, fieldDefinition);

		if (fieldBuilder != null) {
			statements.statement(fieldBuilder.build());
		}
	}

	private DeclarativeBlock enclosingBlock(DeclarativeBlock definition) {

		final LocationInfo location = getLocation();
		final Declaratives statements =
				definition.propose(location).alternative(location);
		final Namespace namespace =
				new Namespace(location, statements.nextContainer());
		final DeclarativeBlock enclosingBlock =
				statements.parentheses(location, namespace);

		encloseInto(enclosingBlock);

		return enclosingBlock;
	}

	@Override
	public String toString() {
		if (this.loggable == null) {
			return super.toString();
		}
		return "Section[" + this.tag + "]:" + this.loggable;
	}

	private static Loggable sectionLoggable(SectionNode node) {

		final SubTitleNode subTitle = node.getSubTitle();

		if (subTitle == null) {
			return node.getLoggable();
		}

		final SentenceNode title = node.getTitle();

		if (title != null) {
			return title.getLoggable();
		}

		return subTitle.getLoggable();
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

	private void addHeader(DeclarativeBlock enclosingBlock) {

		final SectionNode header = getCompiler().getNode().getHeader();

		if (header == null) {
			return;
		}

		final HeaderStatementVisitor visitor =
				new HeaderStatementVisitor(getContext());

		for (SentenceNode sentence : header.getContent()) {
			if (sentence.getType() != PROPOSITION) {
				getLogger().error(
						"not_header_proposition",
						sentence.getMark(),
						"Only propositions allowed in file header");
			}
			addSentence(visitor, enclosingBlock, sentence, PROPOSITION);
		}

		enclosingBlock.executeInstructions();
	}

}
