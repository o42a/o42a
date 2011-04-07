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
import static org.o42a.core.ir.op.CodeDirs.splitWhenUnknown;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.core.ir.op.CodeDirs;
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

			final Code code = dirs.code();
			final ValOp result = code.allocate(null, VAL_TYPE).storeIndefinite(code);

			object.objectType(code).writeOverriddenValue(code, result);
			result.go(code, dirs);
		}

		@Override
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeValue(dirs, result);
				return;
			}

			final Code code = dirs.code();

			object.objectType(code).writeOverriddenValue(code, result);
			result.go(code, dirs);
		}

	},

	VALUE("Value", "value") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.valuePart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final Code code = dirs.code();
			final ValOp result = code.allocate(null, VAL_TYPE).storeIndefinite(code);

			writeValue(dirs, result, op);
		}

		@Override
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeValue(dirs, result);
				return;
			}

			final Code code = dirs.code();

			object.objectType(code).writeOverriddenValue(code, result);
			result.go(code, dirs);
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
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final Code code = dirs.code();
			final CodeBlk reqFalse = code.addBlock("req_false");
			final CodeBlk reqUnknown = code.addBlock("req_unknown");

			writeLogicalValue(
					splitWhenUnknown(
							code,
							reqFalse.head(),
							reqUnknown.head()),
					op);

			result.storeVoid(code);
			if (reqFalse.exists()) {
				result.storeFalse(reqFalse);
			}
			if (reqUnknown.exists()) {
				result.storeUnknown(reqUnknown);
			}
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
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final Code code = dirs.code();
			final CodeBlk reqFalse = code.addBlock("cond_false");
			final CodeBlk reqUnknown = code.addBlock("cond_unknown");

			writeLogicalValue(
					splitWhenUnknown(
							code,
							reqFalse.head(),
							reqUnknown.head()),
					op);

			result.storeVoid(code);
			if (reqFalse.exists()) {
				result.storeFalse(reqFalse);
			}
			if (reqUnknown.exists()) {
				result.storeUnknown(reqUnknown);
			}
		}

	},

	CLAIM("Claim", "claim") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.claimPart(ex);
		}

		@Override
		void writeLogicalValue(CodeDirs dirs, ValuePartOp op) {

			final Code code = dirs.code();
			final ValOp result = code.allocate(null, VAL_TYPE).storeIndefinite(code);

			writeValue(dirs, result, op);
		}

		@Override
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeClaim(dirs, result);
				return;
			}

			final Code code = dirs.code();

			object.objectType(code).writeOverriddenClaim(code, result);
			result.go(code, dirs);
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

			final Code code = dirs.code();
			final ValOp result = code.allocate(null, VAL_TYPE).storeIndefinite(code);

			writeValue(dirs, result, op);
		}

		@Override
		void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(dirs);

			if (!op.isOverridden()) {
				object.writeProposition(dirs, result);
				return;
			}

			final Code code = dirs.code();
			final ObjectTypeOp data = object.objectType(code);

			data.writeOverriddenProposition(code, result);
			result.go(code, dirs);
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

	abstract void writeValue(CodeDirs dirs, ValOp result, ValuePartOp op);

}
