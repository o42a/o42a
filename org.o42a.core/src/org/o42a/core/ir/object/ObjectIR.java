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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE_OBJECT;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT_OBJECT;
import static org.o42a.core.ir.object.desc.ObjectDescIR.objectDescIR;
import static org.o42a.core.ir.object.vmt.VmtIR.vmtIR;
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;
import static org.o42a.util.fn.FlagInit.flagInit;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Codegen;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.desc.ObjectDescIR;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.FlagInit;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public class ObjectIR implements Codegen {

	private final Generator generator;
	private final Obj object;
	private final ObjectDataIR dataIR;
	private final ValueIR valueIR;
	private final Init<ObjectDescIR> descIR =
			init(() -> objectDescIR(this));
	private final Init<VmtIR> vmtIR = init(() -> vmtIR(this));
	private final Init<ObjectValueIR> objectValueIR =
			init(() -> new ObjectValueIR(this));
	private final Init<ObjectIRBodies> typeBodies = init(
			() -> !isSampleDeclaration()
			? getSampleDeclaration().ir(getGenerator()).typeBodies()
			: new ObjectIRBodies(this, true).allocate());
	private final Init<ObjectIRBodies> bodies =
			init(() -> new ObjectIRBodies(this, false).allocate());
	private final FlagInit hasInheritableFields =
			flagInit(this::detectInheritableFields);

	public ObjectIR(Generator generator, Obj object) {
		this.generator = generator;
		this.object = object;
		this.dataIR = new ObjectDataIR(this);
		this.valueIR =
				object.type().getValueType().ir(generator).valueIR(this);
	}

	@Override
	public final Generator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Obj getAncestor() {

		final TypeRef ancestor = getObject().type().getAncestor();

		return ancestor != null ? ancestor.getInterface() : null;
	}

	public final boolean isSampleDeclaration() {
		return getObject().is(getSampleDeclaration());
	}

	public final Obj getSampleDeclaration() {
		return getObject().type().getSampleDeclaration();
	}

	public final ID getId() {
		return getScopeIR().getScope().getId();
	}

	public final boolean isExact() {
		return !getObject().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	public final ScopeIR getScopeIR() {
		return getObject().getScope().ir(getGenerator());
	}

	public final ObjectDescIR getDescIR() {
		return this.descIR.get();
	}

	public final VmtIR getVmtIR() {
		return this.vmtIR.get();
	}

	public final ObjectDataIR getDataIR() {
		return this.dataIR;
	}

	public final ObjectIRStruct getType() {
		return typeBodies().getStruct();
	}

	public final Ptr<ObjectIROp> ptr() {
		return bodies().ptr();
	}

	public final ObjectIRStruct getInstance() {
		return bodies().getStruct();
	}

	public final ValueIR getValueIR() {
		return this.valueIR;
	}

	public final ObjectValueIR getObjectValueIR() {
		return this.objectValueIR.get();
	}

	public final ObjectIRBodies typeBodies() {
		return this.typeBodies.get();
	}

	public final ObjectIRBodies bodies() {
		return this.bodies.get();
	}

	public final boolean hasInheritableFields() {
		return this.hasInheritableFields.get();
	}

	public final ObjectIR allocate() {
		getInstance();
		return this;
	}

	public final ObjOp exactOp(BuilderCode code) {
		return exactOp(code.getBuilder(), code.code());
	}

	public final ObjOp exactOp(CodeBuilder builder, Code code) {
		return op(builder, code, EXACT_OBJECT);
	}

	public final ObjOp compatibleOp(BuilderCode code) {
		return compatibleOp(code.getBuilder(), code.code());
	}

	public final ObjOp compatibleOp(CodeBuilder builder, Code code) {
		return op(builder, code, COMPATIBLE_OBJECT);
	}

	public final ObjOp compatibleOp(
			CodeBuilder builder,
			OpMeans<ObjectIROp> ptr) {
		return new ObjOp(builder, this, ptr, COMPATIBLE_OBJECT);
	}

	public final TargetStoreOp exactTargetStore(ID id) {
		return new ExactObjectStoreOp(id, this);
	}

	@Override
	public String toString() {
		return this.object + " IR";
	}

	private boolean detectInheritableFields() {
		for (FldIR<?, ?> fldIR : typeBodies().getMainBodyIR().allFields()) {
			if (fldIR.isStateless()) {
				continue;
			}
			if (fldIR.getKind().isInheritable()) {
				return true;
			}
		}

		final Obj ancestor = getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.ir(getGenerator()).hasInheritableFields();
	}

	private final ObjOp op(
			CodeBuilder builder,
			Code code,
			ObjectPrecision precision) {
		return new ObjOp(
				builder,
				this,
				code.means(
						c -> getInstance()
						.pointer(getGenerator())
						.op(null, c)),
				precision);
	}

	private static final class ExactObjectStoreOp implements TargetStoreOp {

		private final ID id;
		private final ObjectIR objectIR;

		ExactObjectStoreOp(ID id, ObjectIR objectIR) {
			this.id = id;
			this.objectIR = objectIR;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
		}

		@Override
		public HostOp loadTarget(CodeDirs dirs) {
			return this.objectIR.exactOp(dirs);
		}

		@Override
		public String toString() {
			if (this.id == null) {
				return super.toString();
			}
			return this.id.toString();
		}

	}

}
