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

function print_lgpl() {
	print indent (progname ? progname : "o42a Programming Language")
	print indent "Copyright (C)", year, author
	print ""
	print indent "This file is part of o42a."
	print ""
	print indent "o42a is free software: you can redistribute it and/or modify"
	print indent "it under the terms of the GNU Lesser General Public License"
	print indent "as published by the Free Software Foundation, either version 3"
	print indent "of the License, or (at your option) any later version."
	print ""
	print indent "o42a is distributed in the hope that it will be useful,"
	print indent "but WITHOUT ANY WARRANTY; without even the implied warranty of"
	print indent "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
	print indent "GNU General Public License for more details."
	print ""
	print indent "You should have received a copy of the GNU Lesser General Public License"
	print indent "along with this program.  If not, see <http://www.gnu.org/licenses/>."
}

function print_copyright() {
	if (o42a) {
		print "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
	} else {
		print "/*"
	}
	if (LGPL) {
		print_lgpl()
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
	buffer = ""
	skip = 0
	comment = 0
	o42a = license ~ "-o42a$"
	LGPL = license ~ "^LGPL" 
	indent = o42a ? "" : "    "
	year = "2012"
	author = "Ruslan Lopatin"
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
		skip=1
		exit 0
	}
	year_str=gensub("^([^,-]*)[,-].*", "\\1-" year, "g", $3)
	if (year_str == $3) {
		year_str=$3 "," year
	}
	printf "%s", buffer
	print indent "Copyright (C)", year_str, author
	comment = 0
	buffer = ""
	skip = 1
	next
}

comment && (o42a && /~~~/ || !o42a && /\*\//) {
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

END {
	if (!skip) {
		print_copyright()
	}
}
