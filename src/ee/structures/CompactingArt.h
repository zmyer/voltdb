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

#ifndef COMPACTINGART_H_
#define COMPACTINGART_H_

#include "ContiguousAllocator.h"

#include <cstdlib>
#include <stdint.h>
#include <utility>
#include <limits>
#include <cassert>
#include <type_traits>
#include "ar_prefix_tree.h"
#include "ar_tree.h"

namespace voltdb {

/**
* ART
*/

namespace detail {
        template<typename _Pair>
        struct Select1st : public std::unary_function<_Pair, typename _Pair::first_type> {

            typename _Pair::first_type &operator()(_Pair &__x) const {
                return __x.first;
            }

            const typename _Pair::first_type &operator()(const _Pair &__x) const {
                return __x.first;
            }
        };
    }

template<typename KeyValuePair, typename Compare,
    bool hasRank=false>
class CompactingArt {
    typedef typename KeyValuePair::first_type Key;
    typedef typename KeyValuePair::second_type Data;
    typedef std::pair<const Key, Data> value_type;
    // typedef typename art::key_transform<Key> _Key_transform;

protected:
    int64_t m_count;
    ContiguousAllocator m_allocator;
    bool m_unique;

    // templated comparison function object
    // follows STL conventions
    Compare m_comper;

private:
        //typedef typename _Alloc::value_type _Alloc_value_type;

        /**
         * Switch between art implementation with and without path compression
         * (prefixes in nodes) based on (transformed) key length. For short keys,
         * the cost of handling prefixes outweigh the benefits.
         */
        typedef typename art::ar_prefix_tree<Key, value_type,
                        detail::Select1st<value_type>> _Rep_type;

        _Rep_type _M_t;

public:
    // Bidirectional iterator
    typedef typename _Rep_type::iterator iterator;
    typedef typename _Rep_type::const_iterator const_iterator;
    typedef typename _Rep_type::size_type size_type;
    typedef typename _Rep_type::difference_type difference_type;
    typedef typename _Rep_type::reverse_iterator reverse_iterator;
    typedef typename _Rep_type::const_reverse_iterator const_reverse_iterator;

    // TODO observer
    /*
    class value_compare : public std::binary_function<value_type, value_type, bool> {

            friend class CompactingArt<KeyValuePair, Compare, hasRank>;

        protected:
            _Key_transform key_transformer;

            value_compare(_Key_transform __key_transformer)
                    : key_transformer(__key_transformer) {}

        public:
            typedef typename art::ar_prefix_tree<Key, Data, _Key_transform>::Key transformed_key_type;

            bool operator()(const value_type &__x, const value_type &__y) const {
                transformed_key_type x_key = {key_transformer(__x.first)};
                transformed_key_type y_key = {key_transformer(__y.first)};
                for (int i = 0; i < sizeof(transformed_key_type); i++) {
                    if (x_key.chunks[i] < y_key.chunks[i])
                        return true;
                    if (x_key.chunks[i] > y_key.chunks[i])
                        return false;
                }
                return false;
            }
        };
    */
    CompactingArt(bool unique, Compare comper);
    ~CompactingArt();

    // TODO: remove this. But two eecheck depend on this.
    bool insert(std::pair<Key, Data> value) { return (insert(value.first, value.second) == NULL); };
    // A syntactically convenient analog to CompactingHashTable's insert function
    const Data *insert(const Key &key, const Data &data);
    bool erase(const Key &key);
    bool erase(iterator &iter);

    iterator find(const Key &key)  { return _M_t.find(key); }
    const_iterator find(const Key &key) const { return _M_t.find(key); }
    // Not support rank
    iterator findRank(int64_t ith) const { return NULL; }
    int64_t size() const noexcept { return  _M_t.size(); }

    const_iterator lowerBound(const Key &key) const;
    const_iterator upperBound(const Key &key) const;

    std::pair<const_iterator, const_iterator> equalRange(const Key &key) const;
    std::pair<iterator, iterator> equalRange(const Key &key);

    size_t bytesAllocated() const { return m_allocator.bytesAllocated(); }

    // TODO(xin): later rename it to rankLower
    // Must pass a key that already in map, or else return -1
    int64_t rankAsc(const Key& key) const;
    int64_t rankUpper(const Key& key) const;

        // Iterators
        /**
         * Returns an iterator that points to the first element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        iterator begin() noexcept {
            return _M_t.begin();
        }

        /**
         * Returns a read-only iterator that points to the first element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        const_iterator begin() const noexcept {
            return _M_t.begin();
        }

        /**
         * Returns a read-only iterator that points to the first element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        const_iterator cbegin() const noexcept {
            return _M_t.begin();
        }

        /**
         * Returns a iterator that points to the last element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        iterator end() noexcept {
            return _M_t.end();
        }

        /**
         * Returns a read-only iterator that points to the last element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        const_iterator end() const noexcept {
            return _M_t.end();
        }

        /**
         * Returns a read-only iterator that points to the last element in the map.
         * Iteration is done in ascending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        const_iterator cend() const noexcept {
            return _M_t.end();
        }

        /**
         * Returns a reverse iterator that points to the first element in the map.
         * Iteration is done in descending order acoording to the comparison
         * of the transformed keys' binary representations.
         */
        reverse_iterator rbegin() noexcept {
            return _M_t.rbegin();
        }

        /**
         * Returns a read-only reverse iterator that points to the first element
         * in the map. Iteration is done in descending order acoording to the
         * comparison of the transformed keys' binary representations.
         */
        const_reverse_iterator rbegin() const noexcept {
            return _M_t.rbegin();
        }

        /**
         * Returns a read-only reverse iterator that points to the first element
         * in the map. Iteration is done in descending order acoording to the
         * comparison of the transformed keys' binary representations.
         */
        const_reverse_iterator crbegin() const noexcept {
            return _M_t.rbegin();
        }

        /**
         * Returns a reverse iterator that points to the last element
         * in the map. Iteration is done in descending order acoording to the
         * comparison of the transformed keys' binary representations.
         */
        reverse_iterator rend() noexcept {
            return _M_t.rend();
        }

        /**
         * Returns a read-only reverse iterator that points to the last element
         * in the map. Iteration is done in descending order acoording to the
         * comparison of the transformed keys' binary representations.
         */
        const_reverse_iterator rend() const noexcept {
            return _M_t.rend();
        }

        /**
         * Returns a read-only reverse iterator that points to the last element
         * in the map. Iteration is done in descending order acoording to the
         * comparison of the transformed keys' binary representations.
         */
        const_reverse_iterator crend() const noexcept {
            return _M_t.rend();
        }

protected:
    // main internal functions
};

template<typename KeyValuePair, typename Compare, bool hasRank>
CompactingArt<KeyValuePair, Compare, hasRank>::CompactingArt(bool unique, Compare comper)
    : m_count(0),
      // m_root(&NIL),
      // m_allocator(static_cast<int>(sizeof(TreeNode) - (hasRank ? 0 : sizeof(NodeCount))), static_cast<int>(10000)),
      m_unique(unique),
      // NIL(&NIL, &NIL, INVALIDCT),
      m_comper(comper),
      _M_t()
{ }

template<typename KeyValuePair, typename Compare, bool hasRank>
CompactingArt<KeyValuePair, Compare, hasRank>::~CompactingArt()
{
    _M_t.clear();
}

template<typename KeyValuePair, typename Compare, bool hasRank>
bool CompactingArt<KeyValuePair, Compare, hasRank>::erase(const Key &key)
{
    // TODO: cast from size type to bool
    return _M_t.erase_unique(key);
}

template<typename KeyValuePair, typename Compare, bool hasRank>
bool CompactingArt<KeyValuePair, Compare, hasRank>::erase(iterator &iter)
{
    _M_t.erase(&iter);
    return true;
}

template<typename KeyValuePair, typename Compare, bool hasRank>
const typename CompactingArt<KeyValuePair, Compare, hasRank>::Data *
CompactingArt<KeyValuePair, Compare, hasRank>::insert(const Key &key, const Data &value)
{
    std::pair<const Key, const void*> valuePair(key, value);
    _M_t.insert_unique(valuePair);
    m_count++;
    // TODO: handle conflict
    return NULL;
}

template<typename KeyValuePair, typename Compare, bool hasRank>
typename CompactingArt<KeyValuePair, Compare, hasRank>::const_iterator
CompactingArt<KeyValuePair, Compare, hasRank>::lowerBound(const Key &key) const
{
    return _M_t.lower_bound(key);
}

template<typename KeyValuePair, typename Compare, bool hasRank>
typename CompactingArt<KeyValuePair, Compare, hasRank>::const_iterator
CompactingArt<KeyValuePair, Compare, hasRank>::upperBound(const Key &key) const
{
   return  _M_t.upper_bound(key);
}

template<typename KeyValuePair, typename Compare, bool hasRank>
typename std::pair<typename CompactingArt<KeyValuePair, Compare, hasRank>::const_iterator,
                   typename CompactingArt<KeyValuePair, Compare, hasRank>::const_iterator>
CompactingArt<KeyValuePair, Compare, hasRank>::equalRange(const Key &key) const
{
    return _M_t.equal_range(key);
}

} // namespace voltdb

#endif // CompactingArt_H_
