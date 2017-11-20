package io.swammdoc.plat.xiaoyaoji;

import java.util.*;

import io.swammdoc.common.HttpClientUtils;
import io.swammdoc.common.Logger;
import io.swammdoc.doc.ClassTypeHelper;
import io.swammdoc.doc.Tags;
import io.swammdoc.handler.Handler;
import io.swammdoc.model.ClassModel;
import io.swammdoc.model.MethodModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import io.swammdoc.doc.DocletContext;
import io.swammdoc.model.FieldModel;

/**
 * Created by panwang.chengpw on 2017/9/14.
 */
public class XiaoYaoJiHandler implements Handler {

    String                      host      = "";
    String                      token;
    String                      projectId = "1hW7GG48X8";
    String                      moduleId  = "";
    String                      folderId  = "";

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
        Logger.info("获取token: " + token);
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
        this.host = DocletContext.getOption(Tags.HOST);
        this.projectId = DocletContext.getOption(Tags.PROJECT_ID);
        if (StringUtils.isAnyBlank(host, projectId)) {
            Logger.error("请先设置projectId, host");
        }
        login();
        getProject();
        newModules();
        newFolder();
        for (ClassModel classModel : classModels) {
            for (MethodModel methodModel : classModel.getMethodModels()) {
                Map<String, String> param = Maps.newHashMap();
                param.put("name", methodModel.getTitle());
                param.put("description", methodModel.getDesc());
                param.put("url", classModel.getUrl().concat(methodModel.getUrl()));

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
                List<Type> genericType = ClassTypeHelper.getGenericType(fieldType);
                if (genericType.isEmpty()) {
                    return "array";
                }

                return "array<" + getDataType(genericType.get(0)) + ">";
            }
        }

        return getDataType(fieldType);
    }

    private String getDataType(Type fieldType) {
        if (ClassTypeHelper.isNumber(fieldType.qualifiedTypeName())) {
            return "number";
        } else if (ClassTypeHelper.isString(fieldType.qualifiedTypeName())) {
            return "string";
        } else if (ClassTypeHelper.isBoolean(fieldType.qualifiedTypeName())) {
            return "boolean";
        }

        return "object";
    }
}
