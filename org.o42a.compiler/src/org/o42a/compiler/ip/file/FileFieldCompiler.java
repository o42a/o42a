/*
    Compiler
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
package org.o42a.compiler.ip.file;

import static org.o42a.core.object.type.FieldAscendants.NO_FIELD_ASCENDANTS;

import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.source.FieldCompiler;
import org.o42a.core.source.ObjectSource;


public class FileFieldCompiler
		extends AbstractObjectCompiler
		implements FieldCompiler {

	public FileFieldCompiler(ObjectSource source, FileNode node) {
		super(source, node);
	}

	@Override
	public FieldDeclaration declare(Obj owner) {
		return getSection().getTitle().fieldDeclaration(
				getEnclosingBlock().distribute());
	}

	@Override
	public Ascendants buildAscendants(Ascendants ascendants) {
		return super.buildAscendants(ascendants)
				.declareField(NO_FIELD_ASCENDANTS);
	}

	@Override
	protected Section createSection() {

		final SectionNode[] sectionNodes = getNode().getSections();

		if (sectionNodes.length == 0) {

			final SectionNode sectionNode =
					new SectionNode(getNode().getStart(), getNode().getEnd());

			return new Section(this, sectionNode);
		}

		return new Section(this, sectionNodes[0]);
	}

}
