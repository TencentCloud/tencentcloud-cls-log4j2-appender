package com.tencentcloudapi.cls;

import com.tencentcloudapi.cls.producer.Callback;
import com.tencentcloudapi.cls.producer.Result;
import com.tencentcloudapi.cls.producer.common.LogItem;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author farmerx
 */
public class LoghubAppenderCallback implements Callback {

    private Logger logger;

    private String topicId;

    private String source;

    private List<LogItem> logItems;

    public LoghubAppenderCallback(Logger logger, String topicId,
                                  String source, List<LogItem> logItems) {
        super();
        this.logger = logger;
        this.topicId = topicId;
        this.source = source;
        this.logItems = logItems;
    }

    @Override
    public void onCompletion(Result result) {
        if (!result.isSuccessful()) {
            logger.error(
                    "Failed to send log"
                            + ", topicId=" + topicId
                            + ", source=" + source
                            + ", logItem=" + logItems
                            + ", errorCode=" + result.getErrorCode()
                            + ", errorMessage=" + result.getErrorMessage());
        }
    }
}
