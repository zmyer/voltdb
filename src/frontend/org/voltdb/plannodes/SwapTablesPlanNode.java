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

package org.voltdb.plannodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json_voltpatches.JSONException;
import org.json_voltpatches.JSONObject;
import org.json_voltpatches.JSONStringer;
import org.voltdb.catalog.CatalogMap;
import org.voltdb.catalog.Constraint;
import org.voltdb.catalog.Database;
import org.voltdb.catalog.Index;
import org.voltdb.catalog.Table;
import org.voltdb.types.PlanNodeType;

public class SwapTablesPlanNode extends AbstractOperationPlanNode {
    private static class Members {
        final static String OTHER_TARGET_TABLE_NAME = "OTHER_TARGET_TABLE_NAME";
        static final String INDEXES = "INDEXES";
        static final String OTHER_INDEXES = "OTHER_INDEXES";
    }

    private String m_otherTargetTableName;
    private List<String> m_theIndexes;
    private List<String> m_otherIndexes;

    public SwapTablesPlanNode() {
        super();
    }

    @Override
    public PlanNodeType getPlanNodeType() { return PlanNodeType.SWAPTABLES; };

    @Override
    public void toJSONString(JSONStringer stringer) throws JSONException {
        super.toJSONString(stringer);
        stringer.keySymbolValuePair(Members.OTHER_TARGET_TABLE_NAME,
                m_otherTargetTableName);
        toJSONStringArrayString(stringer, Members.INDEXES, m_theIndexes);
        toJSONStringArrayString(stringer, Members.OTHER_INDEXES, m_otherIndexes);
    }

    @Override
    public void loadFromJSONObject(JSONObject jobj, Database db)
            throws JSONException {
        super.loadFromJSONObject(jobj, db);
        m_otherTargetTableName = jobj.getString(Members.OTHER_TARGET_TABLE_NAME);
        m_theIndexes = loadStringListMemberFromJSON(jobj, "INDEXES");
        m_otherIndexes = loadStringListMemberFromJSON(jobj, "OTHER_INDEXES");
    }

    @Override
    protected String explainPlanForNode(String indent) {
        return "SWAP TABLE " + getTargetTableName() + " WITH " + m_otherTargetTableName;
    }

    @Override
    public boolean isOrderDeterministic() { return true; }

    public void init(Table theTable, Table otherTable) {
        String theTableName = theTable.getTypeName();
        setTargetTableName(theTableName);
        String otherTableName = otherTable.getTypeName();
        m_otherTargetTableName = otherTableName;
        m_theIndexes = new ArrayList<>();
        m_otherIndexes = new ArrayList<>();

        CatalogMap<Index> candidateIndexes = otherTable.getIndexes();

        for (Index theIndex : theTable.getIndexes()) {
            String theName = theIndex.getTypeName();
            String otherName = theName.replace(theTableName, otherTableName);
            // FIXME: for unhappy path, error out rather than assert
            assert(candidateIndexes.getIgnoreCase(otherName) != null);
            m_theIndexes.add(theName);
            m_otherIndexes.add(otherName);
        }

        // The indexes underlying the constraints also need to be
        // paired up by index name. For the other table, capture
        // the canonically generated names of all such indexes.
        Set<String> candidatesFromConstraints = new HashSet<>();

        for (Constraint otherConstraint : otherTable.getConstraints()) {
            Index otherIndex = otherConstraint.getIndex();
            if (otherIndex == null) {
                continue;
            }
            candidatesFromConstraints.add(otherIndex.getTypeName());
        }

        for (Constraint theConstraint : theTable.getConstraints()) {
            Index theIndex = theConstraint.getIndex();
            if (theIndex == null) {
                continue;
            }
            String theName = theIndex.getTypeName();
            String otherName = theName.replace(theTableName, otherTableName);
            // FIXME: for unhappy path, error out rather than assert
            assert(candidatesFromConstraints.contains(otherName));
            m_theIndexes.add(theName);
            m_otherIndexes.add(otherName);
        }
    }

}
