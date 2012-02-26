/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;


public class DereferenceStep extends Step {

	private static final LinkAncestor LINK_ANCESTOR = new LinkAncestor();

	private LinkValueStruct linkStruct;

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.VALUE_REF_USAGE;
	}

	@Override
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return path.cut(1).append(LINK_ANCESTOR).typeRef(distributor);
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj linkObject = start.toObject();

		assert linkObject != null :
			start + " is not an object";

		final ObjectValue linkObjectValue =
				linkObject.value().explicitUseBy(resolver);

		if (resolver.isFullResolution()) {
			linkObject.value().resolveAll(resolver);
		}

		final LinkValueStruct linkStruct =
				linkObjectValue.getValueStruct().toLinkStruct();

		assert linkStruct != null :
			linkObject + " is not a link object";

		if (this.linkStruct == null) {
			this.linkStruct = linkStruct;
		}

		final Value<ObjectLink> linkValue =
				linkStruct.cast(linkObjectValue.getValue());
		final ObjectLink link;

		if (linkValue.getKnowledge().isKnownToCompiler()) {
			if (linkValue.getKnowledge().isFalse()) {
				return null;
			}
			link = linkValue.getCompilerValue();
		} else {
			link = new RtLink(path, start);
		}

		walker.dereference(linkObject, this, link);

		return link.getTarget();
	}

	@Override
	protected Scope revert(Scope target) {
		return target.toObject().getDereferencedLink().getScope();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(new DereferenceStep().toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class LinkAncestor extends PathFragment {

		@Override
		public Path expand(PathExpander expander, int index, Scope start) {

			final LinkValueStruct linkStruct =
					start.toObject().value().getValueStruct().toLinkStruct();
			final TypeRef typeRef = linkStruct.getTypeRef();

			return start.getEnclosingScopePath()
					.append(typeRef.getPath().getPath());
		}

		@Override
		public String toString() {
			return "^^";
		}

	}

	private final class Op extends StepOp<DereferenceStep> {

		Op(PathOp start, DereferenceStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final LinkValueStruct linkStruct = DereferenceStep.this.linkStruct;
			final ValDirs valDirs = dirs.value(linkStruct, "link");
			final ValOp value = start().writeValue(valDirs);
			final Block code = valDirs.code();

			final DataOp ptr =
					value.value(null, code)
					.toPtr(null, code)
					.load(null, code)
					.toData(code.id("target"), code);

			valDirs.done();

			return anonymousObject(
					getBuilder(),
					ptr,
					linkStruct.getTypeRef().typeObject(dummyUser()));
		}

	}

}
