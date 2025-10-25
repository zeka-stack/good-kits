package dev.dong4j.zeka.stack.idea.plugin.settings.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.List;

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

import dev.dong4j.zeka.stack.idea.plugin.ai.AIProviderType;
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
@SuppressWarnings( {"D", "DuplicatedCode"})
public class JavaDocSettingsPanel {

    private JPanel mainPanel;

    // AI 提供商配置
    private ComboBox<String> providerComboBox;
    private ComboBox<String> modelComboBox;
    private JBTextField baseUrlField;
    private JBPasswordField apiKeyField;
    private JButton testConnectionButton;
    private JButton refreshModelsButton;

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
    private JSpinner topPSpinner;
    private JSpinner topKSpinner;
    private JSpinner presencePenaltySpinner;
    private JBCheckBox verboseLoggingCheckBox;
    private JBCheckBox performanceModeCheckBox;

    public JTextArea systemPromptTextArea;
    public JTextArea classPromptTextArea;
    public JTextArea methodPromptTextArea;
    public JTextArea fieldPromptTextArea;
    public JTextArea testPromptTextArea;

    public JavaDocSettingsPanel() {
        createUI();
        setupListeners();
    }

    private void createUI() {
        // AI 提供商配置
        providerComboBox = new ComboBox<>(AIProviderType.getAllDisplayNames().toArray(new String[0]));

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

        refreshModelsButton = new JButton(JavaDocBundle.message("settings.refresh.models"));
        refreshModelsButton.addActionListener(e -> refreshAvailableModels());

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
        topPSpinner = new JSpinner(new SpinnerNumberModel(0.9, 0.0, 1.0, 0.1));
        topKSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
        presencePenaltySpinner = new JSpinner(new SpinnerNumberModel(0.1, -2.0, 2.0, 0.1));
        verboseLoggingCheckBox = new JBCheckBox(JavaDocBundle.message("settings.verbose.logging"));
        performanceModeCheckBox = new JBCheckBox(JavaDocBundle.message("settings.performance.mode"));

        // Prompt 配置 - 创建文本区域（将在 Tab 页中使用）
        systemPromptTextArea = new JTextArea(10, 50);
        classPromptTextArea = new JTextArea(10, 50);
        methodPromptTextArea = new JTextArea(10, 50);
        fieldPromptTextArea = new JTextArea(10, 50);
        testPromptTextArea = new JTextArea(10, 50);

        // 构建主面板
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.provider.label")), providerComboBox)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.base.url.label")), baseUrlField)
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.api.key.label")), createApiKeyPanel())
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.model.label")), createModelPanel())
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.generation.options")))
            .addComponent(createCheckBoxWithHint(generateForClassCheckBox, "settings.generate.for.class.hint"))
            .addComponent(createCheckBoxWithHint(generateForMethodCheckBox, "settings.generate.for.method.hint"))
            .addComponent(createCheckBoxWithHint(generateForFieldCheckBox, "settings.generate.for.field.hint"))
            .addComponent(createCheckBoxWithHint(skipExistingCheckBox, "settings.skip.existing.hint"))
            .addComponent(createCheckBoxWithHint(optimizeClassCodeCheckBox, "settings.optimize.class.code.hint"))

            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.class.code.lines")), maxClassCodeLinesSpinner)
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.language.support")))
            .addComponent(javaCheckBox)
            .addComponent(kotlinCheckBox)
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.model.config")))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.tokens")),
                                 createAdvancedConfigPanel(maxTokensSpinner,
                                                           "settings.max.tokens.hint"))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.temperature")),
                                 createAdvancedConfigPanel(temperatureSpinner
                                     , "settings.temperature.hint"))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.top.p")),
                                 createAdvancedConfigPanel(topPSpinner,
                                                           "settings.top.p.hint"))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.top.k")),
                                 createAdvancedConfigPanel(topKSpinner,
                                                           "settings.top.k.hint"))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.presence.penalty")),
                                 createAdvancedConfigPanel(presencePenaltySpinner,
                                                           "settings.presence.penalty.hint"))
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.advanced.config")))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.max.retries")),
                                 createAdvancedConfigPanel(maxRetriesSpinner,
                                                           "settings.max.retries.hint"))
            .addLabeledComponent(new JBLabel(JavaDocBundle.message("settings.timeout")),
                                 createAdvancedConfigPanel(timeoutSpinner,
                                                           "settings.timeout.hint"))
            .addComponent(verboseLoggingCheckBox)
            .addComponent(createCheckBoxWithHint(performanceModeCheckBox, "settings.performance.mode.hint"))
            .addSeparator(10)

            .addComponent(new JBLabel(JavaDocBundle.message("settings.prompt.templates")))
            .addComponent(new JBLabel(JavaDocBundle.message("settings.prompt.hint")))
            .addComponent(createPromptTabbedPane())

            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();

        mainPanel.setBorder(JBUI.Borders.empty(10));
    }

    private JPanel createModelPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(modelComboBox, BorderLayout.CENTER);

        // 创建右侧按钮面板
        JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
        rightPanel.add(refreshModelsButton, BorderLayout.WEST);
        rightPanel.add(testConnectionButton, BorderLayout.CENTER);
        
        JBLabel hintLabel = new JBLabel(JavaDocBundle.message("settings.model.hint"));
        hintLabel.setFont(hintLabel.getFont().deriveFont(hintLabel.getFont().getSize() - 2.0f));
        hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        rightPanel.add(hintLabel, BorderLayout.EAST);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createApiKeyPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(apiKeyField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdvancedConfigPanel(JSpinner spinner, String hintKey) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));

        // 固定输入框宽度
        spinner.setPreferredSize(new Dimension(120, spinner.getPreferredSize().height));
        panel.add(spinner, BorderLayout.WEST);

        // 提示文本放在右侧，但限制宽度
        JBLabel hintLabel = new JBLabel(JavaDocBundle.message(hintKey));
        hintLabel.setFont(hintLabel.getFont().deriveFont(hintLabel.getFont().getSize() - 2.0f));
        hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        hintLabel.setPreferredSize(new Dimension(300, hintLabel.getPreferredSize().height));
        panel.add(hintLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCheckBoxWithHint(JBCheckBox checkBox, String hintKey) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));

        // 复选框放在左侧
        panel.add(checkBox, BorderLayout.WEST);

        // 提示文本放在右侧
        JBLabel hintLabel = new JBLabel(JavaDocBundle.message(hintKey));
        hintLabel.setFont(hintLabel.getFont().deriveFont(hintLabel.getFont().getSize() - 2.0f));
        hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        hintLabel.setPreferredSize(new Dimension(400, hintLabel.getPreferredSize().height));
        panel.add(hintLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JBTabbedPane createPromptTabbedPane() {
        // Prompt 配置 - Tab 页
        JBTabbedPane promptTabbedPane = new JBTabbedPane();
        promptTabbedPane.setPreferredSize(new Dimension(600, 200));

        // 创建各个 Tab 页
        promptTabbedPane.addTab(JavaDocBundle.message("settings.prompt.tab.system"), createPromptTab(systemPromptTextArea, "system"));
        promptTabbedPane.addTab(JavaDocBundle.message("settings.prompt.tab.class"), createPromptTab(classPromptTextArea, "class"));
        promptTabbedPane.addTab(JavaDocBundle.message("settings.prompt.tab.method"), createPromptTab(methodPromptTextArea, "method"));
        promptTabbedPane.addTab(JavaDocBundle.message("settings.prompt.tab.field"), createPromptTab(fieldPromptTextArea, "field"));
        promptTabbedPane.addTab(JavaDocBundle.message("settings.prompt.tab.test"), createPromptTab(testPromptTextArea, "test"));

        return promptTabbedPane;
    }

    private JPanel createPromptTab(JTextArea textArea, String promptType) {
        JPanel tabPanel = new JPanel(new BorderLayout());

        // 创建文本区域
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setToolTipText(JavaDocBundle.message("settings.prompt." + promptType + ".tooltip"));

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tabPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建重置按钮
        JButton resetButton = new JButton(JavaDocBundle.message("settings.prompt.reset"));
        resetButton.addActionListener(e -> resetPromptToDefault(promptType, textArea));
        tabPanel.add(resetButton, BorderLayout.SOUTH);

        return tabPanel;
    }

    public void resetPromptToDefault(String promptType, JTextArea textArea) {
        String defaultTemplate = switch (promptType) {
            case "system" -> SettingsState.getDefaultSystemPromptTemplate();
            case "class" -> SettingsState.getDefaultClassPromptTemplate();
            case "method" -> SettingsState.getDefaultMethodPromptTemplate();
            case "field" -> SettingsState.getDefaultFieldPromptTemplate();
            case "test" -> SettingsState.getDefaultTestPromptTemplate();
            default -> "";
        };
        textArea.setText(defaultTemplate);
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

        // Base URL 变更时清除验证状态
        baseUrlField.addActionListener(e -> markConfigurationAsUnverified());

        // API Key 变更时清除验证状态
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
        String displayName = (String) providerComboBox.getSelectedItem();
        if (displayName == null) {
            return;
        }

        // 将显示名称转换为提供商标识符
        String providerId = AIProviderType.getProviderIdByDisplayName(displayName);
        if (providerId == null) {
            return;
        }

        AIProviderType providerType = AIProviderType.fromProviderId(providerId);
        if (providerType == null) {
            return;
        }

        // 保存当前输入的模型名称
        String currentModel = (String) modelComboBox.getSelectedItem();

        // 清空并添加推荐的模型列表（仅作为参考）
        modelComboBox.removeAllItems();
        for (String model : providerType.getSupportedModels()) {
            modelComboBox.addItem(model);
        }

        // 恢复用户之前输入的值，如果为空则使用默认值
        if (currentModel != null && !currentModel.trim().isEmpty()) {
            modelComboBox.setSelectedItem(currentModel);
        } else {
            modelComboBox.setSelectedItem(providerType.getDefaultModel());
        }

        // 设置提示文本
        if (modelComboBox.getEditor() != null && modelComboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
            textField.setToolTipText(JavaDocBundle.message("settings.model.hint"));
        }
    }

    /**
     * 刷新可用模型列表（用户手动触发）
     *
     * <p>当用户点击"获取最新模型"按钮时调用此方法。
     * 会显示加载状态，并在完成后恢复按钮状态。
     */
    private void refreshAvailableModels() {
        String displayName = (String) providerComboBox.getSelectedItem();
        String baseUrl = baseUrlField.getText().trim();

        if (displayName == null || baseUrl.isEmpty()) {
            JOptionPane.showMessageDialog(
                mainPanel,
                JavaDocBundle.message("error.base.url.missing"),
                JavaDocBundle.message("settings.error.title"),
                JOptionPane.WARNING_MESSAGE
                                         );
            return;
        }

        // 将显示名称转换为提供商标识符
        String providerId = AIProviderType.getProviderIdByDisplayName(displayName);
        if (providerId == null) {
            return;
        }

        // 检查是否需要 API Key
        AIProviderType providerType = AIProviderType.fromProviderId(providerId);
        boolean needsApiKey = providerType != null && providerType.requiresApiKey();
        if (needsApiKey && apiKeyField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(
                mainPanel,
                JavaDocBundle.message("error.api.key.missing"),
                JavaDocBundle.message("settings.error.title"),
                JOptionPane.WARNING_MESSAGE
                                         );
            return;
        }

        // 设置按钮状态
        refreshModelsButton.setEnabled(false);
        refreshModelsButton.setText(JavaDocBundle.message("settings.refresh.models.testing"));

        // 在后台线程中获取模型列表
        new Thread(() -> {
            try {
                SettingsState tempSettings = new SettingsState();
                tempSettings.aiProvider = providerId;
                tempSettings.baseUrl = baseUrl;
                tempSettings.apiKey = new String(apiKeyField.getPassword());
                tempSettings.configurationVerified = true;

                AIServiceProvider provider = AIServiceFactory.createProvider(tempSettings);
                if (provider == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "创建 AI 服务提供商失败，请检查配置是否正确",
                            JavaDocBundle.message("settings.error.title"),
                            JOptionPane.ERROR_MESSAGE
                                                     );
                        refreshModelsButton.setText(JavaDocBundle.message("settings.refresh.models"));
                        refreshModelsButton.setEnabled(true);
                    });
                    return;
                }

                List<String> availableModels = provider.getAvailableModels();

                // 在 UI 线程中更新下拉框
                SwingUtilities.invokeLater(() -> {
                    if (!availableModels.isEmpty()) {
                        // 保存当前选择的模型
                        String currentModel = (String) modelComboBox.getSelectedItem();

                        // 清空当前列表
                        modelComboBox.removeAllItems();

                        // 添加可用模型
                        for (String model : availableModels) {
                            modelComboBox.addItem(model);
                        }

                        // 尝试恢复用户之前选择的模型
                        if (currentModel != null && !currentModel.trim().isEmpty()) {
                            // 检查当前模型是否在可用列表中
                            boolean found = false;
                            for (String model : availableModels) {
                                if (model.equals(currentModel)) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                modelComboBox.setSelectedItem(currentModel);
                            } else {
                                // 如果当前模型不可用，选择第一个可用模型
                                modelComboBox.setSelectedIndex(0);
                            }
                        } else {
                            // 如果没有当前选择，使用默认模型
                            modelComboBox.setSelectedItem(provider.getDefaultModel());
                        }

                        // 更新提示文本
                        if (modelComboBox.getEditor() != null &&
                            modelComboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
                            textField.setToolTipText("从服务提供商获取的可用模型列表");
                        }

                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "成功获取到 " + availableModels.size() + " 个可用模型",
                            JavaDocBundle.message("settings.test.result.title"),
                            JOptionPane.INFORMATION_MESSAGE
                                                     );
                    } else {
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "未获取到可用模型，请检查配置是否正确",
                            JavaDocBundle.message("settings.error.title"),
                            JOptionPane.WARNING_MESSAGE
                                                     );
                    }

                    refreshModelsButton.setText(JavaDocBundle.message("settings.refresh.models"));
                    refreshModelsButton.setEnabled(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        mainPanel,
                        "获取模型列表失败: " + e.getMessage(),
                        JavaDocBundle.message("settings.error.title"),
                        JOptionPane.ERROR_MESSAGE
                                                 );
                    refreshModelsButton.setText(JavaDocBundle.message("settings.refresh.models"));
                    refreshModelsButton.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * 更新可用的模型列表
     *
     * <p>当用户输入了 Base URL 和 API Key（如果需要）后，
     * 尝试从 AI 服务提供商获取实际的可用模型列表。
     * 这个方法会调用提供商的 getAvailableModels() 方法。
     *
     * <p>更新策略：
     * <ul>
     *   <li>检查是否已输入必要的配置信息</li>
     *   <li>创建临时的提供商实例</li>
     *   <li>调用 getAvailableModels() 获取实际模型列表</li>
     *   <li>更新下拉框内容</li>
     *   <li>保持用户当前选择的模型（如果仍然可用）</li>
     * </ul>
     *
     * <p>错误处理：
     * <ul>
     *   <li>网络错误：静默失败，保持当前模型列表</li>
     *   <li>认证错误：静默失败，保持当前模型列表</li>
     *   <li>解析错误：静默失败，保持当前模型列表</li>
     * </ul>
     */
    private void updateAvailableModels() {
        String displayName = (String) providerComboBox.getSelectedItem();
        String baseUrl = baseUrlField.getText().trim();

        if (displayName == null || baseUrl.isEmpty()) {
            return;
        }

        // 将显示名称转换为提供商标识符
        String providerId = AIProviderType.getProviderIdByDisplayName(displayName);
        if (providerId == null) {
            return;
        }

        // 检查是否需要 API Key
        AIProviderType providerType = AIProviderType.fromProviderId(providerId);
        boolean needsApiKey = providerType != null && providerType.requiresApiKey();
        if (needsApiKey && apiKeyField.getPassword().length == 0) {
            return;
        }

        // 保存当前选择的模型
        String currentModel = (String) modelComboBox.getSelectedItem();

        // 在后台线程中获取模型列表
        new Thread(() -> {
            try {
                SettingsState tempSettings = new SettingsState();
                tempSettings.aiProvider = providerId;
                tempSettings.baseUrl = baseUrl;
                tempSettings.apiKey = new String(apiKeyField.getPassword());
                tempSettings.configurationVerified = true;

                AIServiceProvider provider = AIServiceFactory.createProvider(tempSettings);
                if (provider == null) {
                    return;
                }

                List<String> availableModels = provider.getAvailableModels();

                // 在 UI 线程中更新下拉框
                SwingUtilities.invokeLater(() -> {
                    if (!availableModels.isEmpty()) {
                        // 清空当前列表
                        modelComboBox.removeAllItems();

                        // 添加可用模型
                        for (String model : availableModels) {
                            modelComboBox.addItem(model);
                        }

                        // 尝试恢复用户之前选择的模型
                        if (currentModel != null && !currentModel.trim().isEmpty()) {
                            // 检查当前模型是否在可用列表中
                            boolean found = false;
                            for (String model : availableModels) {
                                if (model.equals(currentModel)) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                modelComboBox.setSelectedItem(currentModel);
                            } else {
                                // 如果当前模型不可用，选择第一个可用模型
                                modelComboBox.setSelectedIndex(0);
                            }
                        } else {
                            // 如果没有当前选择，使用默认模型
                            modelComboBox.setSelectedItem(provider.getDefaultModel());
                        }

                        // 更新提示文本
                        if (modelComboBox.getEditor() != null &&
                            modelComboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
                            textField.setToolTipText("从服务提供商获取的可用模型列表");
                        }
                    }
                });

            } catch (Exception e) {
                // 静默失败，保持当前模型列表
                // 在实际应用中，可以考虑显示一个非阻塞的提示
            }
        }).start();
    }

    private void updateDefaultValues() {
        String displayName = (String) providerComboBox.getSelectedItem();
        if (displayName == null) {
            return;
        }

        // 将显示名称转换为提供商标识符
        String providerId = AIProviderType.getProviderIdByDisplayName(displayName);
        if (providerId == null) {
            return;
        }

        AIProviderType providerType = AIProviderType.fromProviderId(providerId);
        if (providerType == null) {
            return;
        }

        // 优先使用已保存的配置，如果没有则使用默认配置
        SettingsState savedSettings = SettingsState.getInstance();
        SettingsState.ProviderConfig savedConfig = findSavedProviderConfig(providerId);

        if (savedConfig != null) {
            // 使用已保存的配置
            baseUrlField.setText(savedConfig.baseUrl);
            modelComboBox.setSelectedItem(savedConfig.modelName);
            apiKeyField.setText(savedConfig.apiKey);
        } else {
            // 使用枚举中的默认配置
            baseUrlField.setText(providerType.getDefaultBaseUrl());
            modelComboBox.setSelectedItem(providerType.getDefaultModel());
            apiKeyField.setText(""); // API Key 默认为空
        }
    }

    /**
     * 查找已保存的提供商配置
     *
     * @param providerId 提供商ID
     * @return 已保存的配置，如果没有找到则返回null
     */
    private SettingsState.ProviderConfig findSavedProviderConfig(String providerId) {
        SettingsState settings = SettingsState.getInstance();
        // 优先查找已验证的配置，如果没有则查找所有配置（包括未验证的）
        return settings.getAvailableProviders().stream()
            .filter(config -> providerId.equals(config.providerId))
            .findFirst()
            .orElse(settings.availableProviders.stream()
                        .filter(config -> providerId.equals(config.providerId))
                        .findFirst()
                        .orElse(null));
    }

    private void updateApiKeyVisibility() {
        String displayName = (String) providerComboBox.getSelectedItem();
        if (displayName == null) {
            apiKeyField.setEnabled(false);
            testConnectionButton.setEnabled(false);
            return;
        }

        // 将显示名称转换为提供商标识符
        String providerId = AIProviderType.getProviderIdByDisplayName(displayName);
        AIProviderType providerType = providerId == null ? null : AIProviderType.fromProviderId(providerId);
        boolean requiresKey = providerType != null && providerType.requiresApiKey();
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

                        // 添加到可用提供商列表
                        addToAvailableProviders();
                        
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
     * 将当前配置添加到可用提供商列表
     */
    private void addToAvailableProviders() {
        SettingsState settings = SettingsState.getInstance();
        SettingsState currentSettings = getSettings();

        // 创建提供商配置
        SettingsState.ProviderConfig providerConfig = new SettingsState.ProviderConfig(
            currentSettings.aiProvider,
            currentSettings.modelName,
            currentSettings.baseUrl,
            currentSettings.apiKey,
            true
        );

        // 添加到可用提供商列表
        settings.addOrUpdateProvider(providerConfig);
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

        // AI 提供商配置 - 将显示名称转换为提供商标识符
        String displayName = (String) providerComboBox.getSelectedItem();
        String providerId = displayName != null ? AIProviderType.getProviderIdByDisplayName(displayName) : null;
        settings.aiProvider = providerId != null ? providerId : AIProviderType.QIANWEN.getProviderId();
        // 获取用户输入的模型名称（可能是从列表选择的，也可能是手动输入的）
        Object selectedModel = modelComboBox.getEditor().getItem();
        settings.modelName = selectedModel != null ? selectedModel.toString().trim() : "";
        settings.setBaseUrl(baseUrlField.getText().trim()); // 使用标准化方法
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
        settings.topP = (Double) topPSpinner.getValue();
        settings.topK = (Integer) topKSpinner.getValue();
        settings.presencePenalty = (Double) presencePenaltySpinner.getValue();
        settings.verboseLogging = verboseLoggingCheckBox.isSelected();
        settings.performanceMode = performanceModeCheckBox.isSelected();

        // Prompt 配置 - 从 Tab 页获取
        settings.systemPromptTemplate = systemPromptTextArea.getText().trim();
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
        // AI 提供商配置 - 将提供商标识符转换为显示名称
        String displayName = AIProviderType.getDisplayNameByProviderId(settings.aiProvider);
        if (displayName != null) {
            providerComboBox.setSelectedItem(displayName);
        } else {
            // 如果找不到对应的显示名称，使用默认值
            providerComboBox.setSelectedItem(AIProviderType.QIANWEN.getDisplayName());
        }
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
        topPSpinner.setValue(settings.topP);
        topKSpinner.setValue(settings.topK);
        presencePenaltySpinner.setValue(settings.presencePenalty);
        verboseLoggingCheckBox.setSelected(settings.verboseLogging);
        performanceModeCheckBox.setSelected(settings.performanceMode);

        // Prompt 配置 - 加载到 Tab 页
        systemPromptTextArea.setText(settings.systemPromptTemplate);
        classPromptTextArea.setText(settings.classPromptTemplate);
        methodPromptTextArea.setText(settings.methodPromptTemplate);
        fieldPromptTextArea.setText(settings.fieldPromptTemplate);
        testPromptTextArea.setText(settings.testPromptTemplate);

        updateApiKeyVisibility();
    }
}

