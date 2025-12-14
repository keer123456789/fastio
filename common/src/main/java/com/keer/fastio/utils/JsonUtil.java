package com.keer.fastio.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keer.fastio.enums.ExceptionErrorMsg;
import com.keer.fastio.exception.ServiceException;

import java.text.SimpleDateFormat;

/**
 * @author 张经伦
 * @date 2025/12/13 19:46
 * @description:
 */
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置ObjectMapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ServiceException(ExceptionErrorMsg.JsonParse.getCode(), e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ServiceException(ExceptionErrorMsg.JsonParse.getCode(), e.getMessage(), e);
        }

    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new ServiceException(ExceptionErrorMsg.JsonParse.getCode(), e.getMessage(), e);
        }

    }
}
