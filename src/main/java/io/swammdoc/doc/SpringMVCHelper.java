package io.swammdoc.doc;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import io.swammdoc.common.Logger;

/**
 * Created by panwang.chengpw on 2017/11/13.
 */
public class SpringMVCHelper {

    public static String       ANNOTATION_REQUEST_MAPPING        = "org.springframework.web.bind.annotation.RequestMapping";
    public static List<String> ANNOTATION_REQUEST_MAPPING_VALUES = Arrays.asList("org.springframework.web.bind.annotation.RequestMapping.value",
                                                                                 "org.springframework.web.bind.annotation.RequestMapping.path");

    public static String getRequestMappingPath(AnnotationDesc[] annotationDescs) {
        String path = StringUtils.EMPTY;
        for (AnnotationDesc annotationDesc : annotationDescs) {
            if (annotationDesc.annotationType().qualifiedTypeName().equals(ANNOTATION_REQUEST_MAPPING)) {
                for (AnnotationDesc.ElementValuePair elementValuePair : annotationDesc.elementValues()) {
                    if (ANNOTATION_REQUEST_MAPPING_VALUES.contains(elementValuePair.element().qualifiedName())) {
                        Object annotationValue = elementValuePair.value().value();
                        if (annotationValue == null) {
                            // ignore
                        } else if (annotationValue instanceof AnnotationValue[]) {
                            AnnotationValue[] annotationValues = (AnnotationValue[]) annotationValue;
                            path = annotationValues[0].value().toString();
                        } else if (annotationValue instanceof String) {
                            path = annotationValue.toString();
                        }
                        Logger.info("request mapping:" + path);
                        return path;
                    }
                }
            }
        }
        return path;
    }
}
