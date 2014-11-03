/*
    Root Object Definition
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.root.array;

import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;
import static org.o42a.core.ref.RefUsage.ASSIGNABLE_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.root.array.IndexedItem.indexPath;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = ArrayItem.class, value = "set.o42a")
final class SetArrayItem extends AnnotatedBuiltin {

	private static final MemberName NEW_VALUE_NAME =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("new value"));

	private Ref array;
	private Ref item;
	private Ref index;
	private Ref newValue;

	SetArrayItem(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
		setValueType(ValueType.VOID);
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return ESCAPE_POSSIBLE;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters().runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {

		final FullResolver valueResolver =
				resolver.setRefUsage(VALUE_REF_USAGE);

		array().resolveAll(valueResolver);
		index().resolveAll(valueResolver);

		final Ref item = item();
		final Ref newValue = newValue();

		item.resolveAll(resolver.setRefUsage(ASSIGNABLE_REF_USAGE));

		final Ref destTarget =
				item.getPath()
				.dereference()
				.target(item.distribute());
		final FullResolver targetResolver =
				resolver.setRefUsage(TARGET_REF_USAGE);
		final Resolution val = newValue.resolveAll(targetResolver);
		final Resolution dest = destTarget.resolveAll(targetResolver);

		if (dest.isError() || val.isError()) {
			return;
		}

		final Obj destObj = dest.toObject();
		final Obj valObj = val.toObject();

		valObj.value().wrapBy(destObj.value());
		valObj.type().wrapBy(destObj.type());
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineArray = array().inline(normalizer, origin);
		final InlineValue inlineIndex = index().inline(normalizer, origin);
		final InlineValue inlineNewValue =
				newValue().inline(normalizer, origin);

		if (inlineArray == null
				|| inlineIndex == null
				|| inlineNewValue == null) {
			return null;
		}

		return new SetArrayItemEval(
				this,
				inlineArray,
				inlineIndex,
				inlineNewValue);
	}

	@Override
	public Eval evalBuiltin() {
		return new SetArrayItemEval(this, null, null, null);
	}

	final Ref array() {
		if (this.array != null) {
			return this.array;
		}

		final Scope enclosingScope = getScope().getEnclosingScope();

		return this.array =
				getScope()
				.getEnclosingScopePath()
				.append(enclosingScope.getEnclosingScopePath())
				.bind(this, getScope())
				.target(distribute());
	}

	final Ref item() {
		if (this.item != null) {
			return this.item;
		}
		return this.item =
				getScope()
				.getEnclosingScopePath()
				.bind(this, getScope())
				.target(distribute());
	}

	final Ref index() {
		if (this.index != null) {
			return this.index;
		}

		final PrefixPath itemPrefix =
				getScope().getEnclosingScopePath().toPrefix(getScope());

		return this.index =
				indexPath(getScope().getEnclosingScope())
				.prefixWith(itemPrefix)
				.target(distribute());
	}

	final Ref newValue() {
		if (this.newValue != null) {
			return this.newValue;
		}

		final MemberKey newValueKey = NEW_VALUE_NAME.key(getScope());
		final Member newValueField = member(newValueKey);

		return this.newValue = newValueKey.toPath()
				.bind(newValueField, getScope())
				.target(distribute());
	}

}
