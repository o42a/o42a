/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ir.value.struct;

import static org.o42a.core.ir.object.type.ValueTypeDescOp.VALUE_TYPE_DESC_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.object.type.ValueTypeDescOp;
import org.o42a.core.ir.value.*;
import org.o42a.core.ir.value.impl.DefaultValueIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValueStructIR<S extends ValueStruct<S, T>, T> {

	private final Generator generator;
	private final S valueStruct;
	private Ptr<ValueTypeDescOp> valueTypeDesc;

	public ValueStructIR(Generator generator, S valueStruct) {
		this.generator = generator;
		this.valueStruct = valueStruct;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final ValueType<S, T> getValueType() {
		return this.valueStruct.getValueType();
	}

	public final S getValueStruct() {
		return this.valueStruct;
	}

	public boolean hasValue() {
		return true;
	}

	public boolean hasLength() {
		return false;
	}

	public final Ptr<ValueTypeDescOp> getValueTypeDesc() {
		if (this.valueTypeDesc != null) {
			return this.valueTypeDesc;
		}
		return this.valueTypeDesc =
				getGenerator().externalGlobal().setConstant().link(
						"o42a_val_type_" + getValueType().getSystemId(),
						VALUE_TYPE_DESC_TYPE);
	}

	public abstract Val val(T value);

	public abstract Ptr<ValType.Op> valPtr(T value);

	public abstract KeeperIR<?, ?> createKeeperIR(
			ObjectIRBody bodyIR,
			Keeper keeper);

	public abstract ValueIR valueIR(ObjectIR objectIR);

	public abstract ValHolder tempValHolder(ValOp value);

	public abstract ValHolder valTrap(ValOp value);

	@Override
	public String toString() {
		if (this.valueStruct == null) {
			return super.toString();
		}
		return this.valueStruct + " IR";
	}

	protected final ValueIR defaultValueIR(ObjectIR objectIR) {
		return new DefaultValueIR(this, objectIR);
	}

}
