/* Copyright (c) 2001-2009, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb_voltpatches;

import org.hsqldb_voltpatches.HSQLInterface.HSQLParseException;
import org.hsqldb_voltpatches.ParserDQL.CompileContext;
import org.hsqldb_voltpatches.result.Result;
import org.hsqldb_voltpatches.result.ResultMetaData;

/**
 * Implementation of SWAP TABLE statements.<p>
 */
public class StatementSwap extends StatementDML {
    /**
     * Instantiate this as a SWAP TABLE statement.
     */
    StatementSwap(Session session, CompileContext compileContext) {
        super(StatementTypes.SWAP, StatementTypes.X_SQL_DATA_CHANGE,
                session.currentSchema);
        setDatabseObjects(compileContext);
        checkAccessRights(session);
    }

    /**
     * Returns the metadata, which is empty if the CompiledStatement does not
     * generate a Result.
     */
    @Override
    public ResultMetaData getResultMetaData() {
        return ResultMetaData.emptyResultMetaData;
    }

    /**
     * Fails to execute a SWAP statement in the HSQL Backend.
     * @throws HSqlException HSQLDB feature not supported -- unconditionally
     * @return never returns successfully
     */
    @Override
    Result getResult(Session session) {
        throw Error.error(ErrorCode.X_0A501); // HSQLDB feature not supported
    }

    /**
     * VoltDB added method to get a non-catalog-dependent
     * representation of this HSQLDB object.
     * @param session The current Session object may be needed to resolve
     * some names.
     * @return the XML-like tree of nodes with attributes,
     *         representing this object.
     * @throws HSQLParseException
     */
    @Override
    VoltXMLElement voltGetStatementXML(Session session)
            throws HSQLParseException {
        VoltXMLElement xml = new VoltXMLElement("swap");
        String table0 = rangeVariables[0].getTable().getName().name;
        xml.attributes.put("table0", table0);
        String table1 = rangeVariables[1].getTable().getName().name;
        xml.attributes.put("table1", table1);
        return xml;
    }
}
