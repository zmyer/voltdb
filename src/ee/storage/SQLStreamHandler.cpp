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

#include "SQLStreamHandler.h"
#include "catalog/column.h"
#include "catalog/columnref.h"
#include "common/tabletuple.h"

ENABLE_BOOST_FOREACH_ON_CONST_MAP(ColumnRef);
typedef std::pair<std::string, catalog::ColumnRef*> LabeledColumnRef;

namespace voltdb {

    SQLStreamHandler::SQLStreamHandler(StreamedTable *destTable, catalog::SQLStreamHandlerInfo *info)
        : m_filterPredicate(parsePredicate(info)),
          m_destTable(destTable)
    {
        BOOST_FOREACH (LabeledColumnRef labeledColumnRef, info->streamCols()) {
            catalog::ColumnRef *columnRef = labeledColumnRef.second;
            m_columnIndices.push_back(columnRef->column()->index());
        }
        m_templateTuple.init(destTable->schema());
    }

    void SQLStreamHandler::handleTupleInsert(TableTuple& source) {
        if (failsPredicate(source)) {
            return;
        }
        Pool* tempPool = ExecutorContext::getTempStringPool();
        TableTuple templateTuple = m_templateTuple.tuple();
        for (int i = 0; i < m_columnIndices.size(); i++) {
            templateTuple.setNValueAllocateForObjectCopies(i,
                                                           source.getNValue(m_columnIndices[i]),
                                                           tempPool);
        }
        m_destTable->insertTuple(templateTuple);
    }

    AbstractExpression* SQLStreamHandler::parsePredicate(catalog::SQLStreamHandlerInfo *info) {
        const string& hexString = info->predicate();
        if (hexString.size() == 0) {
            return NULL;
        }
        assert (hexString.length() % 2 == 0);
        int bufferLength = (int)hexString.size() / 2 + 1;
        boost::shared_array<char> buffer(new char[bufferLength]);
        catalog::Catalog::hexDecodeString(hexString, buffer.get());

        PlannerDomRoot domRoot(buffer.get());
        if (domRoot.isNull()) {
            return NULL;
        }
        PlannerDomValue expr = domRoot.rootObject();
        return AbstractExpression::buildExpressionTree(expr);
    }

} // namespace voltdb
