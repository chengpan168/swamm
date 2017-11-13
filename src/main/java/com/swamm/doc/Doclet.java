package com.swamm.doc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.*;
import com.swamm.common.Logger;
import com.swamm.model.ClassModel;
import com.swamm.plat.xiaoyaoji.XiaoYaoJiHandler;

/**
 * javadoc 主类， 从这里开始解析，类， 方法
 * Created by chengpanwang on 2016/10/19.
 */
public class Doclet {

    public static boolean start(RootDoc root) {

        Logger.info("开始解析源代码。。。");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String tagName = readOptions(root.options());
        DocletContext.init(root, tagName);
        writeContents(root, tagName);

        Logger.info("解析源代码完成， 耗时：" + stopWatch.getTime() + " ms");
        return true;
    }

    private static void writeContents(RootDoc rootDoc, String tagName) {

        ClassDoc[] classes = rootDoc.classes();

        List<ClassModel> classModels = new ArrayList<ClassModel>();

        for (ClassDoc classDoc : classes) {

            // dubbo 协议
            if (Tags.PROTOCOL_DUBBO.equals(DocletContext.PROTOCOL)) {
                // 只解析接口
                if (!classDoc.isInterface()) {
                    continue;
                }

            }
            // spring mvc
            else if (Tags.PROTOCOL_CONTROLLER.equals(DocletContext.PROTOCOL)){
                if (!DocletHelper.isController(classDoc)) {
                    continue;
                }
            } else {
                Logger.info("目前只支持dubbo, spring mvc，其它暂未支持");
                return;
            }

            // http spring mvc 协议
            if (!DocletHelper.isInclude(classDoc)) {
                continue;
            }


            Logger.info("开始解析接口：" + classDoc.qualifiedTypeName());

            ClassModel classModel = new ClassModel();
            classModel.setDesc(classDoc.commentText());
            classModel.setName(classDoc.name());
            classModel.setType(classDoc.qualifiedTypeName());
            classModel.setUrl(getUrl(classDoc));
            classModels.add(classModel);

            classModel.setMethodModels(DocletHelper.getMethodModels(classDoc));

            Logger.info("解析接口：" + classDoc.qualifiedTypeName() + " 完成");
        }

        Logger.info("解析完成：");
        Logger.info(JSON.toJSONString(classModels));

        new XiaoYaoJiHandler().execute(rootDoc, classModels);
    }

    private static String getUrl(ClassDoc classDoc) {
        if (Tags.PROTOCOL_DUBBO.equals(DocletContext.PROTOCOL)) {
            return "/" + classDoc.qualifiedTypeName();
        } else {
            for (AnnotationDesc annotationDesc : classDoc.annotations()) {
                if (annotationDesc.annotationType().qualifiedTypeName().equals("org.springframework.web.bind.annotation.RequestMapping")) {
                    for (AnnotationDesc.ElementValuePair elementValuePair : annotationDesc.elementValues()) {


                        if (elementValuePair.element().qualifiedName().equals("org.springframework.web.bind.annotation.RequestMapping.value")) {
                            Logger.info("request mapping:" + elementValuePair.value());
                            return elementValuePair.value().toString();
                        }
                    }
                }
            }
        }
        return "";
    }

    private static String readOptions(String[][] options) {
        String tagName = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-tag")) {
                tagName = opt[1];
            }
        }
        return tagName;
    }

    public static int optionLength(String option) {
        if (option.equals("-tag")) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        boolean foundTagOption = false;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-tag")) {
                if (foundTagOption) {
                    reporter.printError("Only one -tag option allowed.");
                    return false;
                } else {
                    foundTagOption = true;
                }
            }
        }
        if (!foundTagOption) {
            reporter.printWarning("Usage: javadoc -tag mytag -doclet ListTags ...");
        }
        return true;
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
}
