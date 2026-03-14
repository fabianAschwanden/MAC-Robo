# Upgrade Plan: robo (20260314084444)

- **Generated**: 2026-03-14 08:57 UTC
- **HEAD Branch**: HEAD
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- Java 25.0.2: /Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin (system default; used for baseline)
- Java 21: /Users/fabian/.jdk/jdk-21.0.8/jdk-21.0.8+9/Contents/Home/bin (installed in Step 1)

**Build Tools**
- Maven 3.9.8: /usr/local/Cellar/maven/3.9.8/bin/mvn

## Guidelines

- Upgrade runtime/compile target to **Java 21 (LTS)**.
- Preserve existing behavior and keep changes minimal.
- Ensure all tests pass after the upgrade (100% pass rate).

## Options

- Working branch: appmod/java-upgrade-20260314084444
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java from 17 to 21 (compile/source/target)

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java (maven.compiler.source/target) | 17 | 21 | User requested runtime target |
| javafx-controls | 21.0.2 | 21.0.2 | - |
| logback-classic | 1.4.11 | 1.4.11 | - |
| jackson-databind | ${jackson.version} | (unknown) | - |
| jna | 5.14.0 | 5.14.0 | - |
| jnativehook | 2.3.5 | 2.3.5 | - |

### Derived Upgrades

- Update Maven compiler plugin configuration to use `source`/`target` 21, which is required to compile for Java 21.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Java 21 is not installed on the system; the build must run with a Java 21 JDK to validate the upgrade.
  - **Changes to Make**:
    - [ ] Install Java 21 via `install_jdk`.
    - [ ] Confirm the installed JDK path and update this plan.
  - **Verification**:
    - Command: `#list_jdks`
    - Expected: Java 21 installation present and usable.

- **Step 2: Setup Baseline**
  - **Rationale**: Capture current compilation and test results before making changes to compare against post-upgrade behavior.
  - **Changes to Make**:
    - [ ] Run baseline build and test using the current environment (default JDK 25).
  - **Verification**:
    - Command: `mvn clean test` (uses system default JDK)
    - Expected: Baseline compilation and tests succeed (or document failures).

- **Step 3: Update Build to Java 21**
  - **Rationale**: Update the project’s compiler settings to target Java 21 and ensure Maven uses the Java 21 JDK.
  - **Changes to Make**:
    - [ ] Update `pom.xml` maven-compiler-plugin configuration to set `<source>21</source>` and `<target>21</target>`.
    - [ ] Run builds with `JAVA_HOME` set to the installed Java 21 JDK.
  - **Verification**:
    - Command: `JAVA_HOME=<path-to-jdk21> mvn clean test-compile` then `JAVA_HOME=<path-to-jdk21> mvn test`
    - Expected: Compilation succeeds; any test failures are documented and addressed in final validation.

- **Step 4: Final Validation**
  - **Rationale**: Confirm the project compiles and all tests pass under Java 21, meeting Upgrade Success Criteria.
  - **Changes to Make**:
    - [ ] Ensure `pom.xml` reflects Java 21 compiler source/target.
    - [ ] Resolve any remaining compilation errors or test failures.
  - **Verification**:
    - Command: `JAVA_HOME=<path-to-jdk21> mvn clean test`
    - Expected: Compilation SUCCESS and 100% tests pass.

## Key Challenges

- Ensuring Maven uses the installed Java 21 JDK rather than the system default (Java 25).
- Verifying native dependencies (e.g., JNA/JNativeHook) remain compatible when running on Java 21.
- Maintaining 100% test pass rate after the runtime upgrade.
