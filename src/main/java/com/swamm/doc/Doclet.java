package com.swamm.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.swamm.common.DocletContext;
import com.swamm.common.DocletLog;
import com.swamm.common.DocletUtil;
import com.swamm.common.Tags;
import com.swamm.rap.RapHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Created by chengpanwang on 2016/10/19.
 */
public class Doclet {

    public static boolean start(RootDoc root) {
        DocletContext.init(root);

        DocletLog.log("开始解析源代码。。。");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String tagName = readOptions(root.options());
        writeContents(root, tagName);

        DocletLog.log("解析源代码完成， 耗时：" + stopWatch.getTime() + " ms");
        return true;
    }

    private static void writeContents(RootDoc rootDoc, String tagName) {

        ClassDoc[] classes = rootDoc.classes();

        Map<String, String> optionMap = new HashMap<>();
        if (StringUtils.isNotBlank(tagName)) {
            for (String pair : StringUtils.split(tagName, Tags.SEPARATOR)) {
                String[] kv = StringUtils.split(pair, Tags.KV_SEPARATOR);
                if (kv != null && kv.length == 2) {
                    optionMap.put(kv[0], kv[1]);
                }
            }
        }

        List<ClassModel> classModels = new ArrayList<ClassModel>();

        for (ClassDoc classDoc : classes) {

            // 只解析接口
            if (!classDoc.isInterface()) {
                continue;
            }

            if (StringUtils.isNotBlank(optionMap.get(Tags.CLASS))) {
                if (!ArrayUtils.contains(StringUtils.split(optionMap.get(Tags.CLASS), ","), classDoc.simpleTypeName())) {
                    continue;
                }
            }


            DocletLog.log("开始解析接口："  + classDoc.qualifiedTypeName());

            ClassModel classModel = new ClassModel();
            classModel.setDesc(classDoc.commentText());
            classModel.setName(classDoc.name());
            classModel.setType(classDoc.qualifiedTypeName());

            classModels.add(classModel);

            classModel.setMethodModels(DocletUtil.getMethodModels(classDoc));

            DocletLog.log("解析接口："  + classDoc.qualifiedTypeName() + " 完成");
        }

        DocletLog.log("解析结果：");
        DocletLog.log(JSON.toJSONString(classModels));

        new RapHandler().execute(rootDoc, classModels, optionMap);
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
