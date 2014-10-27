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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.field.object.ObjFldConf.CONF_ID;
import static org.o42a.core.ir.field.object.ObjFldConf.OBJ_FLD_CONF_TYPE;

import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.field.RefVmtRecord;
import org.o42a.core.ir.field.object.ObjFldConf.Op;
import org.o42a.core.ir.object.vmt.VmtIROp;


final class ObjVmtRecord extends RefVmtRecord<
		StatefulOp,
		StatefulType,
		Ptr<ObjFldConf.Op>,
		StructRec<ObjFldConf.Op>> {

	ObjVmtRecord(ObjFld fld) {
		super(fld);
	}

	@Override
	public final ObjFld fld() {
		return (ObjFld) super.fld();
	}

	@Override
	protected StructRec<Op> allocateRecord(SubData<VmtIROp> vmt) {
		return vmt.addPtr(fld().getId().detail(CONF_ID), OBJ_FLD_CONF_TYPE);
	}

	@Override
	protected Ptr<ObjFldConf.Op> dummyContent() {
		return getGenerator().getGlobals().nullPtr(OBJ_FLD_CONF_TYPE);
	}

	@Override
	protected Ptr<ObjFldConf.Op> reuseContent() {

		final ObjFld fld = fld();
		final ObjFldConf conf = fld.conf();

		if (conf.fld() == fld) {
			// Own config. Not reused.
			return null;
		}

		return conf.ptr();
	}

	@Override
	protected Ptr<Op> createContent() {
		return fld().conf().ptr();
	}

}
