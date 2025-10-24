package dev.dong4j.zeka.stack.idea.plugin.task;

import com.intellij.openapi.project.Project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TaskCollector 代码优化功能测试
 *
 * @author dong4j
 * @since 1.0.0.0
 */
@DisplayName("TaskCollector 代码优化功能测试")
class TaskCollectorCodeOptimizationTest {
    @Mock
    private Project mockProject;
    private SettingsState settings;
    private TaskCollector taskCollector;

    @BeforeEach
    void setUp() {
        settings = new SettingsState();
        taskCollector = new TaskCollector(mockProject);
    }

    @Test
    @DisplayName("测试代码优化功能 - 删除空行")
    void testOptimizeClassCode_removeEmptyLines() {
        // 使用反射访问私有方法进行测试
        String originalCode = """
            public class TestClass {
            
                private String name;
            
                public String getName() {
                    return name;
                }
            
            }
            """;

        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证空行被删除
        assertThat(optimized).doesNotContain("\n\n");
        assertThat(optimized).contains("public class TestClass {");
        assertThat(optimized).contains("private String name;");
        assertThat(optimized).contains("public String getName() {");
    }

    @Test
    @DisplayName("测试代码优化功能 - 删除单行注释")
    void testOptimizeClassCode_removeSingleLineComments() {
        String originalCode = """
            public class TestClass {
                // 这是一个测试类
                private String name;
            
                // 获取名称的方法
                public String getName() {
                    return name; // 返回名称
                }
            }
            """;

        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证单行注释被删除
        assertThat(optimized).doesNotContain("// 这是一个测试类");
        assertThat(optimized).doesNotContain("// 获取名称的方法");
        assertThat(optimized).doesNotContain("// 返回名称");

        // 验证代码保留
        assertThat(optimized).contains("public class TestClass {");
        assertThat(optimized).contains("private String name;");
        assertThat(optimized).contains("public String getName() {");
    }

    @Test
    @DisplayName("测试代码优化功能 - 保留 JavaDoc 注释")
    void testOptimizeClassCode_preserveJavaDocComments() {
        String originalCode = """
            /**
             * 测试类
             * <p>这是一个用于测试的类
             */
            public class TestClass {
                /**
                 * 获取名称
                 * @return 名称
                 */
                public String getName() {
                    return name;
                }
            }
            """;

        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证 JavaDoc 注释被保留
        assertThat(optimized).contains("/**");
        assertThat(optimized).contains("测试类");
        assertThat(optimized).contains("获取名称");
        assertThat(optimized).contains("@return 名称");

        // 验证代码保留
        assertThat(optimized).contains("public class TestClass {");
        assertThat(optimized).contains("public String getName() {");
    }

    @Test
    @DisplayName("测试代码优化功能 - 行数截取")
    void testOptimizeClassCode_lineTruncation() {
        // 设置较小的最大行数进行测试
        settings.maxClassCodeLines = 5;

        String originalCode = """
            public class TestClass {
                private String field1;
                private String field2;
                private String field3;
                private String field4;
                private String field5;
                private String field6;
            }
            """;

        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证代码被截取
        assertThat(optimized).contains("// ... (代码已截取，超过 5 行)");
        assertThat(optimized).contains("public class TestClass {");
        assertThat(optimized).contains("private String field1;");

        // 验证后面的字段被截取
        assertThat(optimized).doesNotContain("private String field6;");
    }

    @Test
    @DisplayName("测试代码优化功能 - 禁用优化")
    void testOptimizeClassCode_disabled() {
        settings.optimizeClassCode = false;

        String originalCode = """
            public class TestClass {
            
                // 这是一个注释
                private String name;
            
            }
            """;

        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证代码没有被优化（应该返回原始代码）
        assertThat(optimized).isEqualTo(originalCode);
    }

    @Test
    @DisplayName("测试代码优化功能 - 空代码处理")
    void testOptimizeClassCode_emptyCode() {
        String originalCode = "";
        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证空代码返回原始值
        assertThat(optimized).isEqualTo(originalCode);
    }

    @Test
    @DisplayName("测试代码优化功能 - null 处理")
    void testOptimizeClassCode_nullCode() {
        String originalCode = null;
        String optimized = invokeOptimizeClassCode(originalCode);

        // 验证 null 代码返回原始值
        assertThat(optimized).isEqualTo(originalCode);
    }

    /**
     * 使用反射调用私有方法 optimizeClassCode
     */
    private String invokeOptimizeClassCode(String originalCode) {
        try {
            java.lang.reflect.Method method = TaskCollector.class.getDeclaredMethod("optimizeClassCode", String.class);
            method.setAccessible(true);
            return (String) method.invoke(taskCollector, originalCode);
        } catch (Exception e) {
            throw new RuntimeException("无法调用 optimizeClassCode 方法", e);
        }
    }
}
