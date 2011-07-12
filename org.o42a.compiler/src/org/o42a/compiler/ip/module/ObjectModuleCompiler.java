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
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.log.Loggable;


public final class ObjectModuleCompiler
		extends AbstractModuleCompiler<ObjectSource>
		implements ObjectCompiler {

	public ObjectModuleCompiler(ObjectSource source, ModuleNode node) {
		super(source, node);
	}

	@Override
	public final CompilerContext getContext() {
		return getSource().getContext();
	}

	@Override
	public Loggable getLoggable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Ascendants buildAscendants(Ascendants ascendants) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void define(DeclarativeBlock definition, SectionTag tag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void done() {
		// TODO Auto-generated method stub

	}

}
