package com.github.kongchen.swagger.docgen.oas;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

public class OpenApiSource {

    @Parameter
    private List<String> resourceClasses;

    @Parameter
    private List<String> resourcePackages;

    @Parameter
    private String openApiFileName;

    @Parameter(required = true)
    private String openApiDirectory;

    @Parameter
    private List<String> outputFormats;

    @Parameter
    private String discoverClasses;

    public List<String> getResourceClasses() {
        return resourceClasses;
    }

    public void setResourceClasses(List<String> resourceClasses) {
        this.resourceClasses = resourceClasses;
    }

    public List<String> getResourcePackages() {
        return resourcePackages;
    }

    public void setResourcePackages(List<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    public String getOpenApiFileName() {
        return openApiFileName;
    }

    public void setOpenApiFileName(String openApiFileName) {
        this.openApiFileName = openApiFileName;
    }

    public String getOpenApiDirectory() {
        return openApiDirectory;
    }

    public void setOpenApiDirectory(String openApiDirectory) {
        this.openApiDirectory = openApiDirectory;
    }

    public List<String> getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(List<String> outputFormats) {
        this.outputFormats = outputFormats;
    }

    public String getDiscoverClasses() {
        return discoverClasses;
    }

    public void setDiscoverClasses(String discoverClasses) {
        this.discoverClasses = discoverClasses;
    }
}
