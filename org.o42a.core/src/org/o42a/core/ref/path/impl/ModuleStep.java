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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import java.util.function.Function;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.source.Module;
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public final class ModuleStep extends Step {

	private final Name moduleName;

	public ModuleStep(Name moduleName) {
		this.moduleName = moduleName;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.ABSOLUTE_PATH;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	public Name getModuleName() {
		return this.moduleName;
	}

	@Override
	public int hashCode() {
		return this.moduleName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ModuleStep other = (ModuleStep) obj;

		return this.moduleName.equals(other.moduleName);
	}

	@Override
	public String toString() {
		return "<" + this.moduleName + '>';
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected void localMember(LocalRegistry registry) {
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Module module =
				resolver.getContext()
				.getIntrinsics()
				.getModule(this.moduleName);

		if (module == null) {
			resolver.getLogger().error(
					"unresolved_module",
					resolver,
					"Module <%s> can not be resolved",
					this.moduleName);
			return null;
		}
		if (resolver.isFullResolution()) {
			module.resolveAll();
		}
		resolver.getWalker().module(this, module);

		return module;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.skipStep();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.skipStep();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected HostOp op(HostOp host) {
		return new ModuleOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

	private static final class ModuleOp extends StepOp<ModuleStep> {

		ModuleOp(HostOp host, ModuleStep step) {
			super(host, step);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public ObjOp pathTarget(CodeDirs dirs) {
			return moduleIR().exactOp(dirs);
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return moduleIR().exactTargetStore(id);
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return moduleIR().exactTargetStore(id);
		}

		private final ObjectIR moduleIR() {
			return getContext()
					.getIntrinsics()
					.getModule(getStep().getModuleName())
					.ir(getGenerator());
		}

	}

}
