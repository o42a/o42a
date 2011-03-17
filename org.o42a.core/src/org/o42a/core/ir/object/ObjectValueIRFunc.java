/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.SourceSpec;


abstract class ObjectValueIRFunc<F extends Func> extends ObjectIRFunc {

	private final CodeId id;
	private FuncRec<F> func;

	ObjectValueIRFunc(ObjectIR objectIR) {
		super(objectIR);
		this.id = objectIR.getId().setLocal(
				getGenerator().id().detail(suffix()));
	}

	public final CodeId getId() {
		return this.id;
	}

	public final FuncPtr<F> get() {
		return this.func.getValue();
	}

	public final FuncPtr<F> get(ObjOp host) {

		final ObjectIR objectIR = host.getAscendant().ir(getGenerator());
		final ObjectTypeIR typeIR =
			objectIR.getBodyType().getObjectIR().getTypeIR();
		final ObjectDataType data = typeIR.getObjectData();

		return func(data).getValue();
	}

	public int addSources(SourceSpec[] destination, SourceSpec[] sources) {

		int size = 0;

		for (SourceSpec def : sources) {
			size = addSource(destination, size, def);
		}

		return size;
	}

	private int addSource(SourceSpec[] destination, int at, SourceSpec source) {

		final Obj src = source.getSource();

		if (src == getObjectIR().getObject()) {
			// explicit definition - add unconditionally
			destination[at] = source;
			return at + 1;
		}

		for (int i = 0; i < at; ++i) {

			final SourceSpec d = destination[i];

			if (d == null) {
				continue;
			}

			final Obj s = d.getSource();

			if (s.derivedFrom(src)) {
				// definition will be generated by derived definition
				return at;
			}
			if (s.derivedFrom(src)) {
				// new definition generates ascending one
				destination[i] = null;
			}
		}

		destination[at] = source;

		return at + 1;
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract String suffix();

	protected abstract DefValue value(Definitions definitions);

	protected final void set(ObjectTypeIR typeIR, FuncPtr<F> ptr) {
		this.func = func(typeIR.getObjectData());
		this.func.setValue(ptr);
	}

	protected abstract FuncRec<F> func(ObjectDataType data);

}
