package org.o42a.core.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.ref.Logical;


final class LogicalCondDef extends CondDef {

	private final LogicalDef logical;

	public LogicalCondDef(Logical logical) {
		super(
				sourceOf(logical),
				null,
				null,
				transparentRescoper(logical.getScope()));
		this.logical = logical.toLogicalDef();
	}

	private LogicalCondDef(
			LogicalCondDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper,
			LogicalDef logical) {
		super(prototype, prerequisite, rescoper);
		this.logical = logical;
	}

	@Override
	public DefKind getKind() {
		return DefKind.CONDITION;
	}

	@Override
	public boolean hasPrerequisite() {
		return false;
	}

	@Override
	public CondDef and(Logical logical) {

		final LogicalDef newLogical = this.logical.and(logical);

		if (newLogical == this.logical) {
			return this;
		}

		return new LogicalCondDef(
				this,
				prerequisite(),
				getRescoper(),
				newLogical);
	}

	@Override
	protected LogicalDef buildPrerequisite() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Logical logical() {
		return this.logical.fullLogical();
	}

	@Override
	protected CondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {

		final LogicalDef logical = this.logical.rescope(additionalRescoper);

		if (logical == null) {
			return null;
		}

		return new LogicalCondDef(this, prerequisite, rescoper, logical);
	}

}
