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

package com.xl.display.dialog;

import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * A class for performing search actions.  The class takes a view context and search string as parameters. The search string can be either
 * (a) a feature (e.g. gene),  or
 * (b) a locus string in the UCSC form,  e.g. chr1:100,000-200,000
 * <p/>
 * Note:  Currently the only recognized features are genes
 */
public class SearchCommand {

    public static int SEARCH_LIMIT = 20;
    private boolean askUser = false;

    private String searchString;
    private Genome genome;

    private static HashMap<ResultType, String> tokenMatchers;

    static {

        //Regexp for a number with commas in it (no periods)
        String numWithCommas = "(((\\d)+,?)+)";

        //chromosome can include anything except whitespace

        String chromosome = "(\\S)+";

        //This will match chr1:1-100, chr1:1, chr1  1, chr1 1   100
        String chromosomeRange = chromosome + "(:|(\\s)+)" + numWithCommas + "(-|(\\s)+)?" + numWithCommas + "?(\\s)*";

        //Simple feature
        String feature = "(\\S)+";

        tokenMatchers = new HashMap<ResultType, String>();
        tokenMatchers.put(ResultType.CHROMOSOME, chromosome);
        tokenMatchers.put(ResultType.FEATURE, feature);
        tokenMatchers.put(ResultType.LOCUS, chromosomeRange);
    }


    public SearchCommand(String searchString) {
        this(searchString, REDApplication.getInstance().dataCollection().genome());
    }

    SearchCommand(String searchString, Genome genome) {
        this.searchString = searchString.trim();
        this.genome = genome;
    }


    public void execute() {

        List<SearchResult> results = runSearch(searchString);
        if (askUser) {
            results = askUserFeature(results);
            if (results == null) {
                return;
            }
        }
        showSearchResult(results);
    }

    /**
     * Given a string, search for the appropriate data to show the user.
     * Different syntaxes are accepted.
     * <p/>
     * In general, whitespace delimited tokens are treated separately and each are shown.
     * There is 1 exception to this. A locus of form chr1   1   10000 will be treated the same
     * as chr1:1-10000. Only one entry of this form can be entered, chr1    1   10000 chr2:1-1000 will
     * not be recognized.
     *
     * @param searchString Feature name (EGFR), chromosome (chr1), or locus string (chr1:1-100 or chr1:6)
     *                     Partial matches to a feature name (EG) will return multiple results, and
     *                     ask the user which they want.
     * @return result
     * List<SearchResult> describing the results of the search. Will never be null, field type will equal ResultType.ERROR if something went wrong.
     */
    public List<SearchResult> runSearch(String searchString) {

        List<SearchResult> results = new ArrayList<SearchResult>();

        searchString = searchString.replaceAll("\"", "");

        Set<ResultType> wholeStringType = checkTokenType(searchString);
        if (wholeStringType.contains(ResultType.LOCUS)) {
            results.add(calcChromoLocus(searchString));
            return results;
        }

        // Space delimited?
        String[] tokens = searchString.split("\\s+");
        for (String s : tokens) {
            results.addAll(parseToken(s));
        }

        if (results.size() == 0) {
            SearchResult result = new SearchResult();
            result.setMessage("Invalid Search String: " + searchString);
            results.add(result);
        }

        return results;
    }

    public void showSearchResult(List<SearchResult> results) {
        SearchResult result = new SearchResult();
        if (results == null || results.size() == 0) {
            results = new ArrayList<SearchResult>();
            results.add(result);
        }
        boolean showMessage = false;
        String message = "Invalid search string: " + searchString;

        if (results.size() == 1) {
            result = results.get(0);
            switch (result.type) {
                case FEATURE:
                    DisplayPreferences.getInstance().setLocation(result.chr, result.start, result.end);
                    break;
                case LOCUS:
                    DisplayPreferences.getInstance().setLocation(result.chr, result.start, result.end);
                    break;
                case CHROMOSOME:
                    DisplayPreferences.getInstance().setLocation(result.chr, result.start, result.end);
                    break;
                case ERROR:
                default: {
                    message = "Cannot find feature or locus: " + searchString;
                    showMessage = true;
                }
            }
        } else {
            //            List<String> loci = new ArrayList<String>(results.size());
            //            message = "";
            //            for (SearchResult res : results) {
            //                if (res.type != ResultType.ERROR) {
            //                    loci.add(res.getLocus());
            //                } else {
            //                    message = message + res.getMessage() + "\n";
            //                    showMessage = true;
            //                }
            //            }
            result = results.get(0);
            DisplayPreferences.getInstance().setLocation(result.chr, result.start, result.end);
        }

        if (showMessage) {
            OptionDialogUtils.showMessageDialog(REDApplication.getInstance(), message);
        }
    }

    /**
     * Get a list of strings of feature names suitable for display, containing only
     * those search results which were not an error
     *
     * @param results  the result
     * @param longName Whether to use the long (true) or short (false)
     *                 of search results.
     * @return Array of strings of results found.
     */
    public static Object[] getSelectionList(List<SearchResult> results, boolean longName) {
        ArrayList<String> options = new ArrayList<String>(Math.min(results.size(), SEARCH_LIMIT));
        for (SearchResult result : results) {
            if (result.type == ResultType.ERROR) {
                continue;
            }
            if (longName) {
                options.add(result.getLongName());
            } else
                options.add(result.getShortName());
        }

        return options.toArray();
    }

    /**
     * Display a dialog asking user which search result they want
     * to display. Number of results are limited to SEARCH_LIMIT.
     * The user can select multiple options, in which case all
     * are displayed.
     *
     * @param results the search result
     * @return SearchResults which the user has selected.
     * Will be null if cancelled
     */
    private List<SearchResult> askUserFeature(List<SearchResult> results) {

        Object[] list = getSelectionList(results, true);
        JList ls = new JList(list);
        ls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final JOptionPane pane = new JOptionPane(new JScrollPane(ls), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        final JDialog dialog = pane.createDialog("Features");
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        //On double click, show that option
        ls.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    dialog.setVisible(false);
                    pane.setValue(JOptionPane.OK_OPTION);
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);

        if (pane.getValue() != null) {
            int resp = (Integer) pane.getValue();
            List<SearchResult> val = null;
            if (resp == JOptionPane.OK_OPTION) {
                int[] selected = ls.getSelectedIndices();
                val = new ArrayList<SearchResult>(selected.length);
                for (int ii = 0; ii < selected.length; ii++) {
                    val.add(ii, results.get(selected[ii]));
                }
            }
            return val;
        }
        return new ArrayList<SearchResult>();
    }

    /**
     * Check token type using regex.
     * Intended to be inclusive, returns all possible matches
     *
     * @param token the token
     * @return a set of result types
     */
    private Set<ResultType> checkTokenType(String token) {
        token = token.trim();

        Set<ResultType> possibles = new HashSet<ResultType>();
        for (ResultType type : tokenMatchers.keySet()) {
            if (token.matches(tokenMatchers.get(type))) { //note: entire string must match
                possibles.add(type);
            }
        }

        return possibles;
    }

    /**
     * Determine searchResult for white-space delimited search query.
     *
     * @param token the token
     * @return search result
     */
    private List<SearchResult> parseToken(String token) {

        List<SearchResult> results = new ArrayList<SearchResult>();

        //Guess at token type via regex. We don't assume success
        Set<ResultType> types = checkTokenType(token);
        SearchResult result;
        if (types.contains(ResultType.LOCUS) || types.contains(ResultType.CHROMOSOME)) {
            //Check if a full or partial locus string
            result = calcChromoLocus(token);
            if (result.type != ResultType.ERROR) {
                results.add(result);
                return results;
            }
        }

        if (types.contains(ResultType.FEATURE)) {
            //Check if we have an exact name for the feature name
            Feature[] features = REDApplication.getInstance().dataCollection().genome().getAnnotationCollection().getFeaturesForName(token.toLowerCase().trim());
            if (features != null) {
                for (Feature feature : features)
                    results.add(new SearchResult(feature));
                askUser |= features.length >= 2;
                return results;
            }
        }

        result = new SearchResult();
        result.setMessage("Invalid token: " + token);
        results.add(result);
        return results;

    }

    /**
     * Parse a string of locus coordinates.
     * Can have whitespace delimiters, and be missing second coordinate,
     * but must have 1st coordinate.
     *
     * @param searchString The chr, feature or locus to be searched.
     * @return The search result.
     */
    private SearchResult calcChromoLocus(String searchString) {
        /*
        chromosome can have whitespace or : delimiter
        chromosome also might have : in the name
         */
        int[] startEnd = null;
        String[] tokens = searchString.split("\\s+");

        String chr = tokens[0];
        boolean whitespace_delim = tokens.length >= 2;
        if (whitespace_delim) {
            String posString = tokens[1];
            if (tokens.length >= 3) {
                posString += "-" + tokens[2];
            }
            startEnd = getStartEnd(posString);
        } else {
            //Not whitespace delimited
            //Could be chr name:1-100, chr name:1, chr name

            int colonIdx = searchString.lastIndexOf(":");
            if (colonIdx > 0) {
                chr = searchString.substring(0, colonIdx);
                String posString = searchString.substring(colonIdx).replace(":", "");
                startEnd = getStartEnd(posString);
                //This MAY for case of chr name having semicolon in it
                if (startEnd == null) {
                    chr = searchString;
                }
            }
        }

        Chromosome chromosome = genome.getChromosome(chr);

        if (chromosome != null) {
            if (startEnd != null) {
                return new SearchResult(ResultType.LOCUS, chr, startEnd[0], startEnd[1]);
            }
            return new SearchResult(ResultType.CHROMOSOME, chr, 0, chromosome.getLength() - 1);
        }
        return new SearchResult(ResultType.ERROR, chr, -1, -1);
    }

    /**
     * Return the start and end positions as a 2 element array for the input
     * position string.  UCSC conventions  are followed for coordinates,
     * specifically the internal representation is "zero" based (first base is
     * numbered 0) and end-exclusive, but the display representation is "one" based (first base is
     * numbered 1) and end-inclusive.   Consequently 1 is subtracted from the parsed positions
     */
    private static int[] getStartEnd(String posString) {
        try {
            String[] posTokens = posString.split("-");
            String startString = posTokens[0].replaceAll(",", "");
            int start = Math.max(0, Integer.parseInt(startString)) - 1;

            // Default value for end
            int end = start + 1;
            if (posTokens.length > 1) {
                String endString = posTokens[1].replaceAll(",", "");
                end = Integer.parseInt(endString);
            }

            if (posTokens.length == 1 || (end - start) < 10) {
                int center = (start + end) / 2;
                int widen = 20;
                start = center - widen;
                start = Math.max(0, start);
                end = center + widen + 1;
            }

            return new int[]{Math.min(start, end), Math.max(start, end)};
        } catch (NumberFormatException numberFormatException) {
            return null;
        }

    }

    public enum ResultType {
        FEATURE,
        LOCUS,
        CHROMOSOME,
        ERROR
    }

    /*
    Container class for search results
     */
    public static class SearchResult {
        String chr;
        private int start;
        private int end;
        ResultType type;

        private String locus;
        private String message;
        private Feature feature;
        private String coords;

        public SearchResult() {
            this(ResultType.ERROR, null, -1, -1);
        }

        public SearchResult(ResultType type, String chr, int start, int end) {
            this.type = type;
            this.chr = chr;
            this.start = start;
            this.end = end;
            this.coords = chr + ":" + (start + 1) + "-" + end;
            this.locus = this.coords;
        }

        public SearchResult(Feature feature) {
            this(ResultType.FEATURE, feature.getChr(), feature.getTxLocation().getStart(), feature.getTxLocation().getEnd());
            this.feature = feature;
            this.locus = this.feature.getName();
        }

        void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }

        /**
         * Always a coordinate string.
         * eg chr1:1-100
         *
         * @return the coordinate
         */
        private String getCoordinates() {
            return this.coords;
        }

        /**
         * Either a feature name, or coordinates
         *
         * @return the locus
         */
        String getLocus() {
            return this.locus;
        }

        String getShortName() {
            if (this.type == ResultType.FEATURE) {
                return feature.getName() + " " + feature.getAliasName();
            } else {
                return getLocus();
            }
        }

        /**
         * Format for display. If a feature,
         * Feature name (chromosome:start-end)
         * eg EGFR (chr7:55,054,218-55,242,525)
         * <p/>
         * Otherwise, just locus
         *
         * @return the feature name.
         */
        String getLongName() {
            if (this.type == ResultType.FEATURE) {
                return feature.getName() + " " + feature.getAliasName() + " (" + this.getCoordinates() + ")";
            } else {
                return getLocus();
            }
        }

        public ResultType getType() {
            return type;
        }

        public String getChr() {
            return chr;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        //May be null
        public Feature getFeature() {
            return feature;
        }
    }
}