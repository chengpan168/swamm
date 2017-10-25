package com.swamm.xiaoyaoji;

import java.util.*;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import com.swamm.common.ClassUtil;
import com.swamm.common.DocletContext;
import com.swamm.common.HttpClientUtils;
import com.swamm.doc.ClassModel;
import com.swamm.doc.FieldModel;
import com.swamm.doc.MethodModel;
import com.swamm.handler.Handler;

/**
 * Created by panwang.chengpw on 2017/9/14.
 */
public class XiaoYaoJiHandler implements Handler {

    String                      host      = "http://doc.3dker.cn";
    String                      token;
    String                      projectId = "2AS9dEdbsM";
    String                      moduleId  = "";
    String                      folderId  = "";
    private static final Logger logger    = LoggerFactory.getLogger(XiaoYaoJiHandler.class);

    public <T> T getData(String json) {
        JSONObject res = JSON.parseObject(json);
        Object data = res.get("data");
        return (T) data;
    }

    public JSONObject getFolders(JSONObject module) {
        return null;
    }

    public void newInterface(Map<String, String> methodParam) {
        Map<String, String> param = Maps.newHashMap();
        param.put("token", token);
        param.put("projectId", projectId);
        param.put("moduleId", moduleId);
        param.put("folderId", folderId);

        param.put("protocol", "HTTP");
        param.put("requestMethod", "POST");
        param.put("dataType", "JSON");
        param.put("contentType", "JSON");
        param.put("requestHeaders", "[]");
        param.put("status", "ENABLE");

        param.put("name", "name");
        param.put("sort", "0");
        param.put("description", "");
        param.put("url", "");

        param.put("requestArgs", "");
        param.put("responseArgs", "");

        param.putAll(methodParam);

        String json = HttpClientUtils.post(host + "/api/interface/save.json", param);
    }

    public void newFolder() {
        Map<String, String> param = Maps.newHashMap();
        param.put("token", token);
        param.put("projectId", projectId);
        param.put("moduleId", moduleId);
        param.put("name", "接口列表");
        String json = HttpClientUtils.post(host + "/api/interfacefolder.json", param);
        folderId = getData(json);
    }

    public void newModules() {
        Map<String, String> param = Maps.newHashMap();
        param.put("token", token);
        param.put("projectId", projectId);
        param.put("name", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd"));
        String json = HttpClientUtils.post(host + "/api/module.json", param);
        moduleId = getData(json);
    }

    public JSONObject getModules() {
        Map<String, String> param = Maps.newHashMap();
        param.put("token", token);
        String json = HttpClientUtils.get(host + "/api/project/" + projectId + ".json", param);
        JSONObject data = getData(json);
        return data;
    }

    public void getProject() {
        Map<String, String> param = Maps.newHashMap();
        param.put("token", token);
        String json = HttpClientUtils.get(host + "/api/project/list.json", param);
        JSONObject data = getData(json);
    }

    public String login() {
        String email = "chengpanwang@shining3d.com";
        String password = "123456";

        Map<String, String> param = Maps.newHashMap();
        param.put("email", email);
        param.put("password", password);
        String res = HttpClientUtils.post(host + "/api/login.json", param);

        JSONObject data = getData(res);
        token = data.get("token").toString();
        logger.info("获取token: {}", token);
        return token;
    }

    public static void main(String[] args) {
        XiaoYaoJiHandler xiaoYaoJiHandler = new XiaoYaoJiHandler();
        xiaoYaoJiHandler.login();
        xiaoYaoJiHandler.getProject();
        xiaoYaoJiHandler.newModules();
        xiaoYaoJiHandler.newFolder();
    }

    @Override
    public void execute(RootDoc rootDoc, List<ClassModel> classModels) {
        login();
        getProject();
        newModules();
        newFolder();
        for (ClassModel classModel : classModels) {
            for (MethodModel methodModel : classModel.getMethodModels()) {
                Map<String, String> param = Maps.newHashMap();
                param.put("name", methodModel.getTitle());
                param.put("description", methodModel.getDesc());
                param.put("url", classModel.getUrl() + methodModel.getUrl());

                param.put("requestArgs", JSON.toJSONString(convertParameter(methodModel.getParamModels())));
                param.put("responseArgs", JSON.toJSONString(convertParameter(methodModel.getReturnModel())));
                newInterface(param);
            }
        }
    }

    private List<XyjParameter> convertParameter(FieldModel fieldModel) {
        if (fieldModel == null) {
            return Collections.emptyList();
        }
        return convertParameter(Arrays.asList(fieldModel));
    }

    private List<XyjParameter> convertParameter(List<FieldModel> fieldModels) {
        if (fieldModels == null || fieldModels.size() == 0) {
            return Collections.emptyList();
        }

        List<XyjParameter> requestParameterList = new ArrayList<>();
        int i = 0;
        for (FieldModel fieldModel : fieldModels) {
            XyjParameter parameter = new XyjParameter();
            parameter.setDescription(fieldModel.getDesc());
            parameter.setName(i++ + ":" + fieldModel.getDesc());

            parameter.setType(getDataType(fieldModel));

            parameter.setChildren(convertParameter(fieldModel.getInnerFields()));

            requestParameterList.add(parameter);

        }

        return requestParameterList;

    }

    private String getDataType(FieldModel fieldModel) {
        Type fieldType = fieldModel.getType();

        ClassDoc classDoc = fieldType.asClassDoc();
        if (classDoc != null) {
            if (classDoc.subclassOf(DocletContext.getRootDoc().classNamed("java.util.Collection"))) {
                List<Type> genericType = ClassUtil.getGenericType(fieldType);
                if (genericType.isEmpty()) {
                    return "array";
                }

                return "array<" + getDataType(genericType.get(0)) + ">";
            }
        }

        return getDataType(fieldType);
    }

    private String getDataType(Type fieldType) {
        if (ClassUtil.isNumber(fieldType.qualifiedTypeName())) {
            return "number";
        } else if (ClassUtil.isString(fieldType.qualifiedTypeName())) {
            return "string";
        } else if (ClassUtil.isBoolean(fieldType.qualifiedTypeName())) {
            return "boolean";
        }

        return "object";
    }
}
