/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.hsqldb_voltpatches;
import java.lang.*;

/**
 * This class is built to create a single in-memory database
 * which can then be easily manipulated by VoltDB code.
 * <p>
 * Primary interaction with HSQLDB in the following ways:
 * <ul>
 * <li>Initialize an In-Memory SQL Store</li>
 * <li>Execute DDL Statements</li>
 * <li>Dump Serialized Catalog as XML</li>
 * <li>Compile SQL DML Statements to XML</li>
 * </ul>
 */
public class AJG {

    public static void log(String msg) {
       System.out.println("AJG: " + msg);
    }

    public static void dump(Object obj) {
       log(obj.toString());
    }
    public static void stackdump() {
       Thread.dumpStack();
    }



}
