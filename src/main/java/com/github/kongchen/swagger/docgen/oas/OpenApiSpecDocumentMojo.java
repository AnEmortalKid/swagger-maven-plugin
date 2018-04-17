package com.github.kongchen.swagger.docgen.oas;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Maven mojo that generates Open API Specification documents.
 */
@Mojo(name = "oas-generate", defaultPhase = LifecyclePhase.COMPILE, configurator = "include-project-dependencies",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class OpenApiSpecDocumentMojo extends AbstractMojo {

    @Parameter
    private OpenApiSource openApiSource;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * A flag indicating if the generation should be skipped.
     */
    @Parameter(property = "swagger.skip", defaultValue = "false")
    private boolean skipSwaggerGeneration;

    private String projectEncoding;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipSwaggerGeneration) {
            getLog().info("Swagger OpenAPI generation is skipped.");
            return;
        }

        validateConfiguration(openApiSource);

        if (project != null) {
            projectEncoding = project.getProperties().getProperty("project.build.sourceEncoding");
        }

        SwaggerConfiguration oasConfig = createSwaggerConfiguration();
        OpenAPI openAPI = null;
        try {
            openAPI = new JaxrsOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new MojoFailureException("A configuration problem occurred", e);
        }


        getLog().debug(String.format("Loaded: %n %s %n", Json.pretty(openAPI)));

        File outputDir = new File(openApiSource.getOpenApiDirectory());
        if (outputDir.isFile()) {
            throw new MojoFailureException(String.format("OpenAPI-outputDirectory[%s] must be a directory!", openApiSource.getOpenApiDirectory()));
        }

        if (!outputDir.exists()) {
            try {
                FileUtils.forceMkdir(outputDir);
            } catch (IOException e) {
                throw new MojoFailureException(String.format("Create OpenAPI-outputDirectory[%s] failed.", openApiSource.getOpenApiDirectory()));
            }
        }

        String fileName = openApiSource.getOpenApiFileName() != null ? openApiSource.getOpenApiFileName() : "openapi";

        List<String> outputFormats = new LinkedList<String>();
        if (openApiSource.getOutputFormats() != null) {
            outputFormats.addAll(openApiSource.getOutputFormats());
        } else {
            outputFormats.add("JSON");
        }

        for (String format : outputFormats) {
            try {
                OutputFormat outputFormat = OutputFormat.valueOf(format.toUpperCase());
                switch (outputFormat) {
                    case JSON:
                        FileUtils.write(new File(outputDir, fileName + ".json"), Json.pretty(openAPI), projectEncoding);
                        break;
                    case YAML:
                        FileUtils.write(new File(outputDir, fileName + ".yaml"), Yaml.pretty().writeValueAsString(openAPI), projectEncoding);
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Declared output format [%s] is not supported", format));
                }
            } catch (Exception e) {
                throw new MojoFailureException(String.format("Failed while generating open API for format [%s]", format), e);
            }
        }
    }

    enum OutputFormat {
        JSON,
        YAML
    }

    private SwaggerConfiguration createSwaggerConfiguration() throws MojoFailureException {
        SwaggerConfiguration oasConfig = new SwaggerConfiguration();

        // TODO load from config file
        // https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-Configuration#configuration-file

        Set<String> resourceClasses = new TreeSet<String>();
        if (openApiSource.getResourceClasses() != null) {
            resourceClasses.addAll(openApiSource.getResourceClasses());
        }

        if (openApiSource.getResourcePackages() != null) {
            oasConfig.resourcePackages(new TreeSet<String>(openApiSource.getResourcePackages()));
        }

        if (!StringUtils.isBlank(openApiSource.getDiscoverClasses())) {
            Set<String> discoveredClasses = new OpenApiResourceFinder(openApiSource.getDiscoverClasses()).discoverClasses();
            getLog().debug(String.format("Discovered classes: %s", discoveredClasses));
            resourceClasses.addAll(discoveredClasses);
        }

        oasConfig.setResourceClasses(resourceClasses);

        // TODO support this config
        // oasConfig.setReadAllResources();

        return oasConfig;
    }

    private void validateConfiguration(OpenApiSource openApiSource) throws MojoFailureException {
        if (openApiSource == null) {
            throw new MojoFailureException("Cannot execute oas-generate without an openApiSource");
        }

        if (openApiSource.getOpenApiDirectory() == null) {
            throw new MojoFailureException("An openApiDirectory must be specified.");
        }

        if (!definesClassSources()) {
            throw new MojoFailureException("One of [ resourceClasses, resourcePackages, discoverClasses ] must be defined.");
        }
    }

    /**
     * Determines if a source to find resource classes was configured or not, if any of the following exist then we can generate a document that won't be blank
     * <ul>
     * <li>resourceClasses</li>
     * <li>resourcePackages</li>
     * <li>openApiConfigurationPath</li>
     * <li>discoverResources</li>
     * </ul>
     */
    private boolean definesClassSources() {
        if (openApiSource.getResourceClasses() != null) {
            return true;
        }

        if (openApiSource.getResourcePackages() != null) {
            return true;
        }

        if (!StringUtils.isBlank(openApiSource.getDiscoverClasses())) {
            return true;
        }

        // TODO include openApiConfigurationPath

        return false;
    }
}
