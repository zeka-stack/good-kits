package dev.dong4j.zeka.stack.idea.plugin;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import dev.dong4j.zeka.stack.idea.plugin.ai.AIProviderHttpIntegrationTest;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceFactoryTest;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceProviderTest;
import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsStateTest;
import dev.dong4j.zeka.stack.idea.plugin.task.DocumentationTaskTest;
import dev.dong4j.zeka.stack.idea.plugin.task.TaskCollectorTest;
import dev.dong4j.zeka.stack.idea.plugin.util.NotificationUtilTest;
import dev.dong4j.zeka.stack.idea.plugin.util.PsiElementLocatorTest;

/**
 * 测试套件 - 运行所有测试
 * <p>
 * 包括单元测试和集成测试
 * <p>
 * 使用 JUnit 5 Platform Suite 来组织和运行所有测试类
 */
@Suite
@SuiteDisplayName("IntelliJ AI Javadoc 插件 - 完整测试套件")
@SelectClasses( {
    // Settings 模块
    SettingsStateTest.class,

    // Task 模块
    DocumentationTaskTest.class,
    TaskCollectorTest.class,
    // TaskExecutorIntegrationTest.class, // 需要 IntelliJ Platform 环境，单独运行

    // AI 模块
    AIServiceFactoryTest.class,
    AIServiceProviderTest.class,
    AIProviderHttpIntegrationTest.class, // HTTP 集成测试

    // Util 模块
    NotificationUtilTest.class,
    PsiElementLocatorTest.class
})
public class AllTests {
    // 测试套件类，不需要实现任何方法
    // JUnit 5 会自动执行 @SelectClasses 中指定的所有测试类
    //
    // 注意：TaskExecutorIntegrationTest 需要完整的 IntelliJ Platform 环境，
    // 建议在 IDE 中单独运行，或使用 IntelliJ Gradle 插件的 test 任务
}

