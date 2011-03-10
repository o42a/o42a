/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class DebugHeader implements Content<DebugHeader.HeaderType> {

	public static final HeaderType DEBUG_HEADER_TYPE = new HeaderType();

	private final Type<?> target;

	public DebugHeader(Type<?> target) {
		this.target = target;
	}

	public final Type<?> getTarget() {
		return this.target;
	}

	@Override
	public void allocated(HeaderType instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fill(HeaderType instance) {
		// TODO Auto-generated method stub

	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

	public static final class HeaderType extends Type<Op> {

		private HeaderType() {
		}

		@Override
		public boolean isPacked() {
			return true;
		}

		@Override
		public boolean isDebugInfo() {
			return true;
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("Header");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			data.addInt32("type_code");
			data.addInt32("instance_code");
		}

	}

}
