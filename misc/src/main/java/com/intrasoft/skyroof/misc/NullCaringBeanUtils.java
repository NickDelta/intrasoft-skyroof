package com.intrasoft.skyroof.misc;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class NullCaringBeanUtils extends BeanUtils {

    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static void copyNonNullProperties(Object src, Object target, String... ignoreProperties) {
        String[] nullProperties = getNullPropertyNames(src);
        String[] totalProperties = new String[nullProperties.length + ignoreProperties.length];
        System.arraycopy(nullProperties,0, totalProperties,0,nullProperties.length);
        System.arraycopy(ignoreProperties,0, totalProperties,nullProperties.length, nullProperties.length);

        BeanUtils.copyProperties(src, target, totalProperties);
    }

    private static String[] getNullPropertyNames (Object source) {
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] desc = wrapper.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(PropertyDescriptor pd : desc) {
            Object src = wrapper.getPropertyValue(pd.getName());
            if (src == null) emptyNames.add(pd.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
