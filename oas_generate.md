# oas-generate goal

This goal lets you generate [Open API](https://github.com/OAI/OpenAPI-Specification) specification documents like you would with the `generate` goal.


## Usage

Import the plugin in your project by adding the following configuration in your plugins block:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.kongchen</groupId>
      <artifactId>swagger-maven-plugin</artifactId>
      <version>${swagger-maven-plugin-version}</version>
      <configuration>
        <openApiSource>
          ...
        </openApiSource>
      </configuration>
    </plugin>
  </plugins>
</build>
```


## Configure the goal

```xml
<executions>
  <execution>
    <phase>compile</phase>
    <goals>
      <goal>oas-generate</goal>
    </goals>
  </execution>
</executions>
```

Then run `mvn compile` or `mvn swagger:oas-generate`

## Configuration for `openApiSource`

The `openApiSource` defines a single source for an Open API document.


| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `resourceClasses` | List of classes containing Open API annotations from `swagger-annotations` or jax-rs `@Path`/`@ApplicationPath` annotations. Each item must be located inside a tag. |
| `resourcePackages` | List of packages containging classes with Open API annotations from `swagger-annotations` or jax-rs `@Path`/`@ApplicationPath` annotations.  Each item must be located inside a tag. If `resourceClasses` or `discoverClasses` is specified, `resourcePackages` will be ignored due to the usage of `io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner`. |
| `discoverClasses` | A package name from which classes should be discovered, see [class discovery](#class-discovery) for the criteria used when determining if a class qualifies or not.  |
| `openApiDirectory` **required** | The directory where the generated `openapi.json` should reside.  |
| `openApiFileName` | The name of the generated file: `filename.ext`. Defaults to `openapi` if not provided. |
| `outputFormats` | The format types of the generated Open API spec. Valid values are `json`, `yaml` or both `json,yaml`. Defaults to `json`. |

### Class Discovery

Both `resourceClasses` and `resourcePackages` rely on the `swagger-core` [https://github.com/swagger-api/swagger-core/blob/v2.0.1/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/integration/JaxrsAnnotationScanner.java) which requires that either a `@Path` or `@OpenAPIDefinition` annotation be present at the class level.

However, it's possible to define a resource without a class level annotation of `@Path` and with method annotations of `@Operation`. In a Jax-RS environment, when generating the Open API spec through the [swagger-core resource](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-Configuration#configuration-properties), these elements will show up in the Open API. To provide a consistent one to one mapping, consumers can use `discoverClasses` to add additional classes to the configuration of the API.

A class qualifies as an Open API defining resource if any of the following hold true:

* A jax-rs `@Path` annotation is defined at class and/or method level, together with the http method annotation (@GET, @POST, etc). As defined by [`@Operation`](https://github.com/swagger-api/swagger-core/blob/v2.0.1/modules/swagger-annotations/src/main/java/io/swagger/v3/oas/annotations/Operation.java#L37)
* A jax-rs `@ApplicationPath` annotation is defined at class level.
* Any of the swagger annotations from `io.swagger.v3.oas.annotations` is defined at the class and/or method level.

Optionally, consumers can define all the classes with the `resourceClasses` element and by pass the limitation all together. The `discoverClasses` option is only provided as a work around.


## Sample Configuration

**Specifying Resource Classes**
```xml
<project>
...
<build>
  <plugins>
    <plugin>
      <groupId>com.github.kongchen</groupId>
      <artifactId>swagger-maven-plugin</artifactId>
      <version>${swagger-maven-plugin-version}</version>
      <configuration>
        <openApiSource>
          <resourceClasses>
            <resourceClass>io.swagger.sample.petstore.example.ExamplesResource</resourceClass>
            <resourceClass>io.swagger.sample.petstore.operation.FullyAnnotatedOperationResource</resourceClass>
          </resourceClasses>
          <openApiDirectory>${project.basedir}/openapi/</openApiDirectory>
          <openApiFileName>petstore</openApiFileName>
          <outputFormats>yaml,json<outputFormats>
        </openApiSource>
      </configuration>
    </plugin>
  </plugins>
</build>
```

**Specifying Packages**
```xml
<project>
...
<build>
  <plugins>
    <plugin>
      <groupId>com.github.kongchen</groupId>
      <artifactId>swagger-maven-plugin</artifactId>
      <version>${swagger-maven-plugin-version}</version>
      <configuration>
        <openApiSource>
          <resourcePackages>
            <resourcePackage>io.swagger.sample.petstore.example</resourcePackage>
            <resourcePackage>io.swagger.sample.petstore.operation</resourcePackage>
          </resourcePackages>
          <openApiDirectory>${project.basedir}/openapi/</openApiDirectory>
          <openApiFileName>petstore</openApiFileName>
          <outputFormats>yaml,json<outputFormats>
        </openApiSource>
      </configuration>
    </plugin>
  </plugins>
</build>
```

**Using class discovery**
```xml
 <project>
 ...
 <build>
   <plugins>
     <plugin>
       <groupId>com.github.kongchen</groupId>
       <artifactId>swagger-maven-plugin</artifactId>
       <version>${swagger-maven-plugin-version}</version>
       <configuration>
         <openApiSource>
           <discoverClasses>io.swagger.sample.petstore</discoverClasses>
           <openApiDirectory>${project.basedir}/openapi/</openApiDirectory>
           <openApiFileName>petstore</openApiFileName>
           <outputFormats>yaml,json<outputFormats>
         </openApiSource>
       </configuration>
     </plugin>
   </plugins>
 </build>
```
