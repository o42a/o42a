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

import org.o42a.ast.FixedPosition;
import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.DeclarableNode;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public final class ObjectModuleCompiler
		extends AbstractModuleCompiler<ObjectSource>
		implements ObjectCompiler {

	private Section section;
	private Loggable loggable;

	public ObjectModuleCompiler(ObjectSource source, ModuleNode node) {
		super(source, node);
	}

	@Override
	public final CompilerContext getContext() {
		return getSource().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		if (this.loggable != null) {
			return this.loggable;
		}
		return this.loggable = getSection().getNode().getLoggable();
	}

	@Override
	public Ascendants buildAscendants(Ascendants ascendants) {

		final Distributor distributor =
				ascendants.getScope().getEnclosingScope().distribute();
		final AscendantsDefinition ascendantsDefinition =
				getSection().getTitle().ascendants(distributor);

		return ascendantsDefinition.updateAscendants(ascendants);
	}

	@Override
	public void define(DeclarativeBlock definition, SectionTag tag) {
		assert tag.isImplicit() :
			"Section tag ignored in object definition";
		getSection().define(definition);
	}

	@Override
	public void done() {
		getLoggable();
		this.section = null;
	}

	private Section getSection() {
		if (this.section != null) {
			return this.section;
		}

		validateFileName();

		final SectionNode[] sectionNodes = getNode().getSections();

		if (sectionNodes.length == 0) {
			return this.section =
					new Section(this, new SectionNode(getNode())).use();
		}

		if (sectionNodes.length > 1) {
			getLogger().error(
					"redundant_section",
					SectionTitle.node(sectionNodes[1]),
					"Module should not contain more than one section");
		}

		final Section section = new Section(this, sectionNodes[0]);

		if (!section.getTag().isImplicit()) {
			getLogger().error(
					"prohibited_section_tag",
					section.getSectionNode().getSubTitle().getTag(),
					"Module section should not be tagged");
		}

		validateTitle(section.getTitle());

		return this.section = section.use();
	}

	private void validateFileName() {

		final SourceFileName fileName = getFileName();

		if (!fileName.isValid()) {
			// No additional testing to invalid file name.
			return;
		}
		if (fileName.isAdapter() || fileName.isOverride()) {
			getLogger().warning(
					"invalid_module_file_name",
					new FixedPosition(getSource().getSource()),
					"Module file should have a module name");
		}
	}

	private void validateTitle(SectionTitle title) {
		if (title.isImplicit() || !title.isValid()) {
			return;
		}

		final DeclarableNode declarableNode =
				title.getDeclaratorNode().getDeclarable();

		if (!(declarableNode instanceof MemberRefNode)) {
			invalidDeclarable(declarableNode);
			return;
		}

		final MemberRefNode fieldNode = (MemberRefNode) declarableNode;

		if (fieldNode.getName() == null) {
			// Invalid field name.
			return;
		}
		if (fieldNode.getOwner() != null) {
			invalidDeclarable(fieldNode.getOwner());
			return;
		}
		if (fieldNode.getDeclaredIn() != null) {
			invalidDeclarable(fieldNode.getDeclaredIn());
			return;
		}
	}

	private void invalidDeclarable(LogInfo location) {
		getLogger().error(
				"invalid_module_title",
				location,
				"Module title should contain declaration of field, "
				+ "which name is the same as module name");
	}

}
