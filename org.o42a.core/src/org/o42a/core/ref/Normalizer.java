package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.User;


public final class Normalizer implements UseCaseInfo {

	private final UseCase useCase;
	private final Scope normalizedScope;

	public Normalizer(UseCaseInfo useCase, Scope normalizedScope) {
		this.useCase = useCase.toUseCase();
		this.normalizedScope = normalizedScope;
	}

	public final Scope getNormalizedScope() {
		return this.normalizedScope;
	}

	@Override
	public final User<?> toUser() {
		return this.useCase;
	}

	@Override
	public final UseCase toUseCase() {
		return this.useCase;
	}

	@Override
	public String toString() {
		if (this.normalizedScope == null) {
			return super.toString();
		}
		return "Normalizer[to " + this.normalizedScope
				+ " by " + this.useCase + ']';
	}

}
