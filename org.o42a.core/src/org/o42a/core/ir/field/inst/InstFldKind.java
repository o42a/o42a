/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.inst;

import static org.o42a.core.ir.field.FldKind.RESUME_FROM;
import static org.o42a.util.string.ID.rawId;

import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public enum InstFldKind {

	INST_LOCK(FldKind.LOCK, rawId("lock")) {

		@Override
		public LockFld declare(ObjectIRBody bodyIR, Obj ascendant) {
			if (bodyIR.isMain()
					&& bodyIR.getObjectIR().getValueIR().requiresLock()) {
				return new LockFld(bodyIR);
			}
			for (FldIR<?, ?> fld : bodyIR.vmtFields()) {
				if (fld.requiresLock()) {
					return new LockFld(bodyIR);
				}
			}
			return null;
		}

	},

	INST_RESUME_FROM(RESUME_FROM, rawId("resume_from")) {

		@Override
		public ResumeFromFld declare(ObjectIRBody bodyIR, Obj ascendant) {
			if (!ascendant.value().getDefinitions().areYielding()) {
				return null;
			}
			return new ResumeFromFld(bodyIR);
		}

	};

	private final FldKind fldKind;
	private final ID id;

	InstFldKind(FldKind fldKind, ID id) {
		this.fldKind = fldKind;
		this.id = id;
	}

	public final FldKind getKind() {
		return this.fldKind;
	}

	public final ID getId() {
		return this.id;
	}

	public abstract InstFld<?, ?> declare(ObjectIRBody bodyIR, Obj ascendant);

}
