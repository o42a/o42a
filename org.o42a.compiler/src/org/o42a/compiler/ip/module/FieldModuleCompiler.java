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

import static org.o42a.core.Distributor.declarativeDistributor;

import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.source.FieldCompiler;
import org.o42a.core.source.ObjectSource;


public class FieldModuleCompiler
		extends AbstractObjectCompiler
		implements FieldCompiler {

	public FieldModuleCompiler(ObjectSource source, ModuleNode node) {
		super(source, node);
	}

	@Override
	public FieldDeclaration declare(MemberOwner owner) {
		return getSection().getTitle().fieldDeclaration(
				declarativeDistributor(owner.getContainer()));
	}

	@Override
	protected Section createSection() {

		final SectionNode[] sectionNodes = getNode().getSections();

		if (sectionNodes.length == 0) {
			return new Section(this, new SectionNode(getNode())).use();
		}

		if (sectionNodes.length > 1) {
			getLogger().error(
					"redundant_field_section",
					SectionTitle.node(sectionNodes[1]),
					"Field source filed should not contain"
					+ " more than one section");
		}

		final Section section = new Section(this, sectionNodes[0]);

		if (!section.getTag().isImplicit()) {
			getLogger().error(
					"prohibited_field_section_tag",
					section.getSectionNode().getSubTitle().getTag(),
					"Field section should not be tagged");
		}

		return section;
	}

}
