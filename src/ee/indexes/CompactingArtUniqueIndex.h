/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
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
/* Copyright (C) 2008 by H-Store Project
 * Brown University
 * Massachusetts Institute of Technology
 * Yale University
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

#ifndef COMPACTINGARTUNIQUEINDEX_H_
#define COMPACTINGARTUNIQUEINDEX_H_

#include <iostream>
#include <cassert>
#include <iterator>
#include "common/debuglog.h"
#include "common/tabletuple.h"
#include "indexes/tableindex.h"
// #include "structures/radix_map.h"
#include "structures/CompactingArt.h"
// #include "structures/CompactingMap.h"

namespace voltdb {

template<typename KeyValuePair, bool hasRank>
class CompactingArtUniqueIndex : public TableIndex
{
    typedef typename KeyValuePair::first_type KeyType;
    // typedef typename KeyValuePair::second_type DataType;
    typedef typename KeyType::KeyComparator KeyComparator;
    typedef CompactingArt<KeyValuePair, KeyComparator, hasRank> MapType;
    // typedef art::radix_map<KeyType, DataType> MapType;
    typedef typename MapType::iterator MapIterator;
    typedef typename MapType::const_iterator MapCIterator;

    MapType m_entries;

    ~CompactingArtUniqueIndex() {};

    static MapCIterator& castToIter(IndexCursor& cursor) {
        return *reinterpret_cast<MapCIterator*> (cursor.m_keyIter);
    }

    static MapCIterator& castToEndIter(IndexCursor& cursor) {
        return *reinterpret_cast<MapCIterator*> (cursor.m_keyEndIter);
    }

    std::string getTypeName() const { return "CompactingArtUniqueIndex"; };

        virtual TableIndex *cloneEmptyNonCountingTreeIndex() const
        {
            return new CompactingArtUniqueIndex<KeyValuePair, false >(TupleSchema::createTupleSchema(getKeySchema()), m_scheme);
        }


        MapCIterator findKey(const TableTuple *searchKey) const {
            return m_entries.find(KeyType(searchKey));
        }

        MapCIterator findTuple(const TableTuple &originalTuple) const {
            return m_entries.find(setKeyFromTuple(&originalTuple));
        }

        const KeyType setKeyFromTuple(const TableTuple *tuple) const
        {
            KeyType result(tuple, m_scheme.columnIndices, m_scheme.indexedExpressions, m_keySchema);
            return result;
        }

    void addEntryDo(const TableTuple *tuple, TableTuple *conflictTuple)
    {
        ++m_inserts;
        std::pair<const KeyType, const void*> valuePair(setKeyFromTuple(tuple), tuple->address());
        std::pair<MapIterator, bool> response = m_entries.insert(valuePair);
        if (!response->second && conflictTuple != NULL) {
            const void* const* conflictEntry = tuple->address();
            conflictTuple->move(const_cast<void*>(*conflictEntry));
        }
    }

    bool deleteEntryDo(const TableTuple *tuple)
    {
        // double lookup ?
        MapCIterator mapIter = findKey(tuple);
        if (mapIter == m_entries.cend()) {
             return false;
        }
        ++m_deletes;
        m_entries.erase(mapIter);
        return false;
    }

    /**
     * Update in place an index entry with a new tuple address
     */
    bool replaceEntryNoKeyChangeDo(const TableTuple &destinationTuple, const TableTuple &originalTuple)
    {
        assert(originalTuple.address() != destinationTuple.address());

        // full delete and insert for certain key types
        if (KeyType::keyDependsOnTupleAddress()) {
            if ( ! CompactingArtUniqueIndex::deleteEntry(&originalTuple)) {
                return false;
            }
            TableTuple conflict(destinationTuple.getSchema());
            CompactingArtUniqueIndex::addEntry(&destinationTuple, &conflict);
            return conflict.isNullTuple();
        }

        MapIterator mapIter = m_entries.find(setKeyFromTuple(&originalTuple));

        if (mapIter == m_entries.end()) {
            return false;
        }
        mapIter->second = destinationTuple.address();
        m_updates++;
        return true;
    }

    bool keyUsesNonInlinedMemory() const { return KeyType::keyUsesNonInlinedMemory(); }

    bool checkForIndexChangeDo(const TableTuple* lhs, const TableTuple* rhs) const
    {
        return  0 != m_cmp(setKeyFromTuple(lhs), setKeyFromTuple(rhs));
    }

    bool existsDo(const TableTuple *persistentTuple) const
    {
        return m_entries.find(setKeyFromTuple(persistentTuple)) != m_entries.cend();
    }

    bool moveToKey(const TableTuple *searchKey, IndexCursor& cursor) const
    {
        cursor.m_forward = true;
        MapCIterator &mapIter = castToIter(cursor);
        mapIter = m_entries.find(KeyType(searchKey));

        if (mapIter == m_entries.cend()) {
            cursor.m_match.move(NULL);
            return false;
        }
        cursor.m_match.move(const_cast<void*>(mapIter->second));
        return true;
    }

    bool moveToKeyByTuple(const TableTuple *persistentTuple, IndexCursor &cursor) const
    {
        cursor.m_forward = true;
        MapCIterator &mapIter = castToIter(cursor);
        mapIter = m_entries.find(setKeyFromTuple(persistentTuple));

        if (mapIter == m_entries.cend()) {
            cursor.m_match.move(NULL);
            return false;
        }
        cursor.m_match.move(const_cast<void*>(mapIter->second));
        return true;
    }

    void moveToKeyOrGreater(const TableTuple *searchKey, IndexCursor& cursor) const
    {
        cursor.m_forward = true;
        MapCIterator &mapIter = castToIter(cursor);

        mapIter = m_entries.lowerBound(KeyType(searchKey));
    }

    bool moveToGreaterThanKey(const TableTuple *searchKey, IndexCursor& cursor) const
    {
        cursor.m_forward = true;
        MapCIterator &mapIter = castToIter(cursor);
        mapIter = m_entries.upperBound(KeyType(searchKey));

        return (mapIter == m_entries.cend());
    }

    void moveToLessThanKey(const TableTuple *searchKey, IndexCursor& cursor) const
    {
        // do moveToKeyOrGreater()
        MapCIterator &mapIter = castToIter(cursor);
        mapIter = m_entries.lowerBound(KeyType(searchKey));

        // find prev entry
        if (mapIter == m_entries.cend()) {
            moveToEnd(false, cursor);
        } else {
            cursor.m_forward = false;
            mapIter++;
        }
    }

    // only be called after moveToGreaterThanKey() for LTE case
    void moveToBeforePriorEntry(IndexCursor& cursor) const
    {
        assert(cursor.m_forward);
        cursor.m_forward = false;
        MapCIterator &mapIter = castToIter(cursor);

        if (mapIter == m_entries.cend()) {
            mapIter = m_entries.rbegin();
        } else {
            // go back 2 entries
            // entries: [..., A, B, C, ...], currently mapIter = C (not NULL if reach here)
            // B is the entry we just evaluated and didn't pass initial_expression test (can not be NULL)
            // so A is the correct starting point (can be NULL)
            mapIter--;
        }
        mapIter--;
    }

    void moveToPriorEntry(IndexCursor& cursor) const
    {
        assert(cursor.m_forward);
        cursor.m_forward = false;
        MapCIterator &mapIter = castToIter(cursor);

        if (mapIter == m_entries.cend()) {
            mapIter = m_entries.rbegin();
        } else {
            mapIter--;
        }
    }

    void moveToEnd(bool begin, IndexCursor& cursor) const
    {
        cursor.m_forward = begin;
        MapCIterator &mapIter = castToIter(cursor);

        if (begin)
            mapIter = m_entries.begin();
        else
            mapIter = m_entries.rbegin();
    }

    TableTuple nextValue(IndexCursor& cursor) const
    {
        TableTuple retval(getTupleSchema());

        MapCIterator &mapIter = castToIter(cursor);

        if (mapIter != m_entries.cend()) {
            retval.move(const_cast<void*>(mapIter->second));
            if (cursor.m_forward) {
                mapIter++;
            } else {
                mapIter--;
            }
        }

        return retval;
    }

    TableTuple nextValueAtKey(IndexCursor& cursor) const
    {
        TableTuple retval = cursor.m_match;
        cursor.m_match.move(NULL);
        return retval;
    }

    bool advanceToNextKey(IndexCursor& cursor) const
    {
        MapCIterator &mapIter = castToIter(cursor);

        if (cursor.m_forward) {
            mapIter++;
        } else {
            mapIter--;
        }
        if (mapIter == m_entries.cend())
        {
            cursor.m_match.move(NULL);
            return false;
        }

        cursor.m_match.move(const_cast<void*>(mapIter->second));
        return true;
    }

    TableTuple uniqueMatchingTuple(const TableTuple &searchTuple) const
    {
        TableTuple retval(getTupleSchema());
        MapCIterator &keyIter = findTuple(searchTuple);
        if (keyIter != m_entries.cend()) {
            retval.move(const_cast<void*>(keyIter->second));
        }
        return retval;
    }

    bool hasKey(const TableTuple *searchKey) const
    {
        return findKey(searchKey) != m_entries.cend();
    }

    /**
     * @See comments in parent class TableIndex
     */
    int64_t getCounterGET(const TableTuple* searchKey, bool isUpper, IndexCursor& cursor) const {
        if (!hasRank) {
            return -1;
        }
        CompactingArtUniqueIndex::moveToKeyOrGreater(searchKey, cursor);
        MapCIterator &mapIter = castToIter(cursor);

        if (mapIter == m_entries.cend()) {
            return m_entries.size() + 1;
        }
        return m_entries.rankAsc(mapIter->first);
    }

    /**
     * See comments in parent class TableIndex
     */
    int64_t getCounterLET(const TableTuple* searchKey, bool isUpper, IndexCursor& cursor) const {
        if (!hasRank) {
           return -1;
        }
        const KeyType tmpKey(searchKey);
        MapCIterator mapIter = m_entries.lowerBound(tmpKey);
        if (mapIter == m_entries.cend()) {
            return m_entries.size();
        }
        int cmp = m_cmp(tmpKey, mapIter->first);
        if (cmp != 0) {
            mapIter--;
            if (mapIter == m_entries.cend()) {
                // we can not find a previous key
                return 0;
            }
        }
        return m_entries.rankAsc(mapIter->first);
    }

    size_t getSize() const { return m_entries.size(); }

    int64_t getMemoryEstimate() const
    {
        return m_entries.max_size();
    }

    std::string debug() const
    {
        std::ostringstream buffer;
        buffer << TableIndex::debug() << std::endl;
        MapIterator iter = m_entries.begin();
        while (iter != m_entries.cend()) {
            TableTuple retval(getTupleSchema());
            retval.move(const_cast<void*>(iter->second));
            buffer << retval.debugNoHeader() << std::endl;
            iter++;
        }
        std::string ret(buffer.str());
        return (ret);
    }

    // comparison stuff
    KeyComparator m_cmp;

public:
    CompactingArtUniqueIndex(const TupleSchema *keySchema, const TableIndexScheme &scheme) :
        TableIndex(keySchema, scheme),
        m_entries(),
        m_cmp(keySchema)
    {}
};

}

#endif // COMPACTINGARTUNIQUEINDEX_H_
