package com.github.kongchen.swagger.docgen.oas;

import org.reflections.scanners.AbstractScanner;

import java.lang.annotation.Inherited;
import java.util.List;

/**
 * {@link org.reflections.scanners.Scanner Reflections Scanner} which captures classes that either:
 * <ul>
 * <li>Have a type level annotation that belongs to the <i>io.swagger.v3.oas.annotations</i> package<./li>
 * <li>Have a method that declares an annotation that belongs to the <i>io.swagger.v3.oas.annotations</i> package.</li>
 * </ul>
 */
class OpenAPIAnnotationScanner extends AbstractScanner {

    static final String OAS_ANNOTATIONS_PACKAGE = "io.swagger.v3.oas.annotations";

    @Override
    public void scan(Object cls) {

        final String className = getMetadataAdapter().getClassName(cls);
        for (String annotationType : (List<String>) getMetadataAdapter().getClassAnnotationNames(cls)) {
            if (annotationType.startsWith(OAS_ANNOTATIONS_PACKAGE) ||
                    annotationType.equals(Inherited.class.getName())) { //as an exception, accept Inherited as well
                getStore().put(annotationType, className);
                getStore().put(OAS_ANNOTATIONS_PACKAGE, className);
            }
        }

        for (Object method : getMetadataAdapter().getMethods(cls)) {
            for (String methodAnnotation : (List<String>) getMetadataAdapter().getMethodAnnotationNames(method)) {
                if (methodAnnotation.startsWith(OAS_ANNOTATIONS_PACKAGE)) {
                    getStore().put(methodAnnotation, getMetadataAdapter().getMethodFullKey(cls, method));
                    getStore().put(OAS_ANNOTATIONS_PACKAGE, className);
                }
            }
        }
    }
}
