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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import java.util.Arrays;

/**
 * This class is used to create the shortest-path-tree from linked entities.
 * <p>
 *
 */
public class MinimumWeightMultiTreeSPEntry extends MultiTreeSPEntry {
	private double minimumWeight = Double.POSITIVE_INFINITY;
	private int numInfiniteWeights = 0;

	public MinimumWeightMultiTreeSPEntry(int adjNode, int edgeId, double edgeWeight, boolean updated, MinimumWeightMultiTreeSPEntry parent, int numTrees) {
		super(adjNode,edgeId, edgeWeight, updated, parent, numTrees);
		updateWeights();
	}

	public void setSubItemOriginalEdgeIds(int newOriginalEdgeId) {
		for (MultiTreeSPEntryItem item : items) {
			item.setOriginalEdge(newOriginalEdgeId);
		}
	}


	@Override
	public void updateWeights() {
		double averageWeight = 0;
		int numNonInfiniteWeights = 0;
		for (int i = 0; i < items.length; i++) {
			MultiTreeSPEntryItem item = items[i];
//			if(item.getWeight() < minimumWeight) {
//				minimumWeight = item.getWeight();
//			}
			if(item.getWeight() != Double.POSITIVE_INFINITY) {
				averageWeight += item.getWeight();
				numNonInfiniteWeights++;
			}
		}
		minimumWeight = averageWeight / numNonInfiniteWeights;
	}

	@Override
	public int compareTo(MultiTreeSPEntry other) {
		MinimumWeightMultiTreeSPEntry o = (MinimumWeightMultiTreeSPEntry) other;

		if (minimumWeight < o.minimumWeight)
			return -1;

		// assumption no NaN and no -0
		return minimumWeight > o.minimumWeight ? 1 : 0;
	}

	@Override
	public String toString() {
		return adjNode + " (" + 0 + ") weights: " + Arrays.toString(items);
	}
}
