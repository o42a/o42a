function print_copyright() {
    print "/*"
    print "    " (progname ? progname : "o42a Programming Language")
    print "    Copyright (C) 2011 Ruslan Lopatin"
    print ""
    print "    This file is part of o42a."
    print ""
    print "    o42a is free software: you can redistribute it and/or modify"
    print "    it under the terms of the GNU General Public License as published by"
    print "    the Free Software Foundation, either version 3 of the License, or"
    print "    (at your option) any later version."
    print ""
    print "    o42a is distributed in the hope that it will be useful,"
    print "    but WITHOUT ANY WARRANTY; without even the implied warranty of"
    print "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
    print "    GNU General Public License for more details."
    print ""
    print "    You should have received a copy of the GNU General Public License"
    print "    along with this program.  If not, see <http://www.gnu.org/licenses/>."
    print "*/"
}


BEGIN {
    buffer = ""
    skip = 0
    comment = 0
}

skip {
    print
    next
}

!comment && /^\/\*/ {
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
    printf "%s", buffer
    print
    comment = 0
    buffer = ""
    skip = 1
    next
}

comment && /\*\// {
    print_copyright()
    printf "%s", buffer
    print
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
	if (buffer) {
	    printf "%s", buffer
	    buffer = ""
	}
    }
}
