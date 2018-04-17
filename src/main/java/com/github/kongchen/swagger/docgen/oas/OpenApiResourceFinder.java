package com.github.kongchen.swagger.docgen.oas;

import com.google.common.collect.Multimap;
import org.apache.maven.plugin.MojoFailureException;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import javax.ws.rs.ApplicationPath;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Discovers classes that define resources. A class will qualify if any of the following are true:
 * <p>
 * <ul>
 * <li>jax-rs @Path is defined at class and/or method level, together with the http method annotation (@GET, @POST, etc)</li>
 * <li>jax-rs @ApplicationPath is defined at class level</li>
 * <li>An annotation from io.swagger.v3.oas.annotations is defined at the class and/or method level</li>
 * </ul>
 */
class OpenApiResourceFinder {


    private String packageName;

    public OpenApiResourceFinder(String packageName) {
        this.packageName = packageName;
    }

    public Set<String> discoverClasses() throws MojoFailureException {


        ConfigurationBuilder reflectionsConfig = ConfigurationBuilder.build(packageName);
        reflectionsConfig.setScanners(new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new MethodAnnotationsScanner(),
                new FieldAnnotationsScanner(),
                new OpenAPIAnnotationScanner());
        reflectionsConfig.setExpandSuperTypes(false);
        Reflections reflections = new Reflections(reflectionsConfig);

        Set<String> openApiResourceClasses = new TreeSet<String>();

        Set<Class<?>> classesWithPath = reflections.getTypesAnnotatedWith(javax.ws.rs.Path.class);
        openApiResourceClasses.addAll(toClassNames(classesWithPath));

        Set<Class<?>> classesWithApplicationPath = reflections.getTypesAnnotatedWith(ApplicationPath.class);
        openApiResourceClasses.addAll(toClassNames(classesWithApplicationPath));

        Set<Method> annotatedMethods =
                reflections.getMethodsAnnotatedWith(javax.ws.rs.Path.class);
        for (Method method : annotatedMethods) {
            openApiResourceClasses.add(method.getDeclaringClass().getName());
        }

        Multimap<String, String> oasAnnotationsStore = reflections.getStore().get(OpenAPIAnnotationScanner.class.getSimpleName());
        Collection<String> classesWithOASAnnotations = oasAnnotationsStore.get(OpenAPIAnnotationScanner.OAS_ANNOTATIONS_PACKAGE);
        openApiResourceClasses.addAll(classesWithOASAnnotations);

        return openApiResourceClasses;
    }


    private static Set<String> toClassNames(Set<Class<?>> classes) {
        Set<String> classNames = new TreeSet<String>();
        for (Class<?> clazz : classes) {
            classNames.add(clazz.getName());
        }

        return classNames;
    }

}
