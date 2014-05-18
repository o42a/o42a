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
package org.o42a.core.st.sentence;

import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Located;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public abstract class BlockBuilder extends Located {

	public static BlockBuilder emptyBlock(LocationInfo location) {
		return new EmptyBlock(location);
	}

	public static BlockBuilder valueBlock(Ref value) {
		return new ValueBlock(value);
	}

	public BlockBuilder(CompilerContext context, LogInfo logInfo) {
		super(context, logInfo);
	}

	public BlockBuilder(LocationInfo location) {
		super(location);
	}

	public abstract void buildBlock(Block block);

	public DefinitionsBuilder definitions(ObjectToDefine object) {
		return new BlockDefinitionsBuilder(this, object);
	}

	private static final class EmptyBlock extends BlockBuilder {

		EmptyBlock(LocationInfo location) {
			super(location);
		}

		@Override
		public void buildBlock(Block block) {
		}

		@Override
		public String toString() {
			return "()";
		}

	}

	private static final class ValueBlock extends BlockBuilder {

		private final Ref value;

		ValueBlock(Ref value) {
			super(value);
			this.value = value;
		}

		@Override
		public void buildBlock(Block block) {
			block.declare(block).alternative(block).returnValue(this.value);
		}

		@Override
		public String toString() {
			return "(= " + this.value + ')';
		}

	}

	private static final class BlockDefinitionsBuilder
			implements DefinitionsBuilder {

		private final BlockBuilder blockBuilder;
		private final ObjectToDefine object;
		private DefinitionsBuilder definitionsBuilder;

		BlockDefinitionsBuilder(
				BlockBuilder blockBuilder,
				ObjectToDefine object) {
			this.blockBuilder = blockBuilder;
			this.object = object;
		}

		@Override
		public void updateMembers() {
			definitionsBuilder().updateMembers();
		}

		@Override
		public Definitions buildDefinitions() {
			return definitionsBuilder().buildDefinitions();
		}

		@Override
		public String toString() {
			if (this.blockBuilder == null) {
				return super.toString();
			}
			return this.blockBuilder.toString();
		}

		private DefinitionsBuilder definitionsBuilder() {
			if (this.definitionsBuilder != null) {
				return this.definitionsBuilder;
			}

			final DeclarativeBlock block =
					this.object.createDefinitionsBlock();

			this.definitionsBuilder =
					block.definitions(this.object.definitionsEnv());
			this.blockBuilder.buildBlock(block);

			return this.definitionsBuilder;
		}

	}

}
