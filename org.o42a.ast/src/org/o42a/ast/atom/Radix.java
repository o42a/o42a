package org.o42a.ast.atom;


public enum Radix implements SignType {

	DECIMAL_RADIX(10, "") {

		@Override
		public boolean isDigit(int c) {
			return c >= '0' && c <= '9';
		}

	},

	HEXADECIMAL_RADIX(16, "0x") {

		@Override
		public boolean isDigit(int c) {
			if (c < '0') {
				return false;
			}
			if (c <= '9') {
				return true;
			}
			if (c > 'f' || c < 'A') {
				return false;
			}
			if (c >= 'a') {
				return true;
			}
			if (c <= 'F') {
				return true;
			}
			return false;
		}

	},

	BINARY_RADIX(2, "0b") {

		@Override
		public boolean isDigit(int c) {
			return c == '0' || c == '1';
		}

	};

	private final int radix;
	private final String sign;

	Radix(int radix, String sign) {
		this.radix = radix;
		this.sign = sign;
	}

	public final int getRadix() {
		return this.radix;
	}

	public abstract boolean isDigit(int c);

	@Override
	public final String getSign() {
		return this.sign;
	}

}
