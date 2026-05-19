package com.syos.shared;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final Map<String, Object> params;

    public Request(String action, Map<String, Object> params) {
        this.action = action;
        this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
    }

    public String getAction() {
        return action;
    }

    public Map<String, Object> getParams() {
        return new LinkedHashMap<>(params);
    }
}