package com.wexuo.scrapy.core.value;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

@Getter
@Setter
public class ListFieldValue {

    private String linkId;

    private List<FieldValue> fieldValues = new ArrayList<>();

    public void add(final String name, final Object value) {
        final FieldValue fieldValue = new FieldValue();
        fieldValue.setName(name);
        fieldValue.setValue(value);
        fieldValues.add(fieldValue);
    }

    public FieldValue getFieldValue(final String name) {
        for (final FieldValue fieldValue : fieldValues) {
            if (fieldValue.getName().equals(name)) {
                return fieldValue;
            }
        }
        return null;
    }

    public void merge(final ListFieldValue listFieldValue) {
        final List<FieldValue> values = listFieldValue.getFieldValues();
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        for (final FieldValue value : values) {
            final Object object = value.getValue();
            if (Objects.isNull(object) || (object instanceof String && ((String) object).isEmpty())) {
                continue;
            }
            final String name = value.getName();
            final FieldValue fieldValue = getFieldValue(name);
            if (Objects.nonNull(fieldValue)) {
                fieldValue.setValue(object);
            } else {
                add(name, object);
            }
        }
    }

    public <T> T toObject(final Class<T> clazz) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final FieldValue fieldValue : fieldValues) {
            map.put(fieldValue.getName(), fieldValue.getValue());
        }
        return new ObjectMapper().convertValue(map, clazz);
    }
}
