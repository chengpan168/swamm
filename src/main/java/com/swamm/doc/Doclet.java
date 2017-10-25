package com.swamm.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.swamm.xiaoyaoji.XiaoYaoJiHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.*;
import com.swamm.common.DocletContext;
import com.swamm.common.DocletLog;
import com.swamm.common.DocletUtil;
import com.swamm.common.Tags;

/**
 * Created by chengpanwang on 2016/10/19.
 */
public class Doclet {

    public static boolean start(RootDoc root) {

        DocletLog.log("开始解析源代码。。。");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String tagName = readOptions(root.options());
        DocletContext.init(root, tagName);
        writeContents(root, tagName);

        DocletLog.log("解析源代码完成， 耗时：" + stopWatch.getTime() + " ms");
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

            } else {
                boolean isController = false;
                for( AnnotationDesc annotationDesc : classDoc.annotations()) {
                    List<String> controllers = Arrays.asList("org.springframework.web.bind.annotation.RestController", "org.springframework.web.bind.annotation.Controller");
                    String annotationType = annotationDesc.annotationType().qualifiedTypeName();
                    if (controllers.contains(annotationType)) {
                        isController = true;
                        break;
                    }
                }
                if (!isController) {
                    continue;
                }
            }

            // http spring mvc 协议

            if (StringUtils.isNotBlank(DocletContext.getOption(Tags.CLASS))) {
                if (!ArrayUtils.contains(StringUtils.split(DocletContext.getOption(Tags.CLASS), ","), classDoc.simpleTypeName())) {
                    continue;
                }
            }


            DocletLog.log("开始解析接口："  + classDoc.qualifiedTypeName());

            ClassModel classModel = new ClassModel();
            classModel.setDesc(classDoc.commentText());
            classModel.setName(classDoc.name());
            classModel.setType(classDoc.qualifiedTypeName());
            classModel.setUrl(getUrl(classDoc));
            classModels.add(classModel);

            classModel.setMethodModels(DocletUtil.getMethodModels(classDoc));

            DocletLog.log("解析接口："  + classDoc.qualifiedTypeName() + " 完成");
        }

        DocletLog.log("解析结果：");
        DocletLog.log(JSON.toJSONString(classModels));

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
                            DocletLog.log("request mapping:" + elementValuePair.value());
                            return elementValuePair.value().toString();
                        }
                    }
                }
            }
        }
        return null;
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
