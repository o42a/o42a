/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.AdapterId.adapterId;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public final class Adapter extends PathFragment implements LocationInfo {

	private final CompilerContext context;
	private final Loggable loggable;
	private final StaticTypeRef adapterType;
	private final CompilerLogger logger;

	public Adapter(
			LocationInfo location,
			StaticTypeRef adapterType,
			CompilerLogger logger) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.adapterType = adapterType;
		this.logger = logger;
	}

	@Override
	public Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	public final StaticTypeRef getAdapterType() {
		return this.adapterType;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final ObjectType objectType = start.toObject().type();
		if (objectType.toString().equals("ObjectType[Build array: a]")) {
			System.err.println("(!)");
		}

		if (objectType.derivedFrom(this.adapterType.getType().type())) {
			return Path.SELF_PATH;
		}

		final Member adapterMember =
				objectType.getObject().member(adapterId(this.adapterType));

		if (adapterMember == null) {
			this.logger.incompatible(this.loggable, this.adapterType);
			return null;
		}

		final MemberKey key = adapterMember.getMemberKey();
		final MemberField adapterField = adapterMember.toField();

		if (adapterField == null) {
			return key.toPath();
		}

		// Select the adapter based on it's declaration.
		// If the adapter field was transformed to a link when overridden,
		// this fact will be ignored.
		final MemberField adapterDecl = adapterField.getFirstDeclaration();
		final Obj adapterObject = adapterDecl.substance(dummyUser());

		final int expectedLinkDepth =
				this.adapterType.getParameters().getLinkDepth();
		final int adapterLinkDepth =
				adapterObject.type().getParameters().getLinkDepth();

		if (adapterLinkDepth - expectedLinkDepth  == 1) {
			// Adapter was declared as a link.
			// Use the link target as adapter.
			return key.toPath().dereference();
		}

		// Adapter was declared as a plain object or
		// adapter to the link of the same depth.
		// Use the field itself as adapter.
		return key.toPath();
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return defaultInterface(ref);
	}

	@Override
	public String toString() {
		return "@@" + this.adapterType;
	}

}
