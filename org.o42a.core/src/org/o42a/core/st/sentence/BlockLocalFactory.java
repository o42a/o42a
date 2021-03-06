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
package org.o42a.core.st.sentence;

import static org.o42a.core.member.MemberIdKind.LOCAL_MEMBER_NAME;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.member.field.VisibilityMode.PRIVATE_VISIBILITY;
import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_NAME;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;
import static org.o42a.util.string.Name.caseInsensitiveName;

import java.util.HashMap;
import java.util.function.Predicate;

import org.o42a.codegen.Generator;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.local.LocalFactory;
import org.o42a.util.string.Name;


final class BlockLocalFactory implements LocalFactory {

	private final Block block;
	private final HashMap<Name, Integer> fieldNames = new HashMap<>();

	BlockLocalFactory(Block block) {
		this.block = block;
	}

	@Override
	public Local createLocal(LocationInfo location, Name name, Ref ref) {
		return new Local(location, name, ref);
	}

	@Override
	public void convertToMember(Local local) {
		if (!local.isConvertedToMember()) {
			new BlockLocalRegistry(local).convert();
		}
	}

	private MemberName localName(Local local) {

		final Name localName = local.getName();
		final Name fieldName;
		final Integer lastIndex = this.fieldNames.get(localName);
		final Integer newIndex;

		if (lastIndex == null) {
			newIndex = 1;
			fieldName = localName;
		} else {
			newIndex = lastIndex + 1;
			if (localName.is(ANONYMOUS_LOCAL_NAME)) {
				fieldName = CASE_SENSITIVE.canonicalName(
						localName.toString() + newIndex);
			} else {
				fieldName = caseInsensitiveName(
						localName.toString() + '_' + newIndex);
			}
		}

		this.fieldNames.put(localName, newIndex);

		return LOCAL_MEMBER_NAME.memberName(fieldName);
	}

	private final class BlockLocalRegistry implements LocalRegistry {

		private final Local local;

		BlockLocalRegistry(Local local) {
			this.local = local;
		}

		@Override
		public final Local getLocal() {
			return this.local;
		}

		@Override
		public void declareMemberLocal() {
			declareMemberLocal(null);
		}

		@Override
		public void declareMemberLocal(Predicate<Generator> isOmitted) {

			final Block block = BlockLocalFactory.this.block;
			final FieldDeclaration declaration = fieldDeclaration(
					this.local,
					block.distribute(),
					localName(this.local))
					.setVisibilityMode(PRIVATE_VISIBILITY);
			final MemberLocal member = block.getMemberRegistry()
					.newLocal(declaration, this.local)
					.build()
					.toMember()
					.toLocal();

			getLocal().setMember(member, isOmitted);
		}

		private void convert() {
			getLocal().convertToMember(this);
		}

	}

}
