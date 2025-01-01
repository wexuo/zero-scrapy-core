package com.wexuo.scrapy.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FieldRule {
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型: link、datetime
     */
    private String type;
    /**
     * 是否多值
     */
    private Boolean multiple = false;
    /**
     * 字段取值的 xpath 表达式列表
     */
    private final List<String> xpaths = new ArrayList<>();
    /**
     * 字段值提取正则表达式列表
     */
    private final List<String> regexs = new ArrayList<>();
    /**
     * 字段值替换规则
     * key 为正则表达式，value 为替换后的值
     */
    private final Map<String, String> replaces = new LinkedHashMap<>();

    public static FieldRule create() {
        return new FieldRule();
    }

    public FieldRule setName(final String name) {
        this.name = name;
        return this;
    }

    public FieldRule setType(final String type) {
        this.type = type;
        return this;
    }

    public FieldRule setMultiple(final Boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public FieldRule addXpath(final String xpath) {
        this.xpaths.add(xpath);
        return this;
    }

    public FieldRule addRegex(final String regex) {
        this.regexs.add(regex);
        return this;
    }

    public FieldRule addReplace(final String key, final String value) {
        this.replaces.put(key, value);
        return this;
    }
}
