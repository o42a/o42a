function print_gpl() {
	print indent (progname ? progname : "o42a Programming Language")
	print indent "Copyright (C)", year, author
	print ""
	print indent "This file is part of o42a."
	print ""
	print indent "o42a is free software: you can redistribute it and/or modify"
	print indent "it under the terms of the GNU General Public License as published by"
	print indent "the Free Software Foundation, either version 3 of the License, or"
	print indent "(at your option) any later version."
	print ""
	print indent "o42a is distributed in the hope that it will be useful,"
	print indent "but WITHOUT ANY WARRANTY; without even the implied warranty of"
	print indent "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
	print indent "GNU General Public License for more details."
	print ""
	print indent "You should have received a copy of the GNU General Public License"
	print indent "along with this program.  If not, see <http://www.gnu.org/licenses/>."
}

function print_mpl() {
	print indent "Copyright (C)", year, author
	print ""
	print indent "This Source Code Form is subject to the terms of the Mozilla Public"
	print indent "License, v. 2.0. If a copy of the MPL was not distributed with this"
	print indent "file, You can obtain one at http://mozilla.org/MPL/2.0/."
}

function print_public_domain() {
	print indent "Any copyright is dedicated to the Public Domain."
	print indent "http://creativecommons.org/publicdomain/zero/1.0/"
}

function print_copyright() {
	if (o42a) {
		print "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
	} else {
		print "/*"
	}
	if (public_domain) {
		print_public_domain()
	} else if (MPL) {
		print_mpl()
	} else {
		print_gpl()
	}
	if (o42a) {
		print "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
	} else {
		print "*/"
	}
}


BEGIN {
	public_domain = license ~ "^PUB"
	MPL = license ~ "^MPL"

	no_copyright=public_domain

	year = "2014"
	author = "Ruslan Lopatin"

	o42a = license ~ ".o42a$"
	indent = o42a ? "" : "    "

	# Set if the license should be changed.
	license_change=0

	buffer = ""
	skip = 0
	comment = 0
}

skip {
	print
	next
}

!comment && (o42a && /^~~~/ || !o42a && /^\/\*/) {
	comment = 1
	buffer = buffer $0 "\n"
	next
}

!comment {
	print_copyright()
	skip = 1
	print
	next
}

comment && /^[ \t]*Copyright/ {
	if ($3 ~ year) {
		if (license_change) {
			year = $3
			next
		}
		exit 0
	}
	year_str=gensub("^([^,-]*)[,-].*", "\\1-" year, "g", $3)
	if (year_str == $3) {
		year_str=$3 "," year
	}
	printf "%s", buffer
	print indent "Copyright (C)", year_str, author
	comment = 0
	skip = 1
	buffer = ""
	next
}

comment && (o42a && /~~~/ || !o42a && /\*\//) {
	if (!license_change) {
		exit 0
	}
	print_copyright()
	comment = 0
	buffer = ""
	skip = 1
	next
}

comment {
	buffer = buffer $0 "\n"
	next
}
