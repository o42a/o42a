/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.object.state;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Data;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public abstract class KeeperIR<
		O extends KeeperIROp<O>,
		T extends KeeperIRType<O>>
				implements FldIR, Content<T> {

	private final TypeParameters<?> typeParameters;
	private final ObjectIRBody bodyIR;
	private final Keeper keeper;
	private T instance;

	public KeeperIR(
			TypeParameters<?> typeParameters,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		this.typeParameters = typeParameters;
		this.bodyIR = bodyIR;
		this.keeper = keeper;
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	public final ValueType<?> getValueType() {
		return getTypeParameters().getValueType();
	}

	public final TypeParameters<?> getTypeParameters() {
		return this.typeParameters;
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final Keeper getKeeper() {
		return this.keeper;
	}

	@Override
	public final ID getId() {
		return getKeeper().toID();
	}

	@Override
	public final Obj getDeclaredIn() {
		return getKeeper().getDeclaredIn();
	}

	public abstract T getType();

	public final T getInstance() {
		return this.instance;
	}

	@Override
	public final Data<?> data(Generator generator) {
		return getInstance().data(generator);
	}

	public final void allocate(ObjectIRBodyData data) {
		this.instance = data.getData().addInstance(getId(), getType(), this);
	}

	public final KeeperOp<O> op(Code code, ObjOp host) {
		assert getInstance() != null :
			this + " is not allocated yet";
		return new KeeperOp<>(
				host,
				this,
				host.ptr().keeper(code, getInstance()));
	}

	@Override
	public String toString() {
		if (this.keeper == null) {
			return super.toString();
		}
		return this.keeper.toString();
	}

}
