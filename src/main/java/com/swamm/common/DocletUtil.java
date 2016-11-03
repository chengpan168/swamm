package com.swamm.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import com.sun.tools.javadoc.FieldDocImpl;
import com.swamm.doc.DocletTree;
import org.apache.commons.collections4.CollectionUtils;
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

    public static DocletTree docletTree = new DocletTree();

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
        Map<String, Type> genericTypeMap = ClassUtil.getGenericTypeMap(type);

        FieldModel returnModel = new FieldModel();

        // 返回参数描述
        Tag[] tags = methodDoc.tags(Tags.RETURN);
        for (Tag tag : tags) {
            if (!StringUtils.startsWith(tag.text(), Tags.PREFIX)) {
                returnModel.setDesc(tag.text());
                break;
            }
        }
        returnModel.setType(type);

        DocletTree.Node node = docletTree.add(methodDoc.flatSignature());

        List<FieldModel> fieldModels = new ArrayList<>();
        for (FieldDoc fieldDoc : getAllField(type)) {
            if (fieldDoc.isStatic()) {
                continue;
            }
            fieldModels.add(getFieldModel(type, fieldDoc, genericTypeMap, node));
        }

        returnModel.setInnerField(fieldModels);

        return returnModel;
    }

    public static FieldModel getFieldModel(Type parentType, FieldDoc fieldDoc, Map<String, Type> genericTypeMap, DocletTree.Node node) {

        String parentTypeName = parentType.qualifiedTypeName();

        FieldModel fieldModel = new FieldModel();

        fieldModel.setDesc(StringUtils.defaultIfBlank(fieldDoc.commentText(), fieldDoc.name()));
        fieldModel.setName(fieldDoc.name());

        // 设置属性类型，或者是泛型
        if (genericTypeMap.containsKey(fieldDoc.type().qualifiedTypeName())) {
            Type type = genericTypeMap.get(fieldDoc.type().qualifiedTypeName());
            DocletLog.log("类型：" + parentTypeName + "，子属性: " + fieldDoc.name() + "使用了泛型：" + type);
            fieldModel.setType(type);
        } else {
            fieldModel.setType(fieldDoc.type());
        }

        DocletLog.log("获取属性子属性: " + fieldDoc.name() + "， 父类：" + parentTypeName + "");

        return getFieldModel(parentType, fieldModel, genericTypeMap, node);
    }


    public static FieldModel getFieldModel(Type parentType, FieldModel fieldModel, Map<String, Type> genericTypeMap, DocletTree.Node node) {
        String parentTypeName = parentType.qualifiedTypeName();
        if (node.deep() > DocletContext.FIELD_DEEP) {
            DocletLog.log("类型：" + parentTypeName + "，子属性: " + fieldModel.getName() + " 递归深度超过：" + DocletContext.FIELD_DEEP);
        } else {
            if (!isGetInnerField(fieldModel.getType())) {
                fieldModel.setInnerField(getInnerField(fieldModel, genericTypeMap, node));
            }
        }
        return fieldModel;
    }

    public static List<FieldModel> getInnerField(FieldModel fieldModel, Map<String, Type> genericTypeMap, DocletTree.Node node) {
        Type type = fieldModel.getType();
        String name = fieldModel.getName();

        if (ClassUtil.isPrimitiveOrWrapper(type)) {
            return Collections.emptyList();
        }

        // 检查属性类型是不是泛型
        if (genericTypeMap.containsKey(type.qualifiedTypeName())) {
            type = genericTypeMap.get(type.qualifiedTypeName());
            DocletLog.log("获取属性：" + name + ", 子属性, 使用了泛型：" + type);
        } else {
            //ignore
        }

        List<FieldModel> fieldModels = new ArrayList<>();

        List<FieldDoc> fields = null;
        Type parentType = type;

        // 如果是集合类型
        if (ClassUtil.isCollection(type)) {

            List<Type> genericTypeList = ClassUtil.getGenericType(type);
            if (!genericTypeList.isEmpty()) {
                Type genericType = genericTypeList.get(0);

                // 如果是基本类型
                if (ClassUtil.isPrimitiveWrapper(genericType)) {
                    DocletLog.log("集合属性：" + name + "，泛型是基本类型：" + genericType);
                }

                // 集合使用了泛型 private List<T> result;
                else if (genericTypeMap.containsKey(genericType.qualifiedTypeName())) {
                    parentType = genericTypeMap.get(genericType.qualifiedTypeName());

                    if (ClassUtil.isCollection(parentType)) {
                        DocletLog.log("集合属性：" + name + " ，是泛型集合类型：" + parentType);
                        return Arrays.asList(getFieldModel(parentType, new FieldModel(parentType), genericTypeMap, node.addChildren(name)));
                    }

                    DocletLog.log("集合属性：" + name + " ，是泛型复杂类型：" + parentType);
                    fields = getAllField(parentType);


                }
                // 复杂类型
                else {

                    if (ClassUtil.isCollection(genericType.asClassDoc())) {
                        DocletLog.log("集合属性：" + name + " ，是集合类型：" + genericType);
                        return Arrays.asList(getFieldModel(parentType, new FieldModel(genericType), genericTypeMap, node.addChildren(name)));
                    }

                    DocletLog.log("集合属性：" + name + " ，是复杂类型：" + genericType);
                    fields = getAllField(genericType);
                    parentType = genericType;
                }

            } else {
                DocletLog.log("集合属性：" + name + " 泛型没有指定：" + type);
            }

        }

        else {
            DocletLog.log("获取属性：" + name + ", 子属性， 属性类型：" + type);
            fields = getAllField(type);
        }

        if (fields != null && fields.size() > 0) {
            for (FieldDoc innerFieldDoc : fields) {
                if (isFieldIgnore(innerFieldDoc)) {
                    continue;
                }
                fieldModels.add(getFieldModel(parentType, innerFieldDoc, genericTypeMap, node.addChildren(innerFieldDoc.name())));
            }
        }

        return fieldModels;
    }

    public static boolean isGetInnerField(Type type) {

        if (ClassUtil.isPrimitiveOrWrapper(type)) {
            return true;
        }

        return false;
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
            paramModel.setType(param.type());

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
            if (isFieldIgnore(fieldDoc)) {
                continue;
            }

            FieldModel paramModel = new FieldModel();

            paramModel.setType(fieldDoc.type());
            paramModel.setName(fieldDoc.name());
            paramModel.setDesc(StringUtils.defaultIfBlank(fieldDoc.commentText(), fieldDoc.name()));

            innerParamModels.add(paramModel);
        }

        DocletLog.log("参数：" + parameter + " 不是基本类型，解析内部参数：" + JSON.toJSONString(innerParamModels));

        return innerParamModels;

    }

    public static List<FieldDoc> getFilterField(MethodDoc methodDoc, Parameter parameter) {
        DocletLog.log("获取过滤需要的属性参数");
        List<FieldDoc> fieldDocs = getAllField(parameter.type());

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
                if (StringUtils.isBlank(property)) {
                    continue;
                }

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

    public static List<FieldDoc> getAllField(Type type) {
        DocletLog.log("获取所有属性：" + type);
        if (type == null) {
            return Collections.emptyList();
        }

        ClassDoc classDoc = type.asClassDoc();

        if (classDoc == null || Object.class.getName().equals(classDoc.qualifiedName())) {
            return Collections.emptyList();
        }

        // 如果参数是集合，获取泛型字段
        if (ClassUtil.isCollection(type)) {
            DocletLog.log("获取所有属性: " + type +  ",是集合类型");
            List<Type> genericTypes = ClassUtil.getGenericType(type);
            DocletLog.log("获取所有属性: " + type + ", 泛型：" + genericTypes);
            if (genericTypes.size() > 0) {
                classDoc = genericTypes.get(0).asClassDoc();
            }
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

    public static List<FieldModel> getCustomTagParam(List<ParamTag> paramTags) {
        List<FieldModel> fieldModels = new ArrayList<>();
        if (paramTags == null || paramTags.size() == 0) {
            return fieldModels;
        }

        for (ParamTag paramTag : paramTags) {
            String comment = paramTag.parameterComment();
            if (comment == null || comment.isEmpty()) {
                continue;
            }

            if (!comment.startsWith(Tags.PREFIX)) {
                continue;
            }

            String[] split = StringUtils.split(comment.trim());
            if (split == null || split.length < 1) {
                continue;
            }

            FieldModel fieldModel = new FieldModel();
            fieldModel.setName(split[0]);
            if (split.length > 1) {
                fieldModel.setDesc(split[1]);
            }

            fieldModels.add(fieldModel);
        }


        return fieldModels;
    }


    /**
     * 根据参数名取得所有tag定义
     * @param methodDoc
     * @param paramName
     * @return
     */
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
