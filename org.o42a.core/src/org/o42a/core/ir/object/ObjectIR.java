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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.o42a.core.ir.object.type.ObjectDescIR.allocateDescIR;
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import java.util.*;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.object.type.ObjectDescIR;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.object.Deps;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class ObjectIR {

	private final Generator generator;
	private final Obj object;
	private final ObjectDataIR dataIR;
	private final ValueIR valueIR;
	private ObjectDescIR descIR;
	private VmtIR vmtIR;
	private ObjectValueIR objectValueIR;
	private ObjectIRBodies typeBodies;
	private ObjectIRBodies bodies;
	private List<DepIR> existingDeps;
	private Map<Dep, DepIR> allDeps;

	public ObjectIR(Generator generator, Obj object) {
		this.generator = generator;
		this.object = object;
		this.dataIR = new ObjectDataIR(this);
		this.valueIR =
				object.type().getValueType().ir(generator).valueIR(this);
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
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
		if (this.descIR != null) {
			return this.descIR;
		}

		assert getObject().assertFullyResolved();

		return this.descIR = allocateDescIR(this);
	}

	public final VmtIR getVmtIR() {
		if (this.vmtIR != null) {
			return this.vmtIR;
		}

		final Obj object = getObject();

		assert object.assertFullyResolved();

		final Obj lastDefinition = object.type().getLastDefinition();

		if (!object.is(lastDefinition)) {
			return this.vmtIR = lastDefinition.ir(getGenerator()).getVmtIR();
		}

		this.vmtIR = new VmtIR(this);

		getGenerator().newGlobal().struct(this.vmtIR).getInstance();

		return this.vmtIR;
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
		if (this.objectValueIR != null) {
			return this.objectValueIR;
		}
		return this.objectValueIR = new ObjectValueIR(this);
	}

	public final ObjectIRBodies typeBodies() {
		if (this.typeBodies != null) {
			return this.typeBodies;
		}
		if (!isSampleDeclaration()) {
			return this.typeBodies =
					getSampleDeclaration().ir(getGenerator()).typeBodies();
		}

		this.typeBodies = new ObjectIRBodies(this, true);
		this.typeBodies.allocate();

		return this.typeBodies;
	}

	public final ObjectIRBodies bodies() {
		if (this.bodies != null) {
			return this.bodies;
		}

		this.bodies = new ObjectIRBodies(this, false);
		this.bodies.allocate();

		return this.bodies;
	}

	public final ObjectIR allocate() {
		getInstance();
		return this;
	}

	public final ObjOp op(CodeBuilder builder, Code code) {
		return getInstance()
				.pointer(getGenerator())
				.op(null, code)
				.op(builder, this);
	}

	public final List<DepIR> existingDeps() {
		deps();
		return this.existingDeps;
	}

	public final DepIR dep(Dep dep) {

		final DepIR depIR = deps().get(dep);

		assert depIR != null :
			"Dependency `" + dep + "` is not present in `" + this + '`';

		return depIR;
	}

	@Override
	public String toString() {
		return this.object + " IR";
	}

	private Map<Dep, DepIR> deps() {
		if (this.allDeps != null) {
			return this.allDeps;
		}

		final Deps deps = getObject().deps();
		final int size = deps.size();

		if (size == 0) {
			this.existingDeps = emptyList();
			return this.allDeps = emptyMap();
		}

		final Analyzer analyzer = getGenerator().getAnalyzer();
		final IdentityHashMap<Dep, DepIR> irs = new IdentityHashMap<>(size);
		final ArrayList<DepIR> existing = new ArrayList<>(size);

		for (Dep dep : deps) {
			if (!dep.exists(analyzer)) {
				continue;
			}

			final DepIR depIR = new DepIR(this, dep, existing.size());

			irs.put(dep, depIR);
			if (!depIR.isOmitted()) {
				existing.add(depIR);
			}
		}

		this.existingDeps = existing;

		return this.allDeps = irs;
	}

}
