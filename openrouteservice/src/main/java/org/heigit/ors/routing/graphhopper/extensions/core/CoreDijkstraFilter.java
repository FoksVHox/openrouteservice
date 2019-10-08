/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;

import java.util.HashMap;
/**
 * Only certain nodes are accepted and therefor the others are ignored.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Andrzej Oles, Hendrik Leuschner
 */
public class CoreDijkstraFilter implements EdgeFilter {
    private final CHGraph graph;
    private final int maxNodes;
    private final int coreNodeLevel;
    EdgeFilter restrictions;

    boolean inCore = false;

    public void setInCore(boolean inCore) {
        this.inCore = inCore;
    }

    /**
     *
     * @param graph
     */
    public CoreDijkstraFilter(CHGraph graph) {
        this.graph = graph;
        maxNodes = graph.getNodes();
        coreNodeLevel = maxNodes + 1;
    }

    /**
     *
     * @param edgeIterState iterator pointing to a given edge
     * @return true iff the edge is virtual or is a shortcut or the level of the base node is greater/equal than
     * the level of the adjacent node
     */
    @Override
    
    public boolean accept(EdgeIteratorState edgeIterState) {
        int base = edgeIterState.getBaseNode();
        int adj = edgeIterState.getAdjNode();

        if (!inCore) {
            // always accept virtual edges, see #288
            if (base >= maxNodes || adj >= maxNodes)
                return true;
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;
            else
                return graph.getLevel(base) <= graph.getLevel(adj);
        }
        else {
            if (adj >= maxNodes)
                return false;
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;

            // do not follow virtual edges, and stay within core
            if (isCoreNode(adj))
                // if edge is in the core check for restrictions
                return restrictions.accept(edgeIterState);
            else
                return false;
        }
    }

    private boolean isCoreNode(int node) {
        return graph.getLevel(node) == coreNodeLevel;
    }

    public void addRestrictionFilter (EdgeFilter restrictions) {
        this.restrictions = restrictions;
    }
}
