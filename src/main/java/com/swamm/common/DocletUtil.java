package com.swamm.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.swamm.doc.FieldModel;
import com.swamm.doc.MethodModel;

/**
 * Created by chengpanwang on 2016/10/20.
 */
public class DocletUtil {

    public static List<MethodModel> getMethodModels(ClassDoc classDoc) {

        if (classDoc == null || classDoc.methods() == null || classDoc.methods().length == 0) {
            DocletLog.log("接口下方法定义!");
            return Collections.emptyList();
        }

        List<MethodModel> methodModels = new ArrayList<MethodModel>(classDoc.methods().length);
        for (MethodDoc methodDoc : classDoc.methods()) {
            DocletLog.log("解析方法：" + methodDoc);
            // 方法名称
            MethodModel methodModel = new MethodModel();
            methodModel.setDesc(methodDoc.commentText());
            methodModel.setName(methodDoc.name());

            Tag[] tags = methodDoc.tags(Tags.TITLE);
            if (tags != null && tags.length > 0) {
                methodModel.setTitle(tags[0].text());
            }
            if (StringUtils.isBlank(methodModel.getTitle())) {
                methodModel.setTitle(methodModel.getDesc());
            }
            if (StringUtils.isBlank(methodModel.getTitle())) {
                methodModel.setTitle(methodModel.getName());
            }

            methodModels.add(methodModel);

            // 参数解析
            methodModel.setParamModels(DocletUtil.getParam(methodDoc));

            // 返回参数解新
            methodModel.setReturnModel(getReturnModel(methodDoc));

            DocletLog.log("解析方法完成：" + JSON.toJSONString(methodModel));
        }
        return methodModels;
    }

    public static FieldModel getReturnModel(MethodDoc methodDoc) {
        DocletLog.log("开始解析返回参数：" + methodDoc);
        Type type = methodDoc.returnType();
        if (type.qualifiedTypeName().equals("void")) {
            return null;
        }

        //属性泛型
        Map<String, String> genericTypeMap = new HashMap<>();
        if (type.asParameterizedType() != null && type.asParameterizedType().typeArguments() != null
            && type.asParameterizedType().typeArguments().length > 0) {
            for (int i = 0; i < type.asParameterizedType().typeArguments().length; i++) {
                genericTypeMap.put(type.asClassDoc().typeParameters()[i].qualifiedTypeName(),
                                   type.asParameterizedType().typeArguments()[i].qualifiedTypeName());
            }
        }
        if (genericTypeMap.size() > 0) {
            DocletLog.log("使用泛型：" + genericTypeMap);
        }

        FieldModel returnModel = new FieldModel();

        // 返回参数描述
        Tag[] tags = methodDoc.tags(Tags.RETURN);
        for (Tag tag : tags) {
            if (!StringUtils.startsWith(tag.text(), Tags.PREFIX)) {
                returnModel.setDesc(tag.text());
                break;
            }
        }
        returnModel.setType(type.qualifiedTypeName());

        List<FieldModel> fieldModels = new ArrayList<>();
        for (FieldDoc fieldDoc : getAllField(type.asClassDoc())) {
            if (fieldDoc.isStatic()) {
                continue;
            }
            fieldModels.add(getFieldModel(type.qualifiedTypeName(), fieldDoc, genericTypeMap));
        }

        returnModel.setInnerField(fieldModels);

        return returnModel;
    }

    public static FieldModel getFieldModel(String parentTypeName, FieldDoc fieldDoc, Map<String, String> genericTypeMap) {
        FieldModel fieldModel = new FieldModel();

        fieldModel.setDesc(StringUtils.defaultIfBlank(fieldDoc.commentText(), fieldDoc.name()));
        fieldModel.setName(fieldDoc.name());
        fieldModel.setType(fieldDoc.type().qualifiedTypeName());

        DocletLog.log("获取属性子属性: " + fieldDoc.name() + "， 父类：" + parentTypeName + "");
        if (!StringUtils.equals(parentTypeName, getGenericTypeName(fieldDoc, genericTypeMap))) {
            fieldModel.setInnerField(getInnerField(fieldDoc, genericTypeMap));
        } else {
            DocletLog.log("获取属性子属性, 泛型是父类， 不再递归");
        }
        return fieldModel;
    }

    public static List<FieldModel> getInnerField(FieldDoc fieldDoc, Map<String, String> genericTypeMap) {
        Type type = fieldDoc.type();

        if (ClassUtil.isPrimitiveOrWrapper(type) || fieldDoc.isStatic()) {
            return Collections.emptyList();
        }

        ClassDoc classDoc = type.asClassDoc();
        List<FieldModel> fieldModels = new ArrayList<>();

        // 解析泛型
        FieldDoc[] fields = null;
        String genericTypeName = null;
        if (classDoc.subclassOf(classDoc.findClass("java.util.Collection"))) {
            if (type.asParameterizedType() != null && type.asParameterizedType().typeArguments() != null
                && type.asParameterizedType().typeArguments().length > 0) {
                DocletLog.log("获取内部属性: " + fieldDoc.name() + " 泛型解析");

                Type genericType = type.asParameterizedType().typeArguments()[0];

                genericTypeName = genericType.qualifiedTypeName();
                // 如果是基本类型
                if (ClassUtil.isPrimitiveWrapper(genericType)) {
                    DocletLog.log("获取内部属性，泛型是基础类型：" + genericTypeName);
                }
                // 如果是父类声明的类型
                else if (genericTypeMap.containsKey(genericTypeName)) {
                    genericTypeName = genericTypeMap.get(genericTypeName);
                    DocletLog.log("获取内部属性，泛型是继承类型：" + genericTypeName);

                    fields = classDoc.findClass(genericTypeName).fields();
                }
                // 是自已定义的类型
                else {
                    DocletLog.log("获取内部属性，泛型是自已定义的类型：" + genericTypeName );

                    fields = classDoc.findClass(genericTypeName).fields();
                }
            }
        }
        /**
         * 例如  private T result;
         */
        else if (genericTypeMap.containsKey(type.qualifiedTypeName())){
            genericTypeName = genericTypeMap.get(type.qualifiedTypeName());
            DocletLog.log("获取内部属性，泛型是继承类型：" + type.qualifiedTypeName() + " , " + genericTypeName);

            fields = classDoc.findClass(genericTypeName).fields();
        }
        else {
            fields = classDoc.fields();
        }

        if (fields != null && fields.length > 0) {
            for (FieldDoc innerFieldDoc : fields) {
                if (isFieldIgnore(innerFieldDoc)) {
                    continue;
                }
                fieldModels.add(getFieldModel(genericTypeName, innerFieldDoc, genericTypeMap));
            }
        }

        return fieldModels;
    }

    public static boolean isFieldIgnore(FieldDoc fieldDoc) {
        if (fieldDoc.isStatic()) {
            return true;
        }

        Tag[] tags = fieldDoc.tags(Tags.IGNORE);
        if (tags != null && tags.length > 0) {
            return true;
        }

        return false;
    }

    public static String getGenericTypeName(FieldDoc fieldDoc, Map<String, String> genericTypeMap) {
        Type type = fieldDoc.type();

        if (ClassUtil.isPrimitiveOrWrapper(type) || fieldDoc.isStatic()) {
            return null;
        }

        ClassDoc classDoc = type.asClassDoc();


        String genericTypeName = null;
        if (classDoc.subclassOf(classDoc.findClass("java.util.Collection"))) {
            if (type.asParameterizedType() != null && type.asParameterizedType().typeArguments() != null
                && type.asParameterizedType().typeArguments().length > 0) {
                DocletLog.log("解析属性泛型: " + fieldDoc.name() + "");

                Type genericType = type.asParameterizedType().typeArguments()[0];

                genericTypeName = genericType.qualifiedTypeName();
                // 如果是基本类型
                if (ClassUtil.isPrimitiveWrapper(genericType)) {
                    DocletLog.log("泛型是基础类型：" + genericTypeName);
                }
                // 如果是父类声明的类型
                else if (genericTypeMap.containsKey(genericTypeName)) {
                    genericTypeName = genericTypeMap.get(genericTypeName);
                    DocletLog.log("泛型是继承类型：" + genericTypeName);

                }
                // 是自已定义的类型
                else {
                    DocletLog.log("泛型是自已定义的类型：" + genericTypeName );
                }
            }
        }
        /**
         * 例如  private T result;
         */
        else if (genericTypeMap.containsKey(type.qualifiedTypeName())){
            genericTypeName = genericTypeMap.get(type.qualifiedTypeName());
            DocletLog.log("泛型是继承类型：" + type.qualifiedTypeName() + " , " + genericTypeName);

        }


        return genericTypeName;
    }

    /**
     * 根据方法获取所有paramModel
     * @param methodDoc
     * @return
     */
    public static List<FieldModel> getParam(MethodDoc methodDoc) {

        DocletLog.log("开始解析方法参数：" + methodDoc);

        Parameter[] parameters = methodDoc.parameters();

        // 参数
        if (parameters == null || parameters.length == 0) {
            return Collections.emptyList();
        }

        List<FieldModel> paramModels = new ArrayList<FieldModel>();
        for (Parameter param : parameters) {
            FieldModel paramModel = new FieldModel();
            paramModel.setName(param.name());
            paramModel.setDesc(getParamDesc(methodDoc, param));
            paramModel.setType(param.type().qualifiedTypeName());

            // 如果参数是类，取出内部参数
            paramModel.setInnerField(getInnerParam(methodDoc, param));

            paramModels.add(paramModel);
        }

        return paramModels;
    }

    public static List<FieldModel> getInnerParam(MethodDoc methodDoc, Parameter parameter) {
        if (ClassUtil.isPrimitiveWrapper(parameter.type())) {
            return Collections.emptyList();
        }

        List<FieldModel> innerParamModels = new ArrayList<>();

        for (FieldDoc fieldDoc : getFilterField(methodDoc, parameter)) {
            if (fieldDoc.isStatic()) {
                continue;
            }

            FieldModel paramModel = new FieldModel();

            paramModel.setType(fieldDoc.type().qualifiedTypeName());
            paramModel.setName(fieldDoc.name());
            paramModel.setDesc(fieldDoc.commentText());

            innerParamModels.add(paramModel);
        }
        DocletLog.log("参数：" + parameter + " 不是基本类型，解析内部参数：" + JSON.toJSONString(innerParamModels));

        return innerParamModels;

    }

    public static List<FieldDoc> getFilterField(MethodDoc methodDoc, Parameter parameter) {
        DocletLog.log("获取过滤需要的属性参数");
        List<FieldDoc> fieldDocs = getAllField(parameter.type().asClassDoc());

        if (fieldDocs == null || fieldDocs.size() == 0) {
            return Collections.emptyList();
        }

        Map<String, String> needTag = new HashMap<>();
        List<ParamTag> paramTags = getParamTag(methodDoc, parameter.name());
        for (ParamTag paramTag : paramTags) {
            String parameterComment = paramTag.parameterComment();
            if (parameterComment == null || parameterComment.isEmpty()) {
                continue;
            }
            parameterComment = parameterComment.trim();

            if (parameterComment.startsWith(Tags.PREFIX)) {
                String[] paramFields = StringUtils.split(parameterComment);
                String property = StringUtils.substring(paramFields[0], 1);
                String fieldComment = StringUtils.substring(parameterComment, property.length() + 1);
                needTag.put(property, fieldComment);
            }
        }
        boolean isSpecialField = needTag.size() > 0;
        if (isSpecialField) {
            DocletLog.log("用户指定接口参数：" + needTag);
        } else {
            DocletLog.log("用户未指定接口参数， 使用参数类的属性定义");
        }

        List<FieldDoc> resultFieldDocs = new ArrayList<>();
        for (FieldDoc fieldDoc : fieldDocs) {

            // 第一优先级，方法上设置 需要的参数
            if (isSpecialField) {
                if (needTag.containsKey(fieldDoc.name())) {
                    resultFieldDocs.add(fieldDoc);

                    // 如果在方法写了注释， 覆掉field上的注释
                    String fieldComment = needTag.get(fieldDoc.name());
                    if (StringUtils.isNotBlank(fieldComment)) {
                        fieldDoc.setRawCommentText(fieldComment);
                    }
                }

                continue;
            }

            // 第二优先级， 取得所有field, 忽略有ignore的字段
            else {
                Tag[] tags = fieldDoc.tags(Tags.IGNORE);
                if (tags != null && tags.length > 0) {
                    continue;
                }
            }

            resultFieldDocs.add(fieldDoc);
        }

        return resultFieldDocs;
    }

    public static List<FieldDoc> getAllField(ClassDoc classDoc) {

        if (classDoc == null || Object.class.getName().equals(classDoc.qualifiedName())) {
            return Collections.emptyList();
        }

        List<FieldDoc> fieldDocs = new ArrayList<>();

        fieldDocs.addAll(Arrays.asList(classDoc.fields()));

        fieldDocs.addAll(getAllField(classDoc.superclass()));

        return fieldDocs;
    }

    public static String getParamDesc(MethodDoc methodDoc, Parameter parameter) {
        String paramName = parameter.name();

        for (ParamTag paramTag : getParamTag(methodDoc, paramName)) {
            if (paramTag.parameterName().equals(paramName)) {
                String parameterComment = paramTag.parameterComment();
                if (parameterComment == null || parameterComment.isEmpty()) {
                    continue;
                }
                parameterComment = parameterComment.trim();

                if (!parameterComment.startsWith(Tags.PREFIX)) {
                    return parameterComment;
                }
            }
        }

        return paramName;
    }

    public static List<ParamTag> getParamTag(MethodDoc methodDoc, String paramName) {
        ParamTag[] tags = methodDoc.paramTags();
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }

        List<ParamTag> paramTags = new ArrayList<>();
        for (ParamTag tag : tags) {
            if (tag.parameterName().equals(paramName)) {
                paramTags.add(tag);
            }
        }

        return paramTags;
    }
}
