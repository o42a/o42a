/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;
import org.o42a.util.io.SourceFileName;
import org.o42a.util.log.LogInfo;


public final class FileModuleCompiler
		extends AbstractObjectCompiler
		implements ModuleCompiler {

	private String moduleName;

	public FileModuleCompiler(ObjectSource source, FileNode node) {
		super(source, node);
	}

	@Override
	public String getModuleName() {
		if (this.moduleName == null) {
			getSection();
		}
		return this.moduleName;
	}

	@Override
	public Ascendants buildAscendants(Ascendants ascendants) {

		final Ascendants result = super.buildAscendants(ascendants);

		if (!result.isEmpty()) {
			return result;
		}

		return result.setAncestor(ValueType.VOID.typeRef(
				new Location(getContext(), getSection().getTitle()),
				result.getScope().getEnclosingScope()));
	}

	@Override
	public void define(DeclarativeBlock definition, SectionTag tag) {
		assert tag.isImplicit() :
			"Section tag ignored in object definition";
		getSection().define(definition);
	}

	@Override
	protected Section createSection() {
		validateFileName();

		final SectionNode[] sectionNodes = getNode().getSections();

		if (sectionNodes.length == 0) {
			this.moduleName = nameByFile();

			final SectionNode sectionNode =
					new SectionNode(getNode().getStart(), getNode().getEnd());

			return new Section(this, sectionNode);
		}

		if (sectionNodes.length > 1) {
			getLogger().error(
					"redundant_module_section",
					SectionTitle.titleLoggable(sectionNodes[1]),
					"Module should not contain more than one section");
		}

		final Section section = new Section(this, sectionNodes[0]);

		if (!section.getTag().isImplicit()) {
			getLogger().error(
					"prohibited_module_section_tag",
					section.getSectionNode().getSubTitle().getTag(),
					"Module section should not be tagged");
		}

		this.moduleName = moduleName(section.getTitle());

		return section;
	}

	private void validateFileName() {

		final SourceFileName fileName = getFileName();

		if (!fileName.isValid()) {
			// No additional testing to invalid file name.
			return;
		}
		if (fileName.isAdapter() || fileName.isOverride()) {
			getLogger().warning(
					"invalid_module_file",
					getSource().getSource(),
					"Module file should have a module name");
		}
	}

	private String moduleName(SectionTitle title) {
		if (title.isImplicit() || !title.isValid()) {
			return nameByFile();
		}

		final DeclaratorNode declaratorNode = title.getDeclaratorNode();

		if (declaratorNode.getTarget() != DeclarationTarget.VALUE) {
			getLogger().error(
					"invalid_module_declaration",
					declaratorNode.getDefinitionAssignment(),
					"Module can only declare new object (':='). "
					+ "It can't declare prototype or override something");
		}
		if (declaratorNode.getDefinitionKind() != null) {
			switch (declaratorNode.getDefinitionKind()) {
			case LINK:
				getLogger().error(
						"prohibited_module_link",
						declaratorNode.getInterface(),
						"Module is object. It can't be a link");
				break;
			case VARIABLE:
				getLogger().error(
						"prohibited_module_variable",
						declaratorNode.getInterface(),
						"Module is object. It can't be variable");
				break;
			default:
				throw new IllegalStateException(
						"Unknown definition kind: "
						+ declaratorNode.getDefinitionKind());
			}
		}

		final DeclarableNode declarableNode = declaratorNode.getDeclarable();

		if (!(declarableNode instanceof MemberRefNode)) {
			invalidDeclarable(declarableNode);
			return nameByFile();
		}

		final MemberRefNode fieldNode = (MemberRefNode) declarableNode;
		final NameNode nameNode = fieldNode.getName();

		if (nameNode == null) {
			// Invalid field name.
			return nameByFile();
		}
		if (fieldNode.getOwner() != null) {
			invalidDeclarable(fieldNode.getOwner());
		} else if (fieldNode.getDeclaredIn() != null) {
			invalidDeclarable(fieldNode.getDeclaredIn());
		}

		return nameNode.getName();
	}

	private final String nameByFile() {

		final String fieldName = getFileName().getFieldName();

		return fieldName != null ? fieldName : "module";
	}

	private void invalidDeclarable(LogInfo location) {
		getLogger().error(
				"invalid_module_title",
				location,
				"Module title should contain declaration of field "
				+ "with module name");
	}

}
