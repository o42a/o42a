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
import static org.o42a.util.fn.Init.init;

import java.util.function.Function;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.util.fn.Init;


public abstract class DefinedObject extends Obj implements ObjectToDefine {

	private final Init<ObjectMemberRegistry> memberRegistry =
			init(() -> new ObjectMemberRegistry(NO_INCLUSIONS, this));
	private final Init<DefinitionsBuilder> definitionsBuilder =
			init(this::createDefinitionsBuilder);

	public DefinedObject(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	@Override
	public final ObjectMemberRegistry getMemberRegistry() {
		return this.memberRegistry.get();
	}

	@Override
	public CommandEnv definitionsEnv() {
		return definitionEnv();
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

	protected final DefinitionsBuilder definitionsBuilder(
			Function<ObjectToDefine, DefinitionsBuilder> definitions) {
		if (definitions == null) {
			return NO_DEFINITIONS_BUILDER;
		}
		return definitions.apply(this);
	}

	protected final DefinitionsBuilder blockDefinitions(BlockBuilder builder) {
		if (builder == null) {
			return NO_DEFINITIONS_BUILDER;
		}
		return builder.definitions(this);
	}

	private DefinitionsBuilder definitionsBuilder() {
		return this.definitionsBuilder.get();
	}

}
