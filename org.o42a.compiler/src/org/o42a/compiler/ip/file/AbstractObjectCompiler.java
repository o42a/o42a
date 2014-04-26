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

import org.o42a.ast.file.FileNode;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Scope;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.DeclarativeBlock;


public abstract class AbstractObjectCompiler
		extends AbstractDefinitionCompiler<ObjectSource>
		implements ObjectCompiler {

	private Section section;
	private Location location;
	private DeclarativeBlock enclosingBlock;

	public AbstractObjectCompiler(ObjectSource source, FileNode node) {
		super(source, node);
	}

	@Override
	public final CompilerContext getContext() {
		return getSource().getContext();
	}

	@Override
	public final Location getLocation() {
		if (this.location != null) {
			return this.location;
		}
		return this.location =
				new Location(getContext(), getSection().getLoggable());
	}

	public final Section getSection() {
		if (this.section != null) {
			return this.section;
		}

		this.section = createSection().useBy(getContext());
		if (this.enclosingBlock != null) {
			this.section.encloseInto(this.enclosingBlock);
		}

		return this.section;
	}

	public final DeclarativeBlock getEnclosingBlock() {
		return this.enclosingBlock;
	}

	@Override
	public void encloseInto(DeclarativeBlock enclosingBlock) {
		this.enclosingBlock = enclosingBlock;
		if (this.section != null) {
			this.section.encloseInto(enclosingBlock);
		}
	}

	@Override
	public Ascendants buildAscendants(Ascendants ascendants) {

		final Scope scope = ascendants.getScope();
		final Nesting nesting = scope.toObject().meta().getNesting();
		final TypeConsumer consumer = TypeConsumer.typeConsumer(nesting);
		final AscendantsDefinition ascendantsDefinition =
				getSection().ascendants(consumer);

		return ascendantsDefinition.updateAscendants(ascendants);
	}

	@Override
	public void define(Block definition) {
		getSection().define(definition);
	}

	protected abstract Section createSection();

}
