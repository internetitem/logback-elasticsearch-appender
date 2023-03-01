package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.internetitem.logback.elasticsearch.config.EsProperty;

public class AccessPropertyAndEncoder extends AbstractPropertyAndEncoder<IAccessEvent> {

    public AccessPropertyAndEncoder(EsProperty property, Context context) {
        super(property, context);
    }

    @Override
    protected PatternLayoutBase<IAccessEvent> getLayout() {
        return new PatternLayout();
    }
}
