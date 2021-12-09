package com.tencentcloudapi.cls;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;
import static org.junit.Assert.assertNotEquals;

public class LoghubAppenderTest {

//    private static final Logger LOGGER = LogManager.getLogger(LoghubAppenderTest.class);
//
//    @AfterClass
//    public static void checkStatusDataList() {
//        try {
//            Thread.sleep(2 * 2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        List<StatusData> statusDataList = StatusLogger.getLogger().getStatusData();
//        for (StatusData statusData : statusDataList) {
//            statusData.getLevel();
//        }
//        for (StatusData statusData : statusDataList) {
//            Level level = statusData.getLevel();
//            assertNotEquals(statusData.getMessage().toString(), Level.ERROR, level);
//        }
//    }
//
//
//    @Test
//    public void testLogThrowable() throws InterruptedException {
//        ThreadContext.put("THREAD_ID1", "name1");
//        ThreadContext.put("THREAD_ID2", "name2");
//        LOGGER.error("This is a test error message logged by log4j2.",
//                new UnsupportedOperationException("Log4j2 UnsupportedOperationException"));
//
//        Thread.sleep(1000 * 4);
//    }
//
//    @Test
//    public void testLogLevelInfo() {
//        LOGGER.info("This is a test error message logged by log4j2, level is info, should not be logged.");
//    }
}
