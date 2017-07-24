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

package org.voltdb;

import java.util.Iterator;

import org.voltdb.PartitionDRGateway.DRConflictType;
import org.voltdb.PartitionDRGateway.DRRecordType;
import org.voltdb.VoltTableRow;

/**
 * This interface defines the contract of DR conflict resolution, including conflict details
 * and possible resolution actions. The conflict occurs because a local cluster's change to
 * a row occurs in the same time window as a remote cluster's change to the row, such that
 * the remote cluster's change was not yet locally visible.
 */
public interface DRConflictResolver {

    /**
     * Wrapper around a (potentially null) {@link VoltTableRow} with accessors
     * for conflict resolution metadata
     */
    public interface ConflictRow {
        /**
         * @return whether the underlying row is {@code null}
         */
        public boolean isNull();

        /**
         * @return the ID of the last cluster to modify this row
         * @throws IllegalStateException if {#isNull()}
         */
        public int getClusterId();

        /**
         * @return the last modification timestamp for this row
         * @throws IllegalStateException if {#isNull()}
         */
        public long getTimestamp();

        /**
         * @return {@code true} if and only if this row is a non-null
         * row from the local cluster that conflicts with a remote row on its
         * primary key
         */
        public boolean conflictsOnPrimaryKey();

        /**
         * @return the underlying {@link VoltTableRow}
         */
        public VoltTableRow getRow();
    }

    /**
     * This interface represents a general DR conflict
     */
    public interface Conflict {
        /**
         * @return the {@link DRConflictType} of this conflict
         */
        public DRConflictType getType();

        /**
         * @return whether it would be possible to accept the change from the
         * remote cluster
         */
        public boolean canResolve();

        /**
         * @return the partition where this conflict is being reported
         */
        public int getPartitionId();

        /**
         * @return the ID of the cluster performing the remote change
         */
        public int getRemoteClusterId();

        /**
         * @return the timestamp of the remote change
         */
        public long getRemoteTimestamp();

        /**
         * @return the name of the table on which this conflict is being reported
         */
        public String getTableName();

        /**
         * @return the {@link DRRecordType} generating this conflict. Can be
         * one of: {@link DRRecordType#DELETE}, {@link DRRecordType#INSERT},
         * {@link DRRecordType#UPDATE}
         */
        public DRRecordType getAction();

        /**
         * Report that you believe the selected resolution for this conflict
         * could be a source of divergence
         */
        public void flagDivergence();

        /**
         * @return {@code true} if the conflict has not been resolved or it has
         * been flagged divergent
         */
        public boolean isResolutionDivergent();

        /**
         * @return {@code true} if the selected resolution was to accept the
         * change from the remote cluster
         * @throws IllegalStateException if the conflict has not yet been
         * resolved
         */
        public boolean isRemoteChangeAccepted();

        /**
         * Select whether or not to accept the change from the remote cluster
         *
         * @param acceptRemoteChange
         * @throws UnsupportedOperationException if {@link #canResolve()} is
         * {@code false}
         */
        public void resolve(boolean acceptRemoteChange);

        /**
         * @return {@code true} either after a call to {@link #resolve(boolean)}
         * or immediately if {@link #canResolve()} is {@code false}
         */
        public boolean isResolved();
    }

    /**
     * A {@link Conflict} representing a situation in which a row the remote
     * cluster is attempting to delete is either missing or has an unexpected
     * timestamp or last modifying cluster
     */
    public interface ExpectedRowConflict extends Conflict {
        /**
         * @return the {@link ConflictRow} expected by the remote change
         */
        public ConflictRow getExpectedRow();

        /**
         * @return {@code true} if the expected row is present, but with an
         * unexpected timestamp or last modifying cluster
         */
        public boolean hasExistingRow();

        /**
         * @return the local {@link ConflictRow} with the same primary key as
         * the expected row. Note, {@link ConflictRow#isNull()} will return the
         * same value as {@link #hasExistingRow()}
         */
        public ConflictRow getExistingRow();
    }

    /**
     * Shared interface for update and insert {@link Conflict}s
     */
    public interface NewRowConflict extends Conflict {
        /**
         * @return the {@link ConflictRow} representing the remote row for this
         * change. Can be null.
         */
        public ConflictRow getNewRow();
    }

    /**
     * A {@link Conflict} representing a situation where a remotely inserted
     * row collides on a uniqueness constraint (primary key or otherwise) with
     * one or more locally inserted/updated rows
     */
    public interface ConstraintViolation extends NewRowConflict {
        /**
         * @return an {@link Iterator} over the local {@link ConflictRow}s
         * conflicting with the remote row
         */
        public Iterator<ConflictRow> getExistingRows();
    }

    /**
     * A {@link Conflict} encapsulating the conflict or conflicts generated
     * by a remotely updated row. The consumer must ensure that this update
     * conflict contains a given type of conflict before asking for information
     * about that conflict. For example: <pre><code>
     * UpdateConflict conflict = ...
     * if (conflict.hasExpectedRowConflict()) &#123
     *     ConflictRow row = conflict.getExpectedRow();
     *     ...
     * &#125</code></pre>
     */
    public interface UpdateConflict extends NewRowConflict {
        /**
         * @return whether the row the remote update is attempting to modify
         * is either missing or has an unexpected timestamp
         */
        public boolean hasExpectedRowConflict();

        /**
         * @return the {@link ConflictRow} expected by the remote change
         * @throws IllegalStateException if {@link #hasExpectedRowConflict()} is {@code false}
         */
        public ConflictRow getExpectedRow();

        /**
         * @return whether the row the remote update is attempting to modify is missing
         */
        public boolean hasMissingRow();

        /**
         * @return whether the row the remote update is attempting to modify is present,
         * but has an unexpected timestamp
         */
        public boolean hasTimestampMismatch();

        /**
         * @return the local {@link ConflictRow} with the same primary key as
         * the expected row, but a mismatched timestamp
         * @throws IllegalStateException if {@link #hasTimestampMismatch()} is {@code false}
         */
        public ConflictRow getExistingRowForTimestampMismatch();

        /**
         * @return whether the target values for the remote update collide on a
         * uniqueness constraint with one or more locally inserted/updated rows
         */
        public boolean hasConstraintViolation();

        /**
         * @return an {@link Iterator} over the local {@link ConflictRow}s
         * conflicting with the remote row
         * @throws IllegalStateException if {@link #hasConstraintViolation()} is {@code false}
         */
        public Iterator<ConflictRow> getExistingRowsForConstraintViolation();
    }
}
