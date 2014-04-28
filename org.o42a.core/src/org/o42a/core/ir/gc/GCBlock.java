/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.gc;

import static org.o42a.core.ir.gc.GCDescOp.GC_DESC_TYPE;

import java.util.function.Supplier;

import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Type;


public final class GCBlock implements Content<GCBlockOp.Type> {

	private final Type<?> type;
	private final String gcDecscriptor;

	public GCBlock(Type<?> type, String gcDecscriptor) {
		assert type != null :
			"Data type not specified";
		assert gcDecscriptor != null :
			"GC descriptor name not specified";
		this.type = type;
		this.gcDecscriptor = gcDecscriptor;
	}

	@Override
	public void allocated(GCBlockOp.Type instance) {
	}

	@Override
	public void fill(GCBlockOp.Type instance) {
		instance.lock().setValue((byte) 0);
		instance.list().setValue((byte) 0);
		instance.flags().setValue((short) 0);
		instance.useCount().setValue(0);
		instance.desc().setConstant(true).setValue(
				instance.getGenerator()
				.externalGlobal()
				.setConstant()
				.link(this.gcDecscriptor, GC_DESC_TYPE));
		instance.prev().setNull();
		instance.next().setNull();
		instance.size()
		.setConstant(true)
		.setLowLevel(true)
		.setValue(new Supplier<Integer>() {
			@Override
			public Integer get() {
				return GCBlock.this.type.layout(
						GCBlock.this.type.getGenerator()).size();
			}
		});
	}

	@Override
	public String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return "GCBlock[" + this.type + ": " + this.gcDecscriptor + ']';
	}

}
