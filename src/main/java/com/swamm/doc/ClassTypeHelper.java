package com.swamm.doc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;
import com.swamm.common.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chengpanwang on 2016/10/20.
 */
public class ClassTypeHelper {

    private static Map<Object, Object> primitiveWrapperMap = new HashMap();
    private static Map<Object, Object> simpleTypeMap       = new HashMap();
    private static Map<Object, Object> numberTypeMap       = new HashMap();
    private static Map<Object, Object> stringTypeMap       = new HashMap();
    private static Map<Object, Object> booleanTypeMap      = new HashMap();
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
        primitiveWrapperMap.put(BigDecimal.class.getName(), BigDecimal.class);

        primitiveWrapperMap.put(Date.class.getName(), Date.class);
        primitiveWrapperMap.put(Timestamp.class.getName(), Timestamp.class);

        // 数字
        numberTypeMap.put(Short.class.getName(), Short.class);
        numberTypeMap.put(Integer.class.getName(), Integer.class);
        numberTypeMap.put(Long.class.getName(), Long.class);
        numberTypeMap.put(Double.class.getName(), Double.class);
        numberTypeMap.put(Float.class.getName(), Float.class);
        numberTypeMap.put("short", Short.class);
        numberTypeMap.put("int", Integer.class);
        numberTypeMap.put("long", Long.class);
        numberTypeMap.put("double", Double.class);
        numberTypeMap.put("float", Float.class);

        // 字符串
        stringTypeMap.put("string", String.class);
        stringTypeMap.put(String.class.getName(), String.class);

        booleanTypeMap.put(Boolean.class.getName(), String.class);
        booleanTypeMap.put("boolean", String.class);

        simpleTypeMap.put(Boolean.class.getSimpleName(), Boolean.class);
        simpleTypeMap.put(Byte.class.getSimpleName(), Byte.class);
        simpleTypeMap.put(Character.class.getSimpleName(), Character.class);
        simpleTypeMap.put(Short.class.getSimpleName(), Short.class);
        simpleTypeMap.put(Integer.class.getSimpleName(), Integer.class);
        simpleTypeMap.put(Long.class.getSimpleName(), Long.class);
        simpleTypeMap.put(Double.class.getSimpleName(), Double.class);
        simpleTypeMap.put(Float.class.getSimpleName(), Float.class);
        simpleTypeMap.put(Void.class.getSimpleName(), Void.TYPE);
        simpleTypeMap.put(String.class.getSimpleName(), String.class);
        simpleTypeMap.put(BigDecimal.class.getSimpleName(), BigDecimal.class);

        simpleTypeMap.put(Date.class.getSimpleName(), Date.class);
        simpleTypeMap.put(Timestamp.class.getSimpleName(), Timestamp.class);


    }

    public static Map<String, Type> getGenericTypeMap(Type type) {
        if (type == null || type.qualifiedTypeName().equals("void")) {
            return Collections.emptyMap();
        }

        Logger.info("获取类型泛型信息：" + type);
        ClassDoc classDoc = type.asClassDoc();
        ParameterizedType parameterizedType = type.asParameterizedType();
        if (parameterizedType == null) {
            return Collections.emptyMap();
        }
        if (parameterizedType.typeArguments() == null
            || parameterizedType.typeArguments().length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Type> genericTypeMap = new HashMap<>();


        for (int i = 0; i < parameterizedType.typeArguments().length; i++) {
            // 泛型声明类型
            String declareType = classDoc.typeParameters()[i].qualifiedTypeName();
            // 泛型实际类型
            Type argType = parameterizedType.typeArguments()[i];

            genericTypeMap.put(declareType, argType);
        }


        if (genericTypeMap.size() > 0) {
            Logger.info("使用泛型：" + genericTypeMap);
        }

        return genericTypeMap;

    }

    public static List<Type> getGenericType(Type type) {
        if (type == null || type.qualifiedTypeName().equals("void")) {
            return Collections.emptyList();
        }


        ParameterizedType parameterizedType = type.asParameterizedType();
        if (parameterizedType == null) {
            return Collections.emptyList();
        }
        if (parameterizedType.typeArguments() == null
            || parameterizedType.typeArguments().length == 0) {
            return Collections.emptyList();
        }

        List<Type> genericTypeList = Arrays.asList(parameterizedType.typeArguments());
        Logger.info("获取类型泛型信息：" + type + ", 泛型：" + genericTypeList);
        return genericTypeList;

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
        return simpleTypeMap.containsKey(type);
    }

    public static boolean isNumber(String type) {
        return numberTypeMap.containsKey(type);
    }
    public static boolean isString(String type) {
        return stringTypeMap.containsKey(type);
    }
    public static boolean isBoolean(String type) {
        return booleanTypeMap.containsKey(type);
    }

    public static boolean isCollection(Type type) {
        if (type == null || type.asClassDoc() == null) {

            return false;
        }

        return type.asClassDoc().subclassOf(DocletContext.COLLECTION_CLASS_DOC);
    }
}
