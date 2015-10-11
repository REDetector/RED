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
            RedCommandRunner.run(args);
        }
    }
}
