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

/**
 * The Class DownloadableGenomeTreeModel provides a tree model to select to downloadable genome by user.
 */
public class DownloadableGenomeTreeModel implements TreeModel {
    /**
     * The downloadable genome set.
     */
    private DownloadableGenomeSet genomes;
    /**
     * The indexes sorted by characters.
     */
    private Character[] charIndexes;
    /**
     * The genome lists.
     */
    private GenomeList[] genomeLists = null;
    /**
     * The genome lists for each character index.
     */
    private Map<Character, Vector<GenomeList>> keepers = null;

    /**
     * Initiate a new downloadable tree model.
     *
     * @param genomes the downloadable genome set.
     */
    public DownloadableGenomeTreeModel(DownloadableGenomeSet genomes) {
        this.genomes = genomes;

        HashSet<Character> usedChars = new HashSet<Character>();

        genomeLists = DownloadableGenomeSet.getGenomeLists().toArray(new GenomeList[0]);
        for (GenomeList genomeList : genomeLists) {
            usedChars.add(genomeList.getDisplayName().charAt(0));

        }

        charIndexes = usedChars.toArray(new Character[0]);
        Arrays.sort(charIndexes);

        keepers = new HashMap<Character, Vector<GenomeList>>(charIndexes.length);
        for (char c : charIndexes) {
            Vector<GenomeList> vec = new Vector<GenomeList>();
            for (GenomeList genomeList : genomeLists) {
                if (genomeList.getDisplayName().charAt(0) == c) {
                    vec.add(genomeList);
                }
            }
            keepers.put(c, vec);
        }
    }

    @Override
    public Object getRoot() {
        return genomes;
    }

    @Override
    public Object getChild(Object parent, int index) {

        if (parent instanceof DownloadableGenomeSet) {
            return charIndexes[index];
        } else if (parent instanceof Character) {
            return keepers.get(parent).get(index);
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof DownloadableGenomeSet) {
            return charIndexes.length;
        } else if (parent instanceof Character) {
            return keepers.get(parent).size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof GenomeList;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {

        if (parent instanceof GenomeList) {
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
            Vector<GenomeList> vec = keepers.get(parent);
            if (child instanceof GenomeList)
                return vec.indexOf(child);
        }

        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }

}
