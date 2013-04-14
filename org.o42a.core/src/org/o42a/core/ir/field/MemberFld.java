/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.util.string.ID;


public abstract class MemberFld<F extends Fld.Op<F>> extends Fld<F> {

	private final Field field;

	public MemberFld(Field field) {
		super();
		this.field = field;
	}

	public final Field getField() {
		return this.field;
	}

	@Override
	public final MemberKey getKey() {
		return getField().getKey();
	}

	@Override
	public final ID getId() {
		return getField().getId();
	}

	@Override
	public boolean isOverrider() {

		final Field field = getField();

		if (!field.isOverride()) {
			// New field declaration.
			return false;
		}

		final Scope definedIn = field.getDefinedIn();
		final Scope enclosingScope = field.getEnclosingScope();

		if (enclosingScope.is(definedIn)) {
			// Explicit field override.
			return true;
		}

		final ObjectType definedInType = definedIn.toObject().type();

		if (definedInType.getAncestor().getType().type().derivedFrom(
				definedInType)) {
			// Field overridden in ancestor.
			return false;
		}

		// Field overridden in sample.
		return true;
	}

	@Override
	public final Obj getDefinedIn() {
		return getField().getDefinedIn().toObject();
	}

	@Override
	public String toString() {
		return getField().toString();
	}

	@Override
	protected void allocate(ObjectIRBodyData data) {
		assert getField()
		.toMember()
		.getAnalysis()
		.getDeclarationAnalysis()
		.isUsed(data.getGenerator().getAnalyzer(), ALL_FIELD_USAGES) :
			"Attempt to generate never accessed field " + getField();
		super.allocate(data);
	}

	@Override
	protected boolean mayOmit() {

		final FieldAnalysis declarationAnalysis =
				getField().toMember().getAnalysis().getDeclarationAnalysis();

		return !declarationAnalysis.derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	@Override
	protected abstract MemberFldOp<F> op(Code code, ObjOp host, F ptr);

}
