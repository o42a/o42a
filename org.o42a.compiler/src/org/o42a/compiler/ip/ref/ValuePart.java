/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.def.Definitions.definitions;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.op.ValOp;


enum ValuePart {

	ALL("") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions;
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeLogicalValue(dirs);
				return;
			}

			final ValDirs valDirs = dirs.value();

			object.objectType(valDirs.code()).writeOverriddenValue(valDirs);
			valDirs.done();
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs.dirs());

			if (!op.isOverridden()) {
				return object.writeValue(dirs);
			}

			return object.objectType(dirs.code()).writeOverriddenValue(dirs);
		}

	},

	VALUE("Value", "value") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.valuePart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ValDirs valDirs = dirs.value();

			writeValue(valDirs, op);
			valDirs.done();
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs.dirs());

			if (!op.isOverridden()) {
				return object.writeValue(dirs);
			}

			return object.objectType(dirs.code()).writeOverriddenValue(dirs);
		}

	},

	REQUIREMENT("Requirement", "requirement") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.requirementPart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeRequirement(dirs);
				return;
			}

			object.objectType(dirs.code()).writeOverriddenRequirement(dirs);
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {
			writeLogicalValue(dirs.dirs(), op);
			return voidValue().op(dirs.code());
		}

	},

	CONDITION("Condition", "condition") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.conditionPart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeCondition(dirs);
				return;
			}

			final Code code = dirs.code();

			object.objectType(code).writeOverriddenCondition(dirs);
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {
			writeLogicalValue(dirs.dirs(), op);
			return voidValue().op(dirs.code());
		}

	},

	CLAIM("Claim", "claim") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.claimPart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ValDirs valDirs = dirs.value();

			writeValue(valDirs, op);
			valDirs.done();
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs.dirs());

			if (!op.isOverridden()) {
				return object.writeClaim(dirs);
			}

			return object.objectType(dirs.code()).writeOverriddenClaim(dirs);
		}

	},

	PROPOSITION("Proposition", "proposition") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions(
					ex,
					ex.getScope(),
					definitions.getPropositions());
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final ValDirs valDirs = dirs.value();

			writeValue(valDirs, op);
			valDirs.done();
		}

		@Override
		ValOp writeValue(ValDirs dirs, ValuePartOp op) {

			final ObjectOp object = op.object(dirs.dirs());

			if (!op.isOverridden()) {
				return object.writeProposition(dirs);
			}

			final ObjectTypeOp objectType = object.objectType(dirs.code());

			return objectType.writeOverriddenProposition(dirs);
		}

	};

	final String partName;

	ValuePart(String partName) {
		this.partName = partName;
	}

	ValuePart(String partName, String partId) {
		this.partName = partName;
		ValuePartRef.partsById.put(partId, this);
	}

	abstract Definitions valuePart(ValuePartRef ex, Definitions definitions);

	abstract void writeLogicalValue(CodeDirs dirs, ValuePartOp op);

	abstract ValOp writeValue(ValDirs dirs, ValuePartOp op);

}
