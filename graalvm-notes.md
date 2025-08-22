This branch tracks current efforts to compile Reeve using GraalVM's native-image. 

# Compilation
Local installation of GraalVM JDK is required. On MacOS GraalVM JDKs are available using brew:
```bash
brew install graalvm-jdk@21
```

Compile to native binary:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home/ ./gradlew clean nativeCompile
```

`Dockerfile.native` builds the native binary inside docker and builds a native docker image. 

New profiles where added to `docker-compose.yml` to support local development:
* `native`: Builds and starts monolith backend as native-image with all 
    dependencies.
* `jvm-agent-profiler`: Builds and starts monolith backend on GraalVM JVM and starts the
    native-image-agent profiler which automatically traces metadata information like
    reflections and external resources. Metadata is saved to 
    `native-image-agent/META-INF/native-image`. 
    * `native-image-agent/fix-and-copy.sh` is used to copy captured metadata to cf-application's classpath. The script 
        takes care of fixing a known issue when tracing Spring applications: 
        [https://github.com/spring-projects/spring-framework/issues/35118#issuecomment-3047893324]
* `java`: Builds and starts api and publisher as seperate components

# State of GraalVM native compilation
* Compilation enabling all backend features at once. Backend can only run as a monolith (api and publisher)
* Compile time configuration for Spring profile `dev--yaci-dev-kit`
* GraalVM native-image metadata for reflections, resources, ...

## Future work
### Runtime configuration enabling/disabling components (e.g. `LOB_BLOCKCHAIN_PUBLISHER_ENABLED: true`)
Efforts related to this topic are tracked in [cf-reeve-platform](https://github.com/cardano-foundation/cf-reeve-platform/tree/chore/native-image).
`support/src/main/java/org/cardanofoundation/lob/app/support/conditional/EnableIf.java` implements an annotation used 
by `support/src/main/java/org/cardanofoundation/lob/app/support/conditional/EnableIfAspect.java`. The aspect intercepts 
all calls to public methods within the annotated scope and proceeds method execution depending on a given property.
This replaces `@ConditionalOnProperty` by Spring which is not supported in GraalVM native code. 

Unfortunately, `@EnableIf` cannot be used in cases where more than on configuration Beans of a type exists. For example
`support/src/main/java/org/cardanofoundation/lob/app/support/spring_web/SecurityConfig.java` and 
`support/src/main/java/org/cardanofoundation/lob/app/support/spring_web/DisabledSecurity.java` both supply a bean 
named `securityConfig`. `EnableIfAspect` cannot enforce that only one of the beans is present at any given time. 

`support/src/main/java/org/cardanofoundation/lob/app/support/spring_web/SecurityConfiguration.java` tries to solve this
by providing a `BeanDefinitionRegistryPostProcessor` in which we check whether a property is enabled or not and 
provide bean definitions accordingly. The current bean definitions however do not instruct Spring to create and provide 
beans form `@Bean` annotated methods inside these configuration beans. This needs to be solved and the solution needs 
to be applied to code with similar issues.
 
### Runtime spring profiles
We use spring profiles to track properties for dev and production deployments. We need to verify if setting 
`spring.profiles.active` in native images leads to those configs being loaded from resources. If that's not the case
we might need to change all environments and load properties externally (by providing environment variables for example).

### native-image resources
Currently all resources required by `native-image` are tracked in 
`cf-application/src/main/resources/META-INF/native-image`. Since they were created entirely by tracing the application 
using the native-image-agent, they still need to be cleaned up. A lot of reflection information from external libraries
are already available by those libraries and are currenlty dupliacted. We want to find a working native-image 
configuration that is as minimal as possible. 

Furthermore, native-image resources should be organised and tracked by component which means that the current 
configuration entries should be moved to the correct component in `cf-reeve-platform`. This would enable members of the
community to use Reeve components in their application while still being able to compile using `native-image`.
