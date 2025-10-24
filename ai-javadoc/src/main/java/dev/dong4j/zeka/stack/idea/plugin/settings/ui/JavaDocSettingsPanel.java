package dev.dong4j.zeka.stack.idea.plugin.settings.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceFactory;
import dev.dong4j.zeka.stack.idea.plugin.ai.AIServiceProvider;
import dev.dong4j.zeka.stack.idea.plugin.ai.ValidationResult;
import dev.dong4j.zeka.stack.idea.plugin.settings.SettingsState;
import dev.dong4j.zeka.stack.idea.plugin.util.JavaDocBundle;

/**
 * JavaDoc 设置面板 UI
 *
 * <p>构建设置界面的所有 UI 组件。
 *
 * @author dong4j
 * @version 1.0.0
 */
public class JavaDocSettingsPanel {

    private JPanel mainPanel;

    // AI 提供商配置
    private ComboBox<String> providerComboBox;
    private ComboBox<String> modelComboBox;
    private JBTextField baseUrlField;
    private JBPasswordField apiKeyField;
    private JButton testConnectionButton;

    // 验证状态标记
    private boolean configurationVerified = false;

    // 功能配置
    private JBCheckBox generateForClassCheckBox;
    private JBCheckBox generateForMethodCheckBox;
    private JBCheckBox generateForFieldCheckBox;
    private JBCheckBox skipExistingCheckBox;
    private JBCheckBox optimizeClassCodeCheckBox;
    private JSpinner maxClassCodeLinesSpinner;

    // 语言支持
    private JBCheckBox javaCheckBox;
    private JBCheckBox kotlinCheckBox;

    // 高级配置
    private JSpinner maxRetriesSpinner;
    private JSpinner timeoutSpinner;
    private JSpinner temperatureSpinner;
    private JSpinner maxTokensSpinner;
    private JSpinner concurrencySpinner;
    private JBCheckBox verboseLoggingCheckBox;

    // Prompt 配置
    private JTextArea classPromptTextArea;
    private JTextArea methodPromptTextArea;
    private JTextArea fieldPromptTextArea;
    private JTextArea testPromptTextArea;

    public JavaDocSettingsPanel() {
        createUI();
        setupListeners();
    }

    private void createUI() {
        // AI 提供商配置
        providerComboBox = new ComboBox<>(new String[] {"qianwen", "ollama"});

        // 创建可编辑的模型下拉框，用户可以输入任何模型名称
        modelComboBox = new ComboBox<>();
        modelComboBox.setEditable(true);  // 允许用户输入自定义模型名称
        updateModelList();

        baseUrlField = new JBTextField();
        baseUrlField.setToolTipText(JavaDocBundle.message("settings.base.url.tooltip"));

        apiKeyField = new JBPasswordField();
        apiKeyField.setToolTipText(JavaDocBundle.message("settings.api.key.tooltip"));

        testConnectionButton = new JButton(JavaDocBundle.message("settings.test.connection"));
        testConnectionButton.addActionListener(e -> testConnection());

        // 功能配置
        generateForClassCheckBox = new JBCheckBox(JavaDocBundle.message("settings.generate.for.class"));
        generateForMethodCheckBox = new JBCheckBox(JavaDocBundle.message("settings.generate.for.method"));
        generateForFieldCheckBox = new JBCheckBox(JavaDocBundle.message("settings.generate.for.field"));
        skipExistingCheckBox = new JBCheckBox(JavaDocBundle.message("settings.skip.existing"));
        optimizeClassCodeCheckBox = new JBCheckBox(JavaDocBundle.message("settings.optimize.class.code"));
        maxClassCodeLinesSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));

        // 语言支持
        javaCheckBox = new JBCheckBox(JavaDocBundle.message("settings.language.java"));
        javaCheckBox.setEnabled(true);
        kotlinCheckBox = new JBCheckBox(JavaDocBundle.message("settings.language.kotlin"));
        kotlinCheckBox.setEnabled(false);

        // 高级配置
        maxRetriesSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));
        timeoutSpinner = new JSpinner(new SpinnerNumberModel(30000, 1000, 300000, 1000));
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 2.0, 0.1));
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 10000, 100));
        concurrencySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        verboseLoggingCheckBox = new JBCheckBox(JavaDocBundle.message("settings.verbose.logging"));

        // Prompt 配置
        classPromptTextArea = new JTextArea(10, 50);
        classPromptTextArea.setLineWrap(true);
        classPromptTextArea.setWrapStyleWord(true);
        classPromptTextArea.setToolTipText(JavaDocBundle.message("settings.prompt.class.tooltip"));

        methodPromptTextArea = new JTextArea(10, 50);
        methodPromptTextArea.setLineWrap(true);
        methodPromptTextArea.setWrapStyleWord(true);
        methodPromptTextArea.setToolTipText(JavaDocBundle.message("settings.prompt.method.tooltip"));

        fieldPromptTextArea = new JTextArea(10, 50);
        fieldPromptTextArea.setLineWrap(true);
        fieldPromptTextArea.setWrapStyleWord(true);
        fieldPromptTextArea.setToolTipText(JavaDocBundle.message("settings.prompt.field.tooltip"));

        testPromptTextArea = new JTextArea(10, 50);
        testPromptTextArea.setLineWrap(true);
        testPromptTextArea.setWrapStyleWord(true);
        testPromptTextArea.setToolTipText(JavaDocBundle.message("settings.prompt.test.tooltip"));

        // 构建主面板
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.provider.label")), providerComboBox)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.model.label")), createModelPanel())
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.base.url.label")), baseUrlField)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.api.key.label")), createApiKeyPanel())
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.generation.options")))
            .addComponent(generateForClassCheckBox)
            .addComponent(generateForMethodCheckBox)
            .addComponent(generateForFieldCheckBox)
            .addComponent(skipExistingCheckBox)
            .addComponent(optimizeClassCodeCheckBox)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.class.code.lines")), maxClassCodeLinesSpinner)
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.language.support")))
            .addComponent(javaCheckBox)
            .addComponent(kotlinCheckBox)
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.advanced.config")))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.retries")), maxRetriesSpinner)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.timeout")), timeoutSpinner)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.temperature")), temperatureSpinner)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.tokens")), maxTokensSpinner)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.concurrency")), concurrencySpinner)
            .addComponent(verboseLoggingCheckBox)
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.prompt.templates")))
            .addComponent(new JBLabel(JavaDocBundle.message("settings.prompt.hint")))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.prompt.class")), createScrollPane(classPromptTextArea))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.prompt.method")), createScrollPane(methodPromptTextArea))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.prompt.field")), createScrollPane(fieldPromptTextArea))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.prompt.test")), createScrollPane(testPromptTextArea))

            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();

        mainPanel.setBorder(JBUI.Borders.empty(10));
    }

    private JPanel createModelPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(modelComboBox, BorderLayout.CENTER);

        JBLabel hintLabel = new JBLabel(JavaDocBundle.message("settings.model.hint"));
        hintLabel.setFont(hintLabel.getFont().deriveFont(hintLabel.getFont().getSize() - 2.0f));
        hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(hintLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createApiKeyPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(apiKeyField, BorderLayout.CENTER);
        panel.add(testConnectionButton, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createScrollPane(JTextArea textArea) {
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private void setupListeners() {
        // 提供商变更时更新模型列表和默认值
        providerComboBox.addActionListener(e -> {
            updateModelList();
            updateDefaultValues();
            updateApiKeyVisibility();
            // 关键配置修改，清除验证状态
            markConfigurationAsUnverified();
        });

        // 监听关键配置变更，清除验证状态
        baseUrlField.addActionListener(e -> markConfigurationAsUnverified());
        apiKeyField.addActionListener(e -> markConfigurationAsUnverified());
        // 模型选择变更时清除验证状态
        modelComboBox.addActionListener(e -> markConfigurationAsUnverified());

        // 监听代码优化配置变更
        optimizeClassCodeCheckBox.addActionListener(e -> {
            // 当启用/禁用代码优化时，可以更新最大行数输入框的可用性
            maxClassCodeLinesSpinner.setEnabled(optimizeClassCodeCheckBox.isSelected());
        });
    }

    private void updateModelList() {
        String providerId = (String) providerComboBox.getSelectedItem();
        if (providerId == null) {
            return;
        }

        try {
            SettingsState tempSettings = new SettingsState();
            tempSettings.aiProvider = providerId;
            // 临时允许创建未验证的提供商用于获取模型列表
            tempSettings.configurationVerified = true;
            AIServiceProvider provider = AIServiceFactory.createProvider(tempSettings);

            if (provider == null) {
                return;
            }

            // 保存当前输入的模型名称
            String currentModel = (String) modelComboBox.getSelectedItem();

            // 清空并添加推荐模型列表（仅作为参考）
            modelComboBox.removeAllItems();
            for (String model : provider.getSupportedModels()) {
                modelComboBox.addItem(model);
            }

            // 恢复用户之前输入的值，如果为空则使用默认值
            if (currentModel != null && !currentModel.trim().isEmpty()) {
                modelComboBox.setSelectedItem(currentModel);
            } else {
                modelComboBox.setSelectedItem(provider.getDefaultModel());
            }

            // 设置提示文本
            if (modelComboBox.getEditor() != null && modelComboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
                textField.setToolTipText(JavaDocBundle.message("settings.model.hint"));
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }

    private void updateDefaultValues() {
        String providerId = (String) providerComboBox.getSelectedItem();
        if (providerId == null) {
            return;
        }

        try {
            SettingsState tempSettings = new SettingsState();
            tempSettings.aiProvider = providerId;
            // 临时允许创建未验证的提供商用于获取默认值
            tempSettings.configurationVerified = true;
            AIServiceProvider provider = AIServiceFactory.createProvider(tempSettings);

            if (provider == null) {
                return;
            }

            baseUrlField.setText(provider.getDefaultBaseUrl());
            modelComboBox.setSelectedItem(provider.getDefaultModel());
        } catch (Exception e) {
            // 忽略错误
        }
    }

    private void updateApiKeyVisibility() {
        String providerId = (String) providerComboBox.getSelectedItem();
        boolean requiresKey = !"ollama".equals(providerId);
        apiKeyField.setEnabled(requiresKey);
        testConnectionButton.setEnabled(true);
    }

    private void testConnection() {
        SettingsState testSettings = getSettings();
        // 临时允许创建未验证的提供商用于测试
        testSettings.configurationVerified = true;
        AIServiceProvider provider = AIServiceFactory.createProvider(testSettings);

        // 检查提供商创建是否成功
        if (provider == null) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "创建 AI 服务提供商失败，请检查配置是否正确（提供商、模型、Base URL 等）",
                JavaDocBundle.message("settings.error.title"),
                JOptionPane.ERROR_MESSAGE
                                         );
            return;
        }

        testConnectionButton.setEnabled(false);
        testConnectionButton.setText(JavaDocBundle.message("settings.test.connection.testing"));

        // 在后台线程测试
        new Thread(() -> {
            try {
                ValidationResult result = provider.validateConfiguration();

                SwingUtilities.invokeLater(() -> {
                    if (result.isSuccess()) {
                        // 测试成功，标记配置为已验证
                        markConfigurationAsVerified();
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            result.getMessage(),
                            JavaDocBundle.message("settings.test.result.title"),
                            JOptionPane.INFORMATION_MESSAGE
                                                     );
                    } else {
                        // 测试失败，清除验证状态
                        markConfigurationAsUnverified();

                        // 构建详细的错误消息
                        String errorMessage = result.getMessage();
                        String errorDetails = result.getErrorDetails();
                        if (errorDetails != null && !errorDetails.isEmpty()) {
                            errorMessage = errorMessage + "\n\n详细信息:\n" + errorDetails;
                        }

                        JOptionPane.showMessageDialog(
                            mainPanel,
                            errorMessage,
                            JavaDocBundle.message("settings.test.result.title"),
                            JOptionPane.ERROR_MESSAGE
                                                     );
                    }
                    testConnectionButton.setText(JavaDocBundle.message("settings.test.connection"));
                    testConnectionButton.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    // 测试异常，清除验证状态
                    markConfigurationAsUnverified();
                    String errorMessage = JavaDocBundle.message("settings.test.connection.error", e.getMessage());
                    JOptionPane.showMessageDialog(
                        mainPanel,
                        errorMessage,
                        JavaDocBundle.message("settings.test.result.title"),
                        JOptionPane.ERROR_MESSAGE
                                                 );
                    testConnectionButton.setText(JavaDocBundle.message("settings.test.connection"));
                    testConnectionButton.setEnabled(true);
                });
            }
        }).start();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    /**
     * 标记配置为已验证
     */
    private void markConfigurationAsVerified() {
        this.configurationVerified = true;
    }

    /**
     * 标记配置为未验证
     */
    private void markConfigurationAsUnverified() {
        this.configurationVerified = false;
    }

    /**
     * 从 UI 获取配置
     */
    @NotNull
    public SettingsState getSettings() {
        SettingsState settings = new SettingsState();

        // AI 提供商配置
        settings.aiProvider = (String) providerComboBox.getSelectedItem();
        // 获取用户输入的模型名称（可能是从列表选择的，也可能是手动输入的）
        Object selectedModel = modelComboBox.getEditor().getItem();
        settings.modelName = selectedModel != null ? selectedModel.toString().trim() : "";
        settings.baseUrl = baseUrlField.getText().trim();
        settings.apiKey = new String(apiKeyField.getPassword()).trim();

        // 设置验证状态
        settings.configurationVerified = this.configurationVerified;

        // 功能配置
        settings.generateForClass = generateForClassCheckBox.isSelected();
        settings.generateForMethod = generateForMethodCheckBox.isSelected();
        settings.generateForField = generateForFieldCheckBox.isSelected();
        settings.skipExisting = skipExistingCheckBox.isSelected();
        settings.optimizeClassCode = optimizeClassCodeCheckBox.isSelected();
        settings.maxClassCodeLines = (Integer) maxClassCodeLinesSpinner.getValue();

        // 语言支持
        settings.supportedLanguages = new HashSet<>();
        if (javaCheckBox.isSelected()) {
            settings.supportedLanguages.add("java");
        }
        if (kotlinCheckBox.isSelected()) {
            settings.supportedLanguages.add("kotlin");
        }

        // 高级配置
        settings.maxRetries = (Integer) maxRetriesSpinner.getValue();
        settings.timeout = (Integer) timeoutSpinner.getValue();
        settings.temperature = (Double) temperatureSpinner.getValue();
        settings.maxTokens = (Integer) maxTokensSpinner.getValue();
        settings.concurrency = (Integer) concurrencySpinner.getValue();
        settings.verboseLogging = verboseLoggingCheckBox.isSelected();

        // Prompt 配置
        settings.classPromptTemplate = classPromptTextArea.getText().trim();
        settings.methodPromptTemplate = methodPromptTextArea.getText().trim();
        settings.fieldPromptTemplate = fieldPromptTextArea.getText().trim();
        settings.testPromptTemplate = testPromptTextArea.getText().trim();

        return settings;
    }

    /**
     * 加载配置到 UI
     */
    @SuppressWarnings("DuplicatedCode")
    public void loadSettings(@NotNull SettingsState settings) {
        // AI 提供商配置
        providerComboBox.setSelectedItem(settings.aiProvider);
        updateModelList();
        modelComboBox.setSelectedItem(settings.modelName);
        baseUrlField.setText(settings.baseUrl);
        apiKeyField.setText(settings.apiKey);

        // 加载验证状态
        this.configurationVerified = settings.configurationVerified;

        // 功能配置
        generateForClassCheckBox.setSelected(settings.generateForClass);
        generateForMethodCheckBox.setSelected(settings.generateForMethod);
        generateForFieldCheckBox.setSelected(settings.generateForField);
        skipExistingCheckBox.setSelected(settings.skipExisting);
        optimizeClassCodeCheckBox.setSelected(settings.optimizeClassCode);
        maxClassCodeLinesSpinner.setValue(settings.maxClassCodeLines);

        // 根据代码优化设置更新最大行数输入框的可用性
        maxClassCodeLinesSpinner.setEnabled(settings.optimizeClassCode);

        // 语言支持
        javaCheckBox.setSelected(settings.supportedLanguages.contains("java"));
        kotlinCheckBox.setSelected(settings.supportedLanguages.contains("kotlin"));

        // 高级配置
        maxRetriesSpinner.setValue(settings.maxRetries);
        timeoutSpinner.setValue(settings.timeout);
        temperatureSpinner.setValue(settings.temperature);
        maxTokensSpinner.setValue(settings.maxTokens);
        concurrencySpinner.setValue(settings.concurrency);
        verboseLoggingCheckBox.setSelected(settings.verboseLogging);

        // Prompt 配置
        classPromptTextArea.setText(settings.classPromptTemplate);
        methodPromptTextArea.setText(settings.methodPromptTemplate);
        fieldPromptTextArea.setText(settings.fieldPromptTemplate);
        testPromptTextArea.setText(settings.testPromptTemplate);

        updateApiKeyVisibility();
    }
}

