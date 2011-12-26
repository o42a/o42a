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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.RefUsage.NON_VALUE_REF_USAGES;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.InlineValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.source.Module;


public final class ModuleStep extends AbstractObjectStep {

	private final String moduleId;

	public ModuleStep(String moduleId) {
		this.moduleId = moduleId;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.ABSOLUTE_PATH;
	}

	public String getModuleId() {
		return this.moduleId;
	}

	@Override
	public boolean isMaterial() {
		return true;
	}

	@Override
	public int hashCode() {
		return this.moduleId.hashCode();
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

		return this.moduleId.equals(other.moduleId);
	}

	@Override
	public String toString() {
		return "<" + this.moduleId + '>';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return objectFieldDefinition(path, distributor);
	}

	@Override
	protected Obj resolveObject(
			BoundPath path,
			int index,
			Scope start) {

		final CompilerContext context = start.getContext();
		final Module module = context.getIntrinsics().getModule(this.moduleId);

		if (module == null) {
			context.getLogger().error(
					"unresolved_module",
					path,
					"Module <%s> can not be resolved",
					this.moduleId);
			return null;
		}

		return module;
	}

	@Override
	protected void walkToObject(PathWalker walker, Obj object) {
		walker.module(this, object);
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		if (uses().selectUse(
				normalizer,
				NON_VALUE_REF_USAGES).isUsed()) {
			return;
		}

		final Module module =
				normalizer.getStepStart()
				.getContext()
				.getIntrinsics()
				.getModule(this.moduleId);
		final InlineValueDef def = module.value().getDefinitions().inline(
				new Normalizer(normalizer, normalizer.getStepStart(), true));

		if (def == null) {
			return;
		}

		normalizer.add(module.getScope(), new InlineStep(def) {
			@Override
			protected Container resolve(
					PathResolver resolver,
					BoundPath path,
					int index,
					Scope start,
					PathWalker walker) {
				return ModuleStep.this.resolve(
						resolver,
						path,
						index,
						start,
						walker);
			}
		});
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<ModuleStep> {

		Op(PathOp start, ModuleStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Obj module = getContext().getIntrinsics().getModule(
					getStep().getModuleId());
			final ObjectIR moduleIR = module.ir(getGenerator());

			return moduleIR.op(getBuilder(), dirs.code());
		}

	}

}
