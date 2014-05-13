/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.core.Container;
import org.o42a.core.ContainerInfo;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;
import org.o42a.util.string.SubID;


public final class Local
		extends PathFragment
		implements ContainerInfo, MemberPath {

	public static final Name ANONYMOUS_LOCAL_NAME =
			CASE_SENSITIVE.canonicalName("L");
	public static final MemberName ANONYMOUS_LOCAL_MEMBER =
			MemberIdKind.LOCAL_NAME.memberName(ANONYMOUS_LOCAL_NAME);
	public static final SubID LOCAL_FIELD_SUFFIX =
			CASE_SENSITIVE.canonicalName("LF");

	private final Location location;
	private final Name name;
	private final MemberName memberId;
	private final Ref originalRef;
	private Ref fieldRef;
	private Ref ref;

	Local(LocationInfo location, Name name, Ref ref) {
		assert name != null :
			"Local name not specified";
		assert ref != null :
			"Local reference not specified";
		this.location = location.getLocation();
		this.name = name;
		this.originalRef = ref;
		this.memberId = MemberIdKind.LOCAL_NAME.memberName(name);
	}

	public final Name getName() {
		return this.name;
	}

	public final MemberName getMemberId() {
		return this.memberId;
	}

	public final boolean isField() {
		return this.fieldRef != null;
	}

	public final Ref originalRef() {
		return this.originalRef;
	}

	public final Ref ref() {
		if (this.ref != null) {
			return this.ref;
		}
		return this.ref = originalRef();
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public final Scope getScope() {
		return originalRef().getScope();
	}

	@Override
	public final Container getContainer() {
		return originalRef().getContainer();
	}

	public Ref toRef() {
		return toPath().bind(this, getScope()).target(distribute());
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Ref ref = ref();// Initialize

		if (!isField()) {
			return new LocalStep(this).toPath();
		}

		return ref.getPath().getPath();
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
	public final Path pathToMember() {
		return toPath();
	}

	@Override
	public final Member toMember() {
		return null;
	}

	@Override
	public final Local toLocal() {
		return this;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name.toString();
	}

	void convertToField(MemberField field) {
		assert this.ref == null :
			"Too late to convert the local to field";
		this.ref = this.fieldRef =
				SELF_PATH.append(field.getMemberKey())
				.bind(this, getScope())
				.target(distribute());
	}

}
