package com.swamm.rap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.swamm.common.HttpUtils;
import com.swamm.doc.ClassModel;
import com.swamm.doc.FieldModel;
import com.swamm.doc.MethodModel;
import com.swamm.handler.Handler;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class RapHandler implements Handler {

    private String host = "http://127.0.0.1:8080";
    private String loadUrl = "/workspace/loadWorkspace.do";
    private String saveUrl = "/workspace/checkIn.do";
    private String lockUrl = "/workspace/lock.do";

    private String projectId = "10";
    int moduleId = 38;

    @Override
    public void execute(List<ClassModel> classModels, Map<String, String> options) {

        String project = HttpUtils.doPost(host + loadUrl, Collections.singletonMap("projectId", projectId));

        Map<String, Object> jsonObj = JSON.parseObject(project, Map.class);
        String projectData = jsonObj.get("projectData").toString();

        System.out.println(projectData);

        Project p = JSON.parseObject(projectData, Project.class);

        System.out.println(p);
        System.out.println(project);

        // 删除原有module
        List<ObjectItem> deleteList = new ArrayList<>();
      /*  List<Module> deleteActionList = p.getModuleList();
        if (deleteActionList != null && deleteActionList.size() > 0) {
            for (Module module : deleteActionList) {
                ObjectItem item = new ObjectItem();
                item.setClassName("Module");
                item.setId(module.getId());
                deleteList.add(item);
            }
        }*/

        Module module = new Module();
        module.setId(getId());
        module.setName("All");

        Page page = new Page();
        page.setId(getId());
        page.setModuleId(module.getId());
        page.setName("All");

        module.setPageList(Arrays.asList(page));


        int id = -1;
        List<Action> actionList = new ArrayList<>();
        for (ClassModel classModel : classModels) {
            for (MethodModel methodModel : classModel.getMethodModels()) {
                Action action = new Action();

                action.setPageId(page.getId());
                action.setId(--id);
                action.setName(methodModel.getTitle());
                action.setDescription(methodModel.getDesc());
                action.setRequestType(2);
                action.setRequestUrl(classModel.getType() + "." + methodModel.getName());

                action.setRequestParameterList(convertParameter(methodModel.getParamModels()));

                action.setResponseParameterList(convertParameter(methodModel.getReturnModel()));

                actionList.add(action);
            }
        }

        page.setActionList(actionList);

        //        System.out.println("class models");
        //        System.out.println(classModels);

        System.out.println(p);

        List<Module> moduleList = new ArrayList<>(p.getModuleList());
        moduleList.add(module);
        p.setModuleList(moduleList);

        System.out.println("action list");
        System.out.println(JSON.toJSONString(actionList));

        save(p, deleteList);

    }

    int id = -1;
    public int getId() {
        return --id;
    }

    private void save(Project p, List<ObjectItem> deleteList) {
        String res = null;
        try {
            res = HttpUtils.doGet(host + lockUrl + "?id=" + projectId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSON.parseObject(res);

        System.out.println("lock project :" + res);
        if (!jsonObject.get("isOk").toString().equals("true")) {
            System.out.println("lock project success");
            return;
        }

        Map<String, String> param = new HashMap<>();
        param.put("id", projectId);
        param.put("versionPosition", "4");
        param.put("description", "quick save");
        param.put("deletedObjectListData", JSON.toJSONString(deleteList));

        param.put("projectData", JSON.toJSONString(p));

        System.out.println("save project");
        System.out.println(JSON.toJSONString(param));

        String result = HttpUtils.doPost(host + saveUrl, param);
        System.out.println(result);

    }

    private List<Parameter> convertParameter(FieldModel fieldModel) {
        if (fieldModel == null) {
            return Collections.emptyList();
        }
        return convertParameter(Arrays.asList(fieldModel));
    }
    private List<Parameter> convertParameter(List<FieldModel> fieldModels) {
        if (fieldModels == null || fieldModels.size() == 0) {
            return Collections.emptyList();
        }

        List<Parameter> requestParameterList = new ArrayList<>();
        int i = 0;
        for (FieldModel fieldModel : fieldModels) {
            Parameter parameter = new Parameter();
            parameter.setIdentifier(fieldModel.getName());
            parameter.setName(i++ + ":" + fieldModel.getDesc());
            parameter.setDataType("object");
            parameter.setRemark(fieldModel.getDesc());

            parameter.setParameterList(convertParameter(fieldModel.getInnerField()));

            requestParameterList.add(parameter);

        }

        return requestParameterList;


    }


    public static void main(String[] args) {
//        new RapHandler().execute(null, null);

        try {
            String res = HttpUtils.doGet("http://127.0.0.1:8080" + "/workspace/lock.do" + "?id=10");
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
