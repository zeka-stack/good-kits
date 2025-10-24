package dev.dong4j.zeka.stack.idea.plugin.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.dong4j.zeka.stack.idea.plugin.settings.ui.JavaDocSettingsPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prompt 配置测试
 *
 * <p>测试 Prompt 模板的保存、加载和重置功能。
 * 验证配置的持久化和 UI 交互的正确性。
 *
 * @author dong4j
 * @version 1.0.0
 * @since 1.0.0
 */
public class PromptConfigTest {

    private JavaDocSettingsPanel settingsPanel;
    private SettingsState originalSettings;

    @BeforeEach
    public void setUp() {
        // 保存原始设置
        originalSettings = SettingsState.getInstance();

        // 创建设置面板
        settingsPanel = new JavaDocSettingsPanel();

        // 加载当前设置到面板
        settingsPanel.loadSettings(originalSettings);
    }

    /**
     * 测试 Prompt 模板的修改检测
     */
    @Test
    public void testPromptModificationDetection() {
        // 获取当前设置
        SettingsState currentSettings = settingsPanel.getSettings();

        // 修改系统提示词
        String originalSystemPrompt = currentSettings.systemPromptTemplate;
        String modifiedSystemPrompt = "修改后的系统提示词";

        // 模拟用户修改
        settingsPanel.systemPromptTextArea.setText(modifiedSystemPrompt);

        // 获取修改后的设置
        SettingsState modifiedSettings = settingsPanel.getSettings();

        // 验证修改被检测到
        assertNotEquals(originalSystemPrompt, modifiedSettings.systemPromptTemplate);
        assertEquals(modifiedSystemPrompt, modifiedSettings.systemPromptTemplate);
    }

    /**
     * 测试 Prompt 模板的重置功能
     */
    @Test
    public void testPromptReset() {
        // 修改系统提示词
        String customPrompt = "自定义的系统提示词";
        settingsPanel.systemPromptTextArea.setText(customPrompt);

        // 验证修改生效
        assertEquals(customPrompt, settingsPanel.systemPromptTextArea.getText());

        // 重置为默认值
        settingsPanel.resetPromptToDefault("system", settingsPanel.systemPromptTextArea);

        // 验证重置成功
        String defaultPrompt = SettingsState.getDefaultSystemPromptTemplate();
        assertEquals(defaultPrompt, settingsPanel.systemPromptTextArea.getText());
        assertNotEquals(customPrompt, settingsPanel.systemPromptTextArea.getText());
    }

    /**
     * 测试所有 Prompt 模板的重置功能
     */
    @Test
    public void testAllPromptReset() {
        // 修改所有提示词
        String customSystemPrompt = "自定义系统提示词";
        String customClassPrompt = "自定义类提示词";
        String customMethodPrompt = "自定义方法提示词";
        String customFieldPrompt = "自定义字段提示词";
        String customTestPrompt = "自定义测试提示词";

        settingsPanel.systemPromptTextArea.setText(customSystemPrompt);
        settingsPanel.classPromptTextArea.setText(customClassPrompt);
        settingsPanel.methodPromptTextArea.setText(customMethodPrompt);
        settingsPanel.fieldPromptTextArea.setText(customFieldPrompt);
        settingsPanel.testPromptTextArea.setText(customTestPrompt);

        // 重置所有提示词
        settingsPanel.resetPromptToDefault("system", settingsPanel.systemPromptTextArea);
        settingsPanel.resetPromptToDefault("class", settingsPanel.classPromptTextArea);
        settingsPanel.resetPromptToDefault("method", settingsPanel.methodPromptTextArea);
        settingsPanel.resetPromptToDefault("field", settingsPanel.fieldPromptTextArea);
        settingsPanel.resetPromptToDefault("test", settingsPanel.testPromptTextArea);

        // 验证所有提示词都重置为默认值
        assertEquals(SettingsState.getDefaultSystemPromptTemplate(), settingsPanel.systemPromptTextArea.getText());
        assertEquals(SettingsState.getDefaultClassPromptTemplate(), settingsPanel.classPromptTextArea.getText());
        assertEquals(SettingsState.getDefaultMethodPromptTemplate(), settingsPanel.methodPromptTextArea.getText());
        assertEquals(SettingsState.getDefaultFieldPromptTemplate(), settingsPanel.fieldPromptTextArea.getText());
        assertEquals(SettingsState.getDefaultTestPromptTemplate(), settingsPanel.testPromptTextArea.getText());
    }

    /**
     * 测试 Prompt 模板的保存和加载
     */
    @Test
    public void testPromptSaveAndLoad() {
        // 修改提示词
        String customSystemPrompt = "测试系统提示词";
        String customClassPrompt = "测试类提示词";

        settingsPanel.systemPromptTextArea.setText(customSystemPrompt);
        settingsPanel.classPromptTextArea.setText(customClassPrompt);

        // 获取修改后的设置
        SettingsState modifiedSettings = settingsPanel.getSettings();

        // 验证设置被正确保存
        assertEquals(customSystemPrompt, modifiedSettings.systemPromptTemplate);
        assertEquals(customClassPrompt, modifiedSettings.classPromptTemplate);

        // 创建新的面板并加载设置
        JavaDocSettingsPanel newPanel = new JavaDocSettingsPanel();
        newPanel.loadSettings(modifiedSettings);

        // 验证设置被正确加载
        assertEquals(customSystemPrompt, newPanel.systemPromptTextArea.getText());
        assertEquals(customClassPrompt, newPanel.classPromptTextArea.getText());
    }

    /**
     * 测试空提示词的处理
     */
    @Test
    public void testEmptyPromptHandling() {
        // 设置空提示词
        settingsPanel.systemPromptTextArea.setText("");
        settingsPanel.classPromptTextArea.setText("   "); // 只有空格

        // 获取设置
        SettingsState settings = settingsPanel.getSettings();

        // 验证空提示词被正确处理（trim 后为空）
        assertEquals("", settings.systemPromptTemplate);
        assertEquals("", settings.classPromptTemplate);
    }

    /**
     * 测试默认提示词模板的有效性
     */
    @Test
    public void testDefaultPromptTemplates() {
        // 验证默认提示词不为空
        assertNotNull(SettingsState.getDefaultSystemPromptTemplate());
        assertNotNull(SettingsState.getDefaultClassPromptTemplate());
        assertNotNull(SettingsState.getDefaultMethodPromptTemplate());
        assertNotNull(SettingsState.getDefaultFieldPromptTemplate());
        assertNotNull(SettingsState.getDefaultTestPromptTemplate());

        // 验证默认提示词不为空字符串
        assertFalse(SettingsState.getDefaultSystemPromptTemplate().trim().isEmpty());
        assertFalse(SettingsState.getDefaultClassPromptTemplate().trim().isEmpty());
        assertFalse(SettingsState.getDefaultMethodPromptTemplate().trim().isEmpty());
        assertFalse(SettingsState.getDefaultFieldPromptTemplate().trim().isEmpty());
        assertFalse(SettingsState.getDefaultTestPromptTemplate().trim().isEmpty());
    }
}
