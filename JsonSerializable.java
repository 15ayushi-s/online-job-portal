package com.jobhub.util;

import java.util.Map;

/** Implemented by model classes so Json.write() knows how to turn them into a JSON object. */
public interface JsonSerializable {
    Map<String, Object> toJson();
}
