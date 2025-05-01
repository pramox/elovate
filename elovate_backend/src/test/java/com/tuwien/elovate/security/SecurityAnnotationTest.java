package com.tuwien.elovate.security;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class SecurityAnnotationTest {

    private static final List<Class<? extends Annotation>> PERMITTED_ENDPOINT_ANNOTATIONS = List.of( //
            RequestMapping.class, PostMapping.class, GetMapping.class, DeleteMapping.class, PutMapping.class, PatchMapping.class //
    );

    private static final List<Class<? extends Annotation>> PERMITTED_SECURITY_ANNOTATIONS = List.of( //
            PermitAll.class, RolesAllowed.class, Secured.class, PreAuthorize.class, PostAuthorize.class //
    );

    /**
     * Checks whether every EndPoint has a Security Annotation and fails if one doesn't.
     */
    @Test
    void when_MethodIsEndpoint_Expect_MethodHasSecurityAnnotationOrPermitAll() throws IOException, ClassNotFoundException {
        String basePackage = "com.tuwien.elovate";

        List<Class<?>> classes = findClassesWithAnnotation(basePackage);

        for (Class<?> controllerClass : classes) {

            if (controllerClass == null) {
                continue;
            }

            Method[] possibleEndpointMethods = controllerClass.getDeclaredMethods();
            for (Method method : possibleEndpointMethods) {
                if (isEndpointMethod(method)) {
                    if (!hasSecurityAnnotation(method)) {
                        fail("Endpoint does not have a valid security annotation! Endpoint in class: " + controllerClass.getName() + ", endpoint: " + method.getName());
                    }
                }
            }
        }
    }

    static List<Class<?>> findClassesWithAnnotation(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                File packageDir = new File(resource.getFile());
                annotatedClasses.addAll(findRestControllerClasses(packageDir, basePackage));
            }
        }

        return annotatedClasses;
    }

    private static List<Class<?>> findRestControllerClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subPackage = packageName + "." + file.getName();
                    annotatedClasses.addAll(findRestControllerClasses(file, subPackage));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(RestController.class)) {
                        annotatedClasses.add(clazz);
                    }
                }
            }
        }
        return annotatedClasses;
    }

    private boolean isEndpointMethod(Method method) {
        for (Class<? extends Annotation> clazz : PERMITTED_ENDPOINT_ANNOTATIONS) {
            if (method.isAnnotationPresent(clazz)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSecurityAnnotation(Method method) {
        for (Class<? extends Annotation> clazz : PERMITTED_SECURITY_ANNOTATIONS) {
            if (method.isAnnotationPresent(clazz)) {
                return true;
            }
        }
        return false;
    }
}
