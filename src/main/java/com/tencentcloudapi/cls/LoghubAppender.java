package com.tencentcloudapi.cls;

import com.tencentcloudapi.cls.producer.AsyncProducerClient;
import com.tencentcloudapi.cls.producer.AsyncProducerConfig;
import com.tencentcloudapi.cls.producer.common.LogItem;
import com.tencentcloudapi.cls.producer.util.NetworkUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Plugin(name = "Loghub", category = "Core", elementType = "appender", printObject = true)
public class LoghubAppender extends AbstractAppender {
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static final String DEFAULT_TIME_ZONE = "UTC";

    protected String topicId;
    protected String endpoint;
    protected String accessKeyId;
    protected String accessKeySecret;

    protected int totalSizeInBytes;
    protected int maxBlockMs;
    protected int sendThreadCount;
    protected int batchSizeThresholdInBytes;
    protected int batchCountThreshold;
    protected int lingerMs;
    protected int retries;
    protected int baseRetryBackoffMs;
    protected int maxRetryBackoffMs;

    private String userAgent = "log4j2";
    private AsyncProducerClient producer;

    private String source;
    private DateTimeFormatter formatter;
    private String mdcFields;

    private AsyncProducerConfig producerConfig;

    protected LoghubAppender(String name,
                             Filter filter,
                             Layout<? extends Serializable> layout,
                             boolean ignoreExceptions,
                             String topicId,
                             String endpoint,
                             String accessKeyId,
                             String accessKeySecret,
                             int totalSizeInBytes,
                             int maxBlockMs,
                             int sendThreadCount,
                             int batchSizeThresholdInBytes,
                             int batchCountThreshold,
                             int lingerMs,
                             int retries,
                             int baseRetryBackoffMs,
                             int maxRetryBackoffMs,
                             String source,
                             DateTimeFormatter formatter,
                             String mdcFields
    ) {
        super(name, filter, layout, ignoreExceptions);
        this.topicId = topicId;
        this.endpoint = endpoint;
        this.accessKeySecret = accessKeySecret;
        this.accessKeyId = accessKeyId;
        this.totalSizeInBytes = totalSizeInBytes;
        this.retries = retries;
        this.sendThreadCount = sendThreadCount;
        this.maxBlockMs = maxBlockMs;
        this.batchCountThreshold = batchCountThreshold;
        this.batchSizeThresholdInBytes = batchSizeThresholdInBytes;
        this.lingerMs = lingerMs;
        this.baseRetryBackoffMs = baseRetryBackoffMs;
        this.maxRetryBackoffMs = maxRetryBackoffMs;
        this.source = source;
        if (Strings.isBlank(this.source)) {
            this.source = NetworkUtils.getLocalMachineIP();
        }
        this.formatter = formatter;
        this.mdcFields = mdcFields;
    }

    @Override
    public void start() {
        super.start();

        producerConfig = new AsyncProducerConfig(endpoint, accessKeyId, accessKeySecret, source);
        producerConfig.setBatchCountThreshold(batchCountThreshold);
        producerConfig.setBatchSizeThresholdInBytes(batchSizeThresholdInBytes);
        producerConfig.setSendThreadCount(sendThreadCount);
        producerConfig.setRetries(retries);
        producerConfig.setBaseRetryBackoffMs(baseRetryBackoffMs);
        producerConfig.setLingerMs(lingerMs);
        producerConfig.setMaxBlockMs(maxBlockMs);
        producerConfig.setMaxRetryBackoffMs(maxRetryBackoffMs);
        producer = new AsyncProducerClient(producerConfig);
    }

    @Override
    public void stop() {
        super.stop();
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception e) {
                this.error("Failed to close LoghubAppender.", e);
            }
        }
    }

    @Override
    public void append(LogEvent event) {
        LogItem item = new LogItem();
        item.SetTime((int) (event.getTimeMillis() / 1000));

        DateTime dateTime = new DateTime(event.getTimeMillis());
        item.PushBack("time", dateTime.toString(formatter));
        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        StackTraceElement source = event.getSource();
        if (source == null && (!event.isIncludeLocation())) {
            event.setIncludeLocation(true);
            source = event.getSource();
            event.setIncludeLocation(false);
        }

        item.PushBack("location", source == null ? "Unknown(Unknown Source)" : source.toString());

        String message = event.getMessage().getFormattedMessage();
        item.PushBack("message", message);

        String throwable = getThrowableStr(event.getThrown());
        if (throwable != null) {
            item.PushBack("throwable", throwable);
        }

        if (getLayout() != null) {
            item.PushBack("log", new String(getLayout().toByteArray(event)));
        }

        Optional.ofNullable(mdcFields).ifPresent(f->event.getContextMap().entrySet().stream()
                .filter(v-> Arrays.stream(f.split(",")).anyMatch(i->i.equals(v.getKey())))
                .forEach(map-> item.PushBack(map.getKey(),map.getValue()))
        );

        try {
            List<LogItem> logItems = new ArrayList<>();
            logItems.add(item);
            producer.putLogs(
                    topicId,
                    logItems,
                    new LoghubAppenderCallback(LOGGER, topicId, this.source, logItems)
            );
        } catch (Exception e) {
            this.error("Failed to send log, topicId=" + topicId
                    + ", source=" + source
                    + ", logItem=" + item, e);
        }
    }

    private String getThrowableStr(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
        }
        return sb.toString();
    }

    @PluginFactory
    public static LoghubAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginConfiguration final Configuration config,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("endpoint") final String endpoint,
            @PluginAttribute("accessKeyId") final String accessKeyId,
            @PluginAttribute("accessKeySecret") final String accessKeySecret,
            @PluginAttribute("totalSizeInBytes") final String  totalSizeInBytes,
            @PluginAttribute("maxBlockMs") final String  maxBlockMs,
            @PluginAttribute("sendThreadCount") final String  sendThreadCount,
            @PluginAttribute("batchSizeThresholdInBytes") final String  batchSizeThresholdInBytes,
            @PluginAttribute("batchCountThreshold") final String  batchCountThreshold,
            @PluginAttribute("lingerMs") final String  lingerMs,
            @PluginAttribute("retries") final String  retries,
            @PluginAttribute("baseRetryBackoffMs") final String  baseRetryBackoffMs,
            @PluginAttribute("maxRetryBackoffMs") final String maxRetryBackoffMs,
            @PluginAttribute("topicId") final String topicId,
            @PluginAttribute("source") final String source,
            @PluginAttribute("timeFormat") final String timeFormat,
            @PluginAttribute("timeZone") final String timeZone,
            @PluginAttribute("mdcFields") final String mdcFields) {

        Boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);

        int maxBlockMsInt = parseStrToInt(maxBlockMs, 0);

        int baseRetryBackoffMsInt = parseStrToInt(baseRetryBackoffMs, 100);

        int maxRetryBackoffMsInt = parseStrToInt(maxRetryBackoffMs, 100);

        int lingerMsInt = parseStrToInt(lingerMs, 3000);

        int batchCountThresholdInt = parseStrToInt(batchCountThreshold, 4096);

        int batchSizeThresholdInBytesInt = parseStrToInt(batchSizeThresholdInBytes, 5 * 1024 * 1024);

        int totalSizeInBytesInt = parseStrToInt(totalSizeInBytes, 104857600);

        int retriesInt = parseStrToInt(retries, 3);

        int sendThreadCountInt = parseStrToInt(sendThreadCount, 8);

        String pattern = isStrEmpty(timeFormat) ? DEFAULT_TIME_FORMAT : timeFormat;
        String timeZoneInfo = isStrEmpty(timeZone) ? DEFAULT_TIME_ZONE : timeZone;
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(timeZoneInfo));

        return new LoghubAppender(
                name,
                filter,
                layout,
                ignoreExceptions,
                topicId,
                endpoint,
                accessKeyId,
                accessKeySecret,
                totalSizeInBytesInt,
                maxBlockMsInt,
                sendThreadCountInt,
                batchSizeThresholdInBytesInt,
                batchCountThresholdInt,
                lingerMsInt,
                retriesInt,
                baseRetryBackoffMsInt,
                maxRetryBackoffMsInt,
                source,
                formatter,
                mdcFields
        );
    }

    static boolean isStrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static int parseStrToInt(String str, final int defaultVal) {
        if (!isStrEmpty(str)) {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        } else {
            return defaultVal;
        }
    }
}
