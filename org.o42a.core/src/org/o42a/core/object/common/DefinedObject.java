/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.object.common;

import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;
import static org.o42a.core.object.def.DefinitionsBuilder.NO_DEFINITIONS_BUILDER;

import java.util.function.Consumer;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


public abstract class DefinedObject extends Obj {

	private ObjectMemberRegistry memberRegistry;
	private DefinitionsBuilder definitionsBuilder;

	public DefinedObject(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	public ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new ObjectMemberRegistry(NO_INCLUSIONS, this);
		}
		return this.memberRegistry;
	}

	@Override
	protected Definitions explicitDefinitions() {
		return definitionsBuilder().buildDefinitions();
	}

	protected abstract DefinitionsBuilder createDefinitionsBuilder();

	@Override
	protected void declareMembers(ObjectMembers members) {
		getMemberRegistry().registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		definitionsBuilder().updateMembers();
	}

	protected final DefinitionsBuilder blockDefinitions(BlockBuilder builder) {
		if (builder == null) {
			return NO_DEFINITIONS_BUILDER;
		}
		return blockDefinitions(builder::buildBlock);
	}

	protected final DefinitionsBuilder blockDefinitions(
			Consumer<DeclarativeBlock> definition) {

		final DeclarativeBlock block =
				new DeclarativeBlock(this, this, getMemberRegistry());
		final DefinitionsBuilder builder =
				block.definitions(definitionEnv());

		definition.accept(block);

		return builder;
	}

	private DefinitionsBuilder definitionsBuilder() {
		if (this.definitionsBuilder != null) {
			return this.definitionsBuilder;
		}
		return this.definitionsBuilder = createDefinitionsBuilder();
	}

}
