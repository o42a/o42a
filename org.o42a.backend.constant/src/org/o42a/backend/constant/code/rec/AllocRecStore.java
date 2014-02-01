/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.rec;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.InstrBE;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.data.AllocPlace;


final class AllocRecStore extends RecStore {

	AllocRecStore(AllocPlace allocPlace) {
		super(allocPlace);
	}

	@Override
	public <O extends Op> void store(
			InstrBE instr,
			RecCOp<?, O, ?> rec,
			OpBE<O> value) {
		instr.alwaysEmit();
		instr.use(rec);
		instr.use(value);
	}

	@Override
	public <O extends Op> void load(RecCOp<?, O, ?> rec, OpBE<O> value) {
		value.use(rec);
	}

	@Override
	protected Usable<SimpleUsage> init(
			RecCOp<?, ?, ?> rec,
			Usable<SimpleUsage> allUses) {
		return allUses;
	}

}
