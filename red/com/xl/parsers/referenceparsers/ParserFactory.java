/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.parsers.referenceparsers;

import com.xl.database.DatabaseManager;

/**
 * Created by Administrator on 2015/10/5.
 */
public class ParserFactory {

    public static AbstractParser createParser(String dataPath, String parserName) {
        if (parserName.equals(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)) {
            return new RNAVCFParser(dataPath);
        } else if (parserName.equals(DatabaseManager.DNA_VCF_RESULT_TABLE_NAME)) {
            return new DNAVCFParser(dataPath);
        } else if (parserName.equals(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME)) {
            return new GTFParser(dataPath, parserName);
        } else if (parserName.equals(DatabaseManager.DARNED_DATABASE_TABLE_NAME)) {
            return new DARNEDParser(dataPath, parserName);
        } else if (parserName.equals(DatabaseManager.REPEAT_MASKER_TABLE_NAME)) {
            return new RepeatMaskerParser(dataPath, parserName);
        } else if (parserName.equals(DatabaseManager.DBSNP_DATABASE_TABLE_NAME)) {
            return new DBSNPParser(dataPath, parserName);
        } else {
            return null;
        }
    }
}
