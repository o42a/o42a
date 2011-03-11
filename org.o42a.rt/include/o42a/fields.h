/*
    Run-Time Library
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
#ifndef O42A_FIELDS_H
#define O42A_FIELDS_H

#include "o42a/fld/link.h"
#include "o42a/fld/obj.h"
#include "o42a/fld/scope.h"
#include "o42a/fld/var.h"


union o42a_fld {

	O42A_HEADER;

	o42a_fld_obj obj;

	o42a_fld_link link;

	o42a_fld_var var;

	o42a_fld_scope scope;

};


#endif /* O42A_FIELDS_H */
