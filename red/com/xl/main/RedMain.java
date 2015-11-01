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

package com.xl.main;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.xl.utils.EmptyChecker;

/**
 * Created by Administrator on 2015/10/11.
 */
public class RedMain {
    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure(ClassLoader.getSystemResource("com/xl/preferences/logbackConfig.xml"));
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (EmptyChecker.isEmptyArray(args)) {
            RedApplication.run(args);
        } else {
            RedCmdLineTool.run(args);
        }
    }

    public static void runRefilter() {
        // java -jar /home/seq/softWare/refilter/RED.jar \
        // -H localhost \
        // -p 3306 \
        // -u seq \
        // -P sequencing \
        // -d DENOVO_REVISION \
        // -m denovo \
        // -e CT \
        // -o /data/RED/revision/red_out/ex \
        // -r /usr/bin/Rscript \
        // -O 123546 \
        // --rnavcf=/data/RED/revision/VariantDiscovery_results/all_samples_results/allsamples.snvs.hard.filtered.vcf \
        // --repeat=/data/RED/hg19.fa.out \
        // --splice=/data/RED/genes.gtf \
        // --dbsnp=/data/RED/dbsnp_138.hg19.vcf \
        // --darned=/data/RED/hg19.txt \
        // --radar=/data/RED/Human_AG_all_hg19_v2.txt
        for (int i = 0; i < 6; i++) {
            char[] chars = new char[6];
            for (int j = 0; j < 6; j++) {
                chars[j] = j == i ? '1' : '0';
            }
            String order = new String(chars);

        }
        String[] args = { "-H", "localhost", "-p", "3306", "-u", "seq", "-P", "sequencing", "-d", "DENOVO_SHARMA", "-m",
            "denovo", "-e", "CT", "-o", "/data/RED/revision/red_out/single_filter", "-r", "/usr/bin/Rscript", "-O",
            "000001",
            "--rnavcf=/data/RED/revision/VariantDiscovery_results/all_samples_results/sharma.allsamples.snvs.vcf",
            "--repeat=/data/RED/hg19.fa.out", "--splice=/data/RED/genes.gtf", "--dbsnp=/data/RED/dbsnp_138.hg19.vcf",
            "--darned=/data/RED/hg19.txt", "--radar=/data/RED/Human_AG_all_hg19_v2.txt" };
        RedCmdLineTool.run(args);
    }
}
