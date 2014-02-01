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
package org.o42a.backend.constant.data.struct;

import static org.o42a.backend.constant.code.op.SystemStore.allocSystemStore;
import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.op.SystemCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.DCDAlloc;
import org.o42a.backend.constant.data.TopLevelCDAlloc;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.SystemData;
import org.o42a.util.string.ID;


public final class SystemCDAlloc extends DCDAlloc<SystemOp, SystemData>  {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;

	public SystemCDAlloc(
			ContainerCDAlloc<?> enclosing,
			SystemData data,
			SystemCDAlloc typeAllocation) {
		super(enclosing.getBackend(), data, typeAllocation);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
		nest();
	}

	@Override
	public final TopLevelCDAlloc<?> getTopLevel() {
		return this.topLevel;
	}

	@Override
	public final ContainerCDAlloc<?> getEnclosing() {
		return this.enclosing;
	}

	public final CSystemType getUnderlyingType() {

		final SystemTypeCDAlloc typeAlloc =
				(SystemTypeCDAlloc) getData().getSystemType().getAllocation();

		return typeAlloc.getUnderlyingType();
	}

	@Override
	public SystemOp op(ID id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);

		return new SystemCOp(
				new OpBE<SystemOp>(id, ccode) {
					@Override
					public void prepare() {
					}
					@Override
					protected SystemOp write() {
						return getUnderlyingPtr().op(
								getId(),
								part().underlying());
					}
				},
				allocSystemStore(allocClass.allocPlace(ccode.code())),
				getUnderlyingType());
	}

	@Override
	protected SystemData allocateUnderlying(SubData<?> container) {

		final SystemData underlying = container.addSystem(
				getData().getId().getLocal().toString(),
				getUnderlyingType());

		underlying.setAttributes(getData());

		return underlying;
	}

}
