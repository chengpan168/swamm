package com.swamm.common;

import com.sun.javadoc.Type;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by chengpanwang on 2016/10/20.
 */
public class ClassUtil {

    private static Map<Object, Object> primitiveWrapperMap = new HashMap();
    private static Map<Object, Object> genericTypeMap = new HashMap();
    static {
        primitiveWrapperMap.put(Boolean.class.getName(), Boolean.class);
        primitiveWrapperMap.put(Byte.class.getName(), Byte.class);
        primitiveWrapperMap.put(Character.class.getName(), Character.class);
        primitiveWrapperMap.put(Short.class.getName(), Short.class);
        primitiveWrapperMap.put(Integer.class.getName(), Integer.class);
        primitiveWrapperMap.put(Long.class.getName(), Long.class);
        primitiveWrapperMap.put(Double.class.getName(), Double.class);
        primitiveWrapperMap.put(Float.class.getName(), Float.class);
        primitiveWrapperMap.put(Void.class.getName(), Void.TYPE);
        primitiveWrapperMap.put(String.class.getName(), String.class);

        primitiveWrapperMap.put(Date.class.getName(), Date.class);
        primitiveWrapperMap.put(Timestamp.class.getName(), Timestamp.class);


        genericTypeMap.put(Boolean.class.getSimpleName(), Boolean.class);
        genericTypeMap.put(Byte.class.getSimpleName(), Byte.class);
        genericTypeMap.put(Character.class.getSimpleName(), Character.class);
        genericTypeMap.put(Short.class.getSimpleName(), Short.class);
        genericTypeMap.put(Integer.class.getSimpleName(), Integer.class);
        genericTypeMap.put(Long.class.getSimpleName(), Long.class);
        genericTypeMap.put(Double.class.getSimpleName(), Double.class);
        genericTypeMap.put(Float.class.getSimpleName(), Float.class);
        genericTypeMap.put(Void.class.getSimpleName(), Void.TYPE);
        genericTypeMap.put(String.class.getSimpleName(), String.class);

        genericTypeMap.put(Date.class.getSimpleName(), Date.class);
        genericTypeMap.put(Timestamp.class.getSimpleName(), Timestamp.class);




    }

    public static boolean isPrimitiveWrapper(Type type) {
        if (type == null) {
            return false;
        }
        return primitiveWrapperMap.containsKey(type.qualifiedTypeName());
    }

    public static boolean isPrimitiveOrWrapper(Type type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive() || isPrimitiveWrapper(type);
    }

    /**
     * 泛型是否是基础类型
     * @param type
     * @return
     */
    public static boolean isBasicGenericType(String type) {
        return genericTypeMap.containsKey(type);
    }
}
