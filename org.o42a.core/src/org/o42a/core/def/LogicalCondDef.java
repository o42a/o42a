package org.o42a.core.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.ref.Logical;


final class LogicalCondDef extends CondDef {

	private final Logical logical;

	public LogicalCondDef(Logical logical) {
		super(
				sourceOf(logical),
				null,
				null,
				transparentRescoper(logical.getScope()));
		this.logical = logical;
	}

	private LogicalCondDef(
			LogicalCondDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper,
			Logical logical) {
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

		final Logical newLogical = this.logical.and(logical);

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
	protected final Logical getLogical() {
		return this.logical;
	}

	@Override
	protected CondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {
		return new LogicalCondDef(this, prerequisite, rescoper, this.logical);
	}

}
