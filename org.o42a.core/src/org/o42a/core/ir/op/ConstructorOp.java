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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.CodeBuilder.codeBuilder;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Function;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ref.Ref;


public class ConstructorOp extends RefOp {

	private Function<ObjectRefFunc> ancestorFunc;

	public ConstructorOp(HostOp host, Ref ref) {
		super(host, ref);
	}

	@Override
	public ObjectOp target(CodeDirs dirs) {

		final CodeBuilder builder = getBuilder();
		final ObjectOp object = host().toObject(dirs);
		final Obj sample = sample();

		if (object != null) {
			return builder.newObject(
					dirs,
					object,
					ancestorFunc().getPointer().op(null, dirs.code()),
					sample);
		}

		final LocalOp local = host().toLocal();

		if (local != null) {
			assert local.getBuilder() == builder :
				"Wrong builder used when instantiating local object: "
				+ this + ", while " + local.getBuilder() + " expected";
		}

		return builder.newObject(
				dirs,
				null,
				buildAncestor(dirs),
				sample);
	}

	protected Obj sample() {
		return getRef().getResolution().toObject();
	}

	protected ObjectOp buildAncestor(CodeDirs dirs) {
		return dirs.getBuilder().objectAncestor(dirs, sample());
	}

	private Function<ObjectRefFunc> ancestorFunc() {
		if (this.ancestorFunc != null) {
			return this.ancestorFunc;
		}

		this.ancestorFunc = getGenerator().newFunction().create(
				getBuilder().nextId(),
				OBJECT_REF);

		final Code ancestorNotFound =
			this.ancestorFunc.addBlock("ancestor_not_found");
		final CodeBuilder builder = codeBuilder(
				this.ancestorFunc,
				ancestorNotFound.head(),
				getRef().getScope(),
				DERIVED);

		buildAncestorFunc(builder, this.ancestorFunc);
		if (ancestorNotFound.exists()) {
			ancestorNotFound.nullPtr().returnValue(ancestorNotFound);
		}

		return this.ancestorFunc;
	}

	private void buildAncestorFunc(CodeBuilder builder, Code code) {

		final Code ancestorFailed = code.addBlock("ancestor_failed");
		final ObjectOp ancestor = buildAncestor(
				builder.falseWhenUnknown(code, ancestorFailed.head()));

		if (ancestor == null) {
			code.nullPtr().returnValue(code);
		} else {
			ancestor.toAny(code).returnValue(code);
		}

		if (ancestorFailed.exists()) {
			getRef().getContext().getFalse()
			.ir(getGenerator()).op(getBuilder(), ancestorFailed)
			.ptr().toAny(null, ancestorFailed).returnValue(ancestorFailed);
		}
	}

}
