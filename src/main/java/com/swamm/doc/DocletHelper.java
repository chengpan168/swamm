package com.swamm.doc;

import java.util.*;

import com.swamm.common.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.*;
import com.swamm.model.FieldModel;
import com.swamm.model.MethodModel;

/**
 * Created by chengpanwang on 2016/10/20.
 */
public class DocletHelper {

    /**
     * 有RestController， Controller 注解才是spring mvc的controller
     * @param classDoc
     * @return
     */
    public static boolean isController (ClassDoc classDoc) {
        for( AnnotationDesc annotationDesc : classDoc.annotations()) {
            List<String> controllers = Arrays.asList("org.springframework.web.bind.annotation.RestController", "org.springframework.web.bind.annotation.Controller");
            String annotationType = annotationDesc.annotationType().qualifiedTypeName();
            if (controllers.contains(annotationType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInclude(ClassDoc classDoc) {
        if (CollectionUtils.isEmpty(DocletContext.INCLUDE_CLASS)) {
            return true;
        }
        return DocletContext.INCLUDE_CLASS.contains(classDoc.simpleTypeName());
    }

    public static List<MethodModel> getMethodModels(ClassDoc classDoc) {

        if (classDoc == null || classDoc.methods() == null || classDoc.methods().length == 0) {
            Logger.info("接口下方法定义!");
            return Collections.emptyList();
        }

        List<MethodModel> methodModels = new ArrayList<MethodModel>(classDoc.methods().length);
        for (MethodDoc methodDoc : classDoc.methods()) {
            Logger.info("解析方法：" + methodDoc);

            if (!isRequestMapping(methodDoc)) {
                continue;
            }

            // 方法名称
            MethodModel methodModel = new MethodModel();
            methodModel.setDesc(methodDoc.commentText());
            methodModel.setName(methodDoc.name());
            methodModel.setUrl(getUrl(methodDoc));

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
            methodModel.setParamModels(DocletHelper.getParam(methodDoc));

            // 返回参数解新
            methodModel.setReturnModel(getReturnModel(methodDoc));

            Logger.info("解析方法完成：" + JSON.toJSONString(methodModel));
        }
        return methodModels;
    }

    private static String getUrl(MethodDoc methodDoc) {
        if (Tags.PROTOCOL_DUBBO.equals(DocletContext.PROTOCOL)) {
            return "/" + methodDoc.qualifiedName();
        } else {
            for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
                if (annotationDesc.annotationType().qualifiedTypeName().equals("org.springframework.web.bind.annotation.RequestMapping")) {
                    for (AnnotationDesc.ElementValuePair elementValuePair : annotationDesc.elementValues()) {
                        if (elementValuePair.element().qualifiedName().equals("org.springframework.web.bind.annotation.RequestMapping.value")
                         || elementValuePair.element().qualifiedName().equals("org.springframework.web.bind.annotation.RequestMapping.path")) {
                            Logger.info("request mapping:" + elementValuePair.value());
                            return elementValuePair.value().toString();
                        }
                    }
                }
            }
        }
        return "";
    }

    public static boolean isRequestMapping(MethodDoc methodDoc) {
        if (Tags.PROTOCOL_DUBBO.equals(DocletContext.PROTOCOL)) {
            return true;
        } else {
            for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
                if (annotationDesc.annotationType().qualifiedTypeName().equals("org.springframework.web.bind.annotation.RequestMapping")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static FieldModel getReturnModel(MethodDoc methodDoc) {
        Logger.info("开始解析返回参数：" + methodDoc);
        Type type = methodDoc.returnType();
        if (type.qualifiedTypeName().equals("void")) {
            return null;
        }

        //属性泛型
        Map<String, Type> genericTypeMap = ClassTypeHelper.getGenericTypeMap(type);

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

        TreeNode node = new TreeNode(methodDoc.returnType().typeName());

        List<FieldModel> fieldModels = new ArrayList<>();
        for (FieldDoc fieldDoc : getAllField(type)) {
            if (fieldDoc.isStatic()) {
                continue;
            }
            fieldModels.add(getFieldModel(type, fieldDoc, genericTypeMap, node));
        }

        returnModel.setInnerFields(fieldModels);

        return returnModel;
    }

    public static FieldModel getFieldModel(Type parentType, FieldDoc fieldDoc, Map<String, Type> genericTypeMap, TreeNode node) {

        String parentTypeName = parentType.qualifiedTypeName();

        FieldModel fieldModel = new FieldModel();

        fieldModel.setDesc(StringUtils.defaultIfBlank(fieldDoc.commentText(), fieldDoc.name()));
        fieldModel.setName(fieldDoc.name());

        // 设置属性类型，或者是泛型
        if (genericTypeMap.containsKey(fieldDoc.type().qualifiedTypeName())) {
            Type type = genericTypeMap.get(fieldDoc.type().qualifiedTypeName());
            Logger.info("类型：" + parentTypeName + "，子属性: " + fieldDoc.name() + "使用了泛型：" + type);
            fieldModel.setType(type);
        } else {
            fieldModel.setType(fieldDoc.type());
        }

        Logger.info("获取属性子属性: " + fieldDoc.name() + "， 父类：" + parentTypeName + "");

        return getFieldModel(parentType, fieldModel, genericTypeMap, node);
    }


    public static FieldModel getFieldModel(Type parentType, FieldModel fieldModel, Map<String, Type> genericTypeMap, TreeNode node) {
        Logger.info("当前路径：" + node.path());

        String parentTypeName = parentType.qualifiedTypeName();
        if (node.deep() > DocletContext.FIELD_DEEP) {
            Logger.info("类型：" + parentTypeName + "，子属性: " + fieldModel.getName() + " 递归深度超过：" + DocletContext.FIELD_DEEP);
        } else {
            if (!isGetInnerField(fieldModel.getType())) {
                fieldModel.setInnerFields(getInnerField(fieldModel, genericTypeMap, node));
            }
        }
        return fieldModel;
    }

    public static List<FieldModel> getInnerField(FieldModel fieldModel, Map<String, Type> genericTypeMap, TreeNode node) {
        Type type = fieldModel.getType();
        String name = fieldModel.getName();

        if (ClassTypeHelper.isPrimitiveOrWrapper(type)) {
            return Collections.emptyList();
        }

        // 检查属性类型是不是泛型
        if (genericTypeMap.containsKey(type.qualifiedTypeName())) {
            type = genericTypeMap.get(type.qualifiedTypeName());
            Logger.info("获取属性：" + name + ", 子属性, 使用了泛型：" + type);
        } else {
            //ignore
        }

        List<FieldModel> fieldModels = new ArrayList<>();

        List<FieldDoc> fields = null;
        Type parentType = type;

        // 如果是集合类型
        if (ClassTypeHelper.isCollection(type)) {

            List<Type> genericTypeList = ClassTypeHelper.getGenericType(type);
            if (!genericTypeList.isEmpty()) {
                Type genericType = genericTypeList.get(0);

                // 如果是基本类型
                if (ClassTypeHelper.isPrimitiveWrapper(genericType)) {
                    Logger.info("集合属性：" + name + "，泛型是基本类型：" + genericType);
                }

                // 集合使用了泛型 private List<T> result;
                else if (genericTypeMap.containsKey(genericType.qualifiedTypeName())) {
                    parentType = genericTypeMap.get(genericType.qualifiedTypeName());

                    if (ClassTypeHelper.isCollection(parentType)) {
                        Logger.info("集合属性：" + name + " ，是泛型集合类型：" + parentType);
                        return Arrays.asList(getFieldModel(parentType, new FieldModel(parentType), genericTypeMap, node.addChildren(name)));
                    }

                    Logger.info("集合属性：" + name + " ，是泛型复杂类型：" + parentType);
                    fields = getAllField(parentType);


                }
                // 复杂类型
                else {

                    if (ClassTypeHelper.isCollection(genericType.asClassDoc())) {
                        Logger.info("集合属性：" + name + " ，是集合类型：" + genericType);
                        return Arrays.asList(getFieldModel(parentType, new FieldModel(genericType), genericTypeMap, node.addChildren(name)));
                    }

                    Logger.info("集合属性：" + name + " ，是复杂类型：" + genericType);
                    fields = getAllField(genericType);
                    parentType = genericType;
                }

            } else {
                Logger.info("集合属性：" + name + " 泛型没有指定：" + type);
            }

        }

        else {
            Logger.info("获取属性：" + name + ", 子属性， 属性类型：" + type);
            fields = getAllField(type);
        }

        if (fields != null && fields.size() > 0) {
            for (FieldDoc innerFieldDoc : fields) {
//                Logger.debug("----------属性名称：" + innerFieldDoc.name());
//                Logger.debug("node的属性：" + JSON.toJSONString(node.getName()));

                if (isFieldIgnore(innerFieldDoc)) {
                    continue;
                }
                List<FieldModel> customFieldModels = null;
                if(node.getCurrentFieldModel() != null) {
                    customFieldModels = node.getCurrentFieldModel().getInnerFields();
                }



                if (customFieldModels == null || customFieldModels.size() == 0) {
                    fieldModels.add(getFieldModel(parentType, innerFieldDoc, genericTypeMap, node.addChildren(innerFieldDoc.name())));
                } else {
                    for (FieldModel customFieldMode : customFieldModels) {
                        if (innerFieldDoc.name().equals(customFieldMode.getName())) {
                            Logger.debug("从node中取得指定的属性,用于过滤不需要的属性：" + innerFieldDoc.name() + " , " + JSON.toJSONString(customFieldModels));
                            fieldModels.add(getFieldModel(parentType, innerFieldDoc, genericTypeMap, node.addChildren(innerFieldDoc.name())));
                        }
                    }
                }
            }
        }

        return fieldModels;
    }

    public static boolean isGetInnerField(Type type) {

        if (ClassTypeHelper.isPrimitiveOrWrapper(type)) {
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

        Logger.info("开始解析方法参数：" + methodDoc);



        Parameter[] parameters = methodDoc.parameters();

        // 参数
        if (parameters == null || parameters.length == 0) {
            return Collections.emptyList();
        }

        List<FieldModel> customFields = getCustomField(methodDoc);

        List<FieldModel> paramModels = new ArrayList<FieldModel>();
        for (Parameter param : parameters) {
            Type type = param.type();

            FieldModel paramModel = new FieldModel();
            paramModel.setName(param.name());
            paramModel.setDesc(getParamDesc(methodDoc, param));
            paramModel.setType(type);

            TreeNode node = new TreeNode(param.name());

            for (FieldModel customField : customFields) {
                if (customField.getName().equals(param.name())) {
                    node.setFieldModel(customField);
                }
            }

            paramModel = getFieldModel(param.type(), paramModel, Collections.emptyMap(), node);


            /*List<FieldModel> fieldModels = new ArrayList<>();
            for (FieldDoc fieldDoc : getAllField(type)) {
                if (fieldDoc.isStatic()) {
                    continue;
                }
                fieldModels.add(getFieldModel(param.type(), fieldDoc, Collections.emptyMap(), node));
            }
            paramModel.setInnerFields(fieldModels);
*/
            paramModels.add(paramModel);
        }

        return paramModels;

    }

    /**
     * 解析指定参数   例如
     * @param methodDoc @name @str
     * @param methodDoc
     * @return
     */
    public static List<FieldModel> getCustomField(MethodDoc methodDoc) {
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();

        ParamTag[] paramTags = methodDoc.paramTags();
        if (paramTags == null || paramTags.length == 0) {
            return fieldModels;
        }



        Map<String, FieldModel> fieldMap = new HashMap<>();

        for (ParamTag paramTag : paramTags) {
            String argName = paramTag.parameterName();

            FieldModel fieldModel = fieldMap.get(argName);
            if (fieldModel == null) {
                fieldModel = new FieldModel();
                fieldModel.setName(argName);
                fieldMap.put(argName, fieldModel);
            }
        }


        // 把在方法指定的参数 整理成fieldModel 的父子关系
        for (ParamTag paramTag : paramTags) {
            Logger.debug("用户指定方法参数属性：" + paramTag.parameterName() + " ," + paramTag.parameterComment());
            String argName = paramTag.parameterName();
            String comment = " " + paramTag.parameterComment();
            String[] childrenFieldNames = comment.split(" ");
            if (childrenFieldNames == null || childrenFieldNames.length == 0) {
                continue;
            }

            FieldModel fieldModel = fieldMap.get(argName);

            for (String childFieldName : childrenFieldNames) {
                if (StringUtils.isBlank(childFieldName) || !childFieldName.startsWith(Tags.PREFIX)) {
                    continue;
                }

                childFieldName = childFieldName.substring(Tags.PREFIX.length());

                FieldModel innerField = fieldModel.getInnerField(childFieldName, true);
                fieldModel.addInnerField(innerField);

                fieldModel = innerField;
            }
        }

        fieldModels.addAll(fieldMap.values());

        if (fieldModels.size() > 0) {
            Logger.info("用户指定方法参数属性集合：" + JSON.toJSON(fieldModels));

        }

        return fieldModels;

    }



    public static List<FieldModel> getInnerParam(MethodDoc methodDoc, Parameter parameter) {
        if (ClassTypeHelper.isPrimitiveWrapper(parameter.type())) {
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

        Logger.info("参数：" + parameter + " 不是基本类型，解析内部参数：" + JSON.toJSONString(innerParamModels));

        return innerParamModels;

    }

    public static List<FieldDoc> getFilterField(MethodDoc methodDoc, Parameter parameter) {
        Logger.info("获取过滤需要的属性参数");
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
            Logger.info("用户指定接口参数：" + needTag);
        } else {
            Logger.info("用户未指定接口参数， 使用参数类的属性定义");
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
        Logger.info("获取所有属性：" + type);
        if (type == null) {
            return Collections.emptyList();
        }
        if (ClassTypeHelper.isPrimitiveOrWrapper(type)) {
            return Collections.emptyList();
        }

        ClassDoc classDoc = type.asClassDoc();

        if (classDoc == null || Object.class.getName().equals(classDoc.qualifiedName())) {
            return Collections.emptyList();
        }

        // 如果参数是集合，获取泛型字段
        if (ClassTypeHelper.isCollection(type)) {
            Logger.info("获取所有属性: " + type + ",是集合类型");
            List<Type> genericTypes = ClassTypeHelper.getGenericType(type);
            Logger.info("获取所有属性: " + type + ", 泛型：" + genericTypes);
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
