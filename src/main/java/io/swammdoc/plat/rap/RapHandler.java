package io.swammdoc.plat.rap;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import io.swammdoc.common.Logger;
import io.swammdoc.doc.ClassTypeHelper;
import io.swammdoc.doc.DocletContext;
import io.swammdoc.common.HttpUtils;
import io.swammdoc.model.ClassModel;
import io.swammdoc.model.FieldModel;
import io.swammdoc.model.MethodModel;
import io.swammdoc.handler.Handler;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class RapHandler implements Handler {

    private String host = "http://rap.3dker.cn";
//    private String host = "http://127.0.0.1:8080";
    private String loadUrl = "/workspace/loadWorkspace.do";
    private String saveUrl = "/workspace/checkIn.do";
    private String lockUrl = "/workspace/lock.do";

    private String projectId = "10";

    @Override
    public void execute(RootDoc rootDoc, List<ClassModel> classModels) {

        String optProjectId = DocletContext.getOption("projectId");
        if (optProjectId != null) {
            projectId = optProjectId;
        }

        String optHost = DocletContext.getOption("host");
        if (optHost != null) {
            host = "http://" + optHost;
            if (DocletContext.getOption("port") != null) {
                host += ":" + DocletContext.getOption("port");
            }
        }

        String project = HttpUtils.doPost(host + loadUrl, Collections.singletonMap("projectId", this.projectId));

//        System.out.println("取得工程结果");
//        System.out.println(project);


        Map<String, Object> jsonObj = JSON.parseObject(project, Map.class);
        String projectData = String.valueOf(jsonObj.get("projectData"));
        if (project == null) {
            System.out.println("取得工程结果出错");
            System.out.println(project);
        }


        Project p = JSON.parseObject(projectData, Project.class);

//        System.out.println(p);

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
                action.setRequestUrl("/dubbo/" + classModel.getType() + "." + methodModel.getName());

                action.setRequestParameterList(convertParameter(methodModel.getParamModels()));

                action.setResponseParameterList(convertParameter(methodModel.getReturnModel().getInnerFields()));

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

        System.out.println("接口列表：");
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

        System.out.println("锁定工程" );
        if (!jsonObject.get("isOk").toString().equals("true")) {
            Logger.info("锁定工程失败");
            return;
        } else {
            Logger.info("锁定工程成功");
        }

        Map<String, String> param = new HashMap<>();
        param.put("id", projectId);
        param.put("versionPosition", "4");
        param.put("description", "quick save");
        param.put("deletedObjectListData", JSON.toJSONString(deleteList));

        param.put("projectData", JSON.toJSONString(p));

        Logger.info("保存工程。。。");
//        System.out.println(JSON.toJSONString(param));

        String result = HttpUtils.doPost(host + saveUrl, param);

        Map map = JSON.parseObject(res, Map.class);
        if ("true".equals(String.valueOf(map.get("isOk")))) {
            Logger.info("保存工程成功");
        }

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

            parameter.setDataType(getDataType(fieldModel));
            parameter.setRemark(fieldModel.getDesc());

            parameter.setParameterList(convertParameter(fieldModel.getInnerFields()));

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
