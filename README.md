# QE BOM & Base Java Framework (`qe-bom-base-java`)

A modular Maven framework designed for Quality Engineering (QE) test automation. It provides a standardized Bill of Materials (**BOM**), common test automation base classes, and seamless integration with **GitLab Maven Package Registry**.

---

## 📁 Project Architecture

This repository is structured as a multi-module Maven project:

```
qe-bom-base-java/
├── .gitlab-ci.yml                 # GitLab CI/CD pipeline (test & deploy to package registry)
├── ci_settings.xml                # Maven settings for GitLab Package Registry authentication
├── pom.xml                        # Parent aggregator POM & plugin management
├── qe-bom-java/                   # Centralized BOM (Bill of Materials) module
│   └── pom.xml                    # Defines managed dependency versions (pom packaging)
└── qe-base-java/                  # Core test framework module (jar packaging)
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/adarsha/qe/base/
        │   │   ├── BaseTest.java             # Base test class with ThreadLocal lifecycle hooks
        │   │   ├── context/
        │   │   │   └── TestExecutionContextManager.java # ThreadLocal context manager
        │   │   ├── listener/
        │   │   │   └── TestExecutionListener.java       # Thread-aware TestNG lifecycle logger
        │   │   └── model/
        │   │       └── TestExecutionContext.java        # Thread-safe context (ConcurrentHashMap)
        │   └── resources/
        │       └── logback.xml                # Standard console & rolling file logging configuration
        └── test/
            ├── java/com/adarsha/qe/base/
            │   ├── BaseTestVerificationTest.java        # Verification test suite
            │   └── ParallelExecutionVerificationTest.java # Tests parallel methods & parallel DataProviders
            └── resources/
                └── testng.xml                 # Parallel suite descriptor (parallel="methods", thread-count=5)
```

---

## 📦 Managed Dependencies

The BOM (`qe-bom-java`) centrally manages standard versions for all QE projects:

| Dependency | Group ID | Artifact ID | Managed Version |
| :--- | :--- | :--- | :--- |
| **TestNG** | `org.testng` | `testng` | `7.10.2` |
| **SLF4J API** | `org.slf4j` | `slf4j-api` | `${slf4j.version}` (`2.0.16`) |
| **Logback Classic** | `ch.qos.logback` | `logback-classic` | `${logback.version}` (`1.5.16`) |
| **Project Lombok** | `org.projectlombok` | `lombok` | `1.18.36` (scope: `provided`) |
| **PicoCLI** | `info.picocli` | `picocli` | `${picocli.version}` (`4.7.7`) |
| **QE Base Java** | `com.adarsha.qe` | `qe-base-java` | `${project.version}` |

---

## 🚀 Building & Testing Locally

Compile, process Lombok annotations, and run TestNG tests:

```bash
mvn clean test
```

Package the artifacts (JAR, sources, Javadoc, and POM):

```bash
mvn clean package
```

---

## 🦊 Publishing to GitLab Maven Package Registry

### 1. Automated via GitLab CI/CD (Recommended)

When commits are pushed to the default branch (`main`) or tagged (`v1.0.0`), the `.gitlab-ci.yml` pipeline automatically triggers:

```bash
mvn deploy -s ci_settings.xml -DskipTests
```

GitLab CI provides predefined environment variables (`CI_PROJECT_ID`, `CI_API_V4_URL`, and `CI_JOB_TOKEN`) to authenticate and publish directly to your repository's Package Registry.

### 2. Manual / Local Deployment

`CI_PROJECT_ID` and `CI_API_V4_URL` already default to this project's values in the parent `pom.xml`, so you only need to supply credentials locally.

#### Option A: Using a Personal Access Token (PAT) — Recommended for local
1. In GitLab, go to **User Settings > Access Tokens** and generate a PAT with the `api` scope (or `read_api` + `write_repository`).
2. Export the token — the active `gitlab-maven` server block in `ci_settings.xml` already sends it as a `Private-Token` header, so no further edits are needed:

```bash
export GITLAB_PRIVATE_TOKEN="<your-personal-access-token>"
```

3. Deploy:

```bash
mvn deploy -s ci_settings.xml -DskipTests
```

#### Option B: Using a GitLab Deploy Token
1. In GitLab, navigate to **Settings > Repository > Deploy tokens**.
2. Create a token with `read_package_registry` and `write_package_registry` scopes.
3. Export the credentials:

```bash
export CI_DEPLOY_USER="<your-deploy-token-username>"
export CI_DEPLOY_PASSWORD="<your-deploy-token-password>"
```

4. Comment out the `Private-Token` header on the default `gitlab-maven` server block and uncomment the Deploy Token server block below it in `ci_settings.xml` (both declare the same server `id`, so only one should be active at a time), then run:

```bash
mvn deploy -s ci_settings.xml -DskipTests
```

To deploy against a different GitLab project, override the defaults from `pom.xml` with `-D`:

```bash
mvn deploy -s ci_settings.xml -DskipTests -DCI_PROJECT_ID=<other-project-id> -DCI_API_V4_URL=https://gitlab.com/api/v4
```

### 3. Verifying a Deployment

After a successful `deploy`, confirm the artifacts landed in the registry under the GitLab project's **Packages and registries > Package Registry** page, where `qe-bom-base-java-parent`, `qe-bom-java`, and `qe-base-java` should each appear with the deployed version (JAR, sources, javadoc, and POM where applicable).

---

## 📥 Consuming the BOM in Downstream Projects

Downstream test projects can consume the artifacts in two ways:

### Pattern 1: Import the BOM (Version Management)

Import `qe-bom-java` in `<dependencyManagement>`. Downstream projects can then add any of the managed dependencies without hardcoding versions:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.adarsha.qe</groupId>
            <artifactId>qe-bom-java</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Version managed by qe-bom-java -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Pattern 2: Depend on `qe-base-java` (Starter Library)

To get TestNG, SLF4J, Logback, Lombok, and the pre-configured `BaseTest` execution hooks out of the box:

```xml
<dependencies>
    <dependency>
        <groupId>com.adarsha.qe</groupId>
        <artifactId>qe-base-java</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

And configure the GitLab Package Registry in the downstream project's `pom.xml` or `~/.m2/settings.xml`:

```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.com/api/v4/projects/<PROJECT_ID>/packages/maven</url>
    </repository>
</repositories>
```

---

## ⚡ Parallel Execution & Thread Safety

The framework natively supports **fully parallel test execution** across both:
1. **Parallel Test Methods**: `parallel="methods"`
2. **Parallel DataProviders**: `@DataProvider(parallel = true)`

### How It Works:
- **`TestExecutionContextManager`**: Uses `ThreadLocal<TestExecutionContext>` to bind a separate, isolated context to each executing thread.
- **`ConcurrentHashMap`**: Attributes within `TestExecutionContext` are stored in thread-safe concurrent maps with type-safe accessors (`getAttribute(key, type)`).
- **Auto-Injection & Parameter Capture**: `BaseTest.@BeforeMethod` automatically injects DataProvider arguments into the current thread's context and captures thread ID/name.
- **Leak-Proof Teardown**: `BaseTest.@AfterMethod` and `TestExecutionListener` systematically call `TestExecutionContextManager.removeContext()` in a `finally` block to prevent worker thread pollution across thread pool re-use.

### Example in Test Classes:
```java
@Slf4j
public class SampleParallelTest extends BaseTest {

    @DataProvider(name = "userRoles", parallel = true)
    public Object[][] userRoles() {
        return new Object[][]{
            {"admin", 1},
            {"editor", 2},
            {"viewer", 3}
        };
    }

    @Test(dataProvider = "userRoles")
    public void testConcurrentExecution(String role, int level) {
        // Retrieve thread-isolated context
        TestExecutionContext context = getContext();

        // Safe concurrent access & attributes
        context.setAttribute("activeRole", role);
        Assert.assertEquals(context.getAttribute("activeRole", String.class), role);
        Assert.assertEquals(context.getThreadId(), Thread.currentThread().getId());
    }
}
```

