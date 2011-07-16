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

import org.o42a.ast.module.ModuleNode;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.log.Loggable;


public abstract class AbstractObjectCompiler
		extends AbstractDefinitionCompiler<ObjectSource>
		implements ObjectCompiler {

	private Section section;
	private Loggable loggable;

	public AbstractObjectCompiler(ObjectSource source, ModuleNode node) {
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

	public final Section getSection() {
		if (this.section != null) {
			return this.section;
		}
		return this.section = createSection().useBy(getContext());
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

	protected abstract Section createSection();

}
