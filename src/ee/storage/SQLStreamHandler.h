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

#ifndef SQLSTREAMHANDLER_H_
#define SQLSTREAMHANDLER_H_

#include "catalog/sqlstreamhandlerinfo.h"
#include "common/tabletuple.h"
#include "streamedtable.h"

#include <vector>

namespace voltdb {

class SQLStreamHandler {
public:
    SQLStreamHandler(StreamedTable *destTable, catalog::SQLStreamHandlerInfo *info);

    const std::vector<int>& getColumnIndices() const {
        return m_columnIndices;
    }

    bool failsPredicate(const TableTuple& tuple) const {
        return (m_filterPredicate && !m_filterPredicate->eval(&tuple, NULL).isTrue());
    }

    void handleTupleInsert(TableTuple& source);

private:
    AbstractExpression* parsePredicate(catalog::SQLStreamHandlerInfo *info);

    std::vector<int> m_columnIndices;
    boost::shared_ptr<AbstractExpression> m_filterPredicate;
    StreamedTable *m_destTable;
    StandAloneTupleStorage m_templateTuple;
};

} // namespace voltdb
#endif // SQLSTREAMHANDLER_H_
