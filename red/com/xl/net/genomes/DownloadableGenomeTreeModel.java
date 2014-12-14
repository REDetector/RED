/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.net.genomes;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class DownloadableGenomeTreeModel implements TreeModel {

    private DownloadableGenomeSet genomes;
    private Character[] charIndexes;
    private GenomeLists[] genomeLists = null;
    private Map<Character, Vector<GenomeLists>> keepers = null;

    public DownloadableGenomeTreeModel(DownloadableGenomeSet genomes) {
        this.genomes = genomes;

        HashSet<Character> usedChars = new HashSet<Character>();

        genomeLists = DownloadableGenomeSet.getGenomeLists().toArray(new GenomeLists[0]);
        // for(GenomeLists genome: genomeLists){
        // System.out.println(genome.getGenomeDisplayName()+" "+genome.getGenomeDownloadLocation()+" "+genome.getGenomeId());
        // }
        for (int s = 0; s < genomeLists.length; s++) {
            usedChars.add(genomeLists[s].getDisplayName().charAt(0));
        }

        charIndexes = usedChars.toArray(new Character[0]);

        Arrays.sort(charIndexes);
        // for(Character c : charIndexes){
        // System.out.print(c);
        // }
        keepers = new HashMap<Character, Vector<GenomeLists>>(charIndexes.length);
        for (int i = 0; i < charIndexes.length; i++) {
            Vector<GenomeLists> vec = new Vector<GenomeLists>();
            for (int s = 0; s < genomeLists.length; s++) {
                if (genomeLists[s].getDisplayName().charAt(0) == charIndexes[i]) {
                    vec.add(genomeLists[s]);
                }
            }
            keepers.put(charIndexes[i], vec);
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public Object getChild(Object parent, int index) {

        if (parent instanceof DownloadableGenomeSet) {
            return charIndexes[index];
        } else if (parent instanceof Character) {
            return keepers.get(parent).get(index);
        } else {
            return null;
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof DownloadableGenomeSet) {
            return charIndexes.length;
        } else if (parent instanceof Character) {
            return keepers.get(parent).size();
        }
        return 0;
    }

    public int getIndexOfChild(Object parent, Object child) {

        if (parent instanceof GenomeLists) {
            for (int i = 0; i < genomeLists.length; i++) {
                if (genomeLists[i] == child) {
                    return i;
                }
            }
        } else if (parent instanceof DownloadableGenomeSet) {
            for (int i = 0; i < charIndexes.length; i++) {
                if (child == charIndexes[i]) {
                    return i;
                }
            }
        } else if (parent instanceof Character) {
            Vector<GenomeLists> vec = keepers.get(parent);
            return vec.indexOf(child);
        }

        return -1;
    }

    public Object getRoot() {
        return genomes;
    }

    public boolean isLeaf(Object node) {
        if (node instanceof GenomeLists) {
            return true;
        }
        return false;
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

}
