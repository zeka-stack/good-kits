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

    /** 主界面主面板，用于承载主要功能组件和布局 */
    private JPanel mainPanel;

    // AI 提供商配置
    /** AI 服务商下拉选择框 */
    private ComboBox<String> providerComboBox;
    /** 模型下拉框组件，用于选择不同的模型 */
    private ComboBox<String> modelComboBox;
    /** 基础 URL 输入框 */
    private JBTextField baseUrlField;
    /** API 密钥输入框 */
    private JBPasswordField apiKeyField;
    /** 测试连接按钮 */
    private JButton testConnectionButton;
    /** 刷新模型按钮 */
    private JButton refreshModelsButton;

    // 验证状态标记
    /** 配置是否已验证的标记 */
    private boolean configurationVerified = false;

    // 功能配置
    /** 生成针对类的复选框 */
    private JBCheckBox generateForClassCheckBox;
    /** 方法生成复选框，用于控制是否为方法生成代码 */
    private JBCheckBox generateForMethodCheckBox;
    /** 生成字段的复选框 */
    private JBCheckBox generateForFieldCheckBox;
    /** 跳过已存在的文件复选框 */
    private JBCheckBox skipExistingCheckBox;
    /** 优化类代码的复选框 */
    private JBCheckBox optimizeClassCodeCheckBox;
    /** 最大类代码行数设置控件 */
    private JSpinner maxClassCodeLinesSpinner;

    // 语言支持
    /** Java 语言支持选项框 */
    private JBCheckBox javaCheckBox;
    /** Kotlin 语言支持开关控件 */
    private JBCheckBox kotlinCheckBox;

    // 高级配置
    /** 最大重试次数的下拉选择器 */
    private JSpinner maxRetriesSpinner;
    /** 超时时间选择器，用于设置请求超时时间 */
    private JSpinner timeoutSpinner;
    /** 温度选择下拉框 */
    private JSpinner temperatureSpinner;
    /** 最大令牌数输入控件 */
    private JSpinner maxTokensSpinner;
    /** 顶部参数的下拉选择器控件 */
    private JSpinner topPSpinner;
    /** 用于选择 Top K 值的下拉框组件 */
    private JSpinner topKSpinner;
    /** 偏差惩罚系数调节器，用于设置生成文本时的偏差惩罚值 */
    private JSpinner presencePenaltySpinner;
    /** 日志详细模式复选框，用于控制是否输出详细日志信息 */
    private JBCheckBox verboseLoggingCheckBox;
    /** 性能模式复选框，用于启用或禁用性能优化模式 */
    private JBCheckBox performanceModeCheckBox;

    /** 系统提示文本区域，用于显示或编辑系统提示内容 */
    public JTextArea systemPromptTextArea;
    /** 类提示文本区域，用于显示或输入类相关的提示信息 */
    public JTextArea classPromptTextArea;
    /** 方法提示文本区域，用于显示方法相关的提示信息 */
    public JTextArea methodPromptTextArea;
    /** 提示信息显示区域，用于展示操作提示或说明文字 */
    public JTextArea fieldPromptTextArea;
    /** 测试提示文本区域 */
    public JTextArea testPromptTextArea;

    /**
     * 构造函数，初始化 JavaDoc 设置面板
     * <p>
     * 调用创建用户界面和设置事件监听器的方法，完成面板的初始化
     */
    public JavaDocSettingsPanel() {
        createUI();
        setupListeners();
    }

    /**
     * 初始化用户界面组件，创建并配置所有 UI 元素，包括下拉框、文本字段、按钮、复选框等。
     * <p>
     * 该方法负责构建整个设置界面的主面板，包括 AI 提供商配置、模型选择、基础 URL 和 API 密钥输入、
     * 连接测试按钮、模型刷新按钮、生成选项、语言支持、高级配置参数以及提示模板区域。
     *
     * @param 无 参数
     * @return 无 返回值
     */
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
            .addComponent(createGenerationOptionsPanel())

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

    /**
     * 创建生成选项面板
     *
     * @return 生成选项面板
     */
    private JPanel createGenerationOptionsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.BorderLayout());

        // 第一行：3个复选框水平排列
        JBCheckBox[] firstRowCheckBoxes = {
            generateForClassCheckBox,
            generateForMethodCheckBox,
            generateForFieldCheckBox
        };

        String[] firstRowHintKeys = {
            "settings.generate.for.class.hint",
            "settings.generate.for.method.hint",
            "settings.generate.for.field.hint"
        };

        JPanel firstRowPanel = createHorizontalCheckBoxPanel(firstRowCheckBoxes, firstRowHintKeys, 3);
        mainPanel.add(firstRowPanel, java.awt.BorderLayout.NORTH);

        // 第二行：2个复选框垂直排列，调整间距
        JPanel secondRowPanel = new JPanel();
        secondRowPanel.setLayout(new java.awt.BorderLayout());
        secondRowPanel.setBorder(JBUI.Borders.emptyTop(8)); // 与第一行保持适当间距

        // 创建垂直布局的面板，控制两个复选框之间的间距
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new java.awt.BorderLayout());
        verticalPanel.add(createCheckBoxWithHint(skipExistingCheckBox, "settings.skip.existing.hint"), java.awt.BorderLayout.NORTH);

        // 添加间距面板
        JPanel spacingPanel = new JPanel();
        spacingPanel.setPreferredSize(new java.awt.Dimension(0, 8)); // 8像素高度间距
        verticalPanel.add(spacingPanel, java.awt.BorderLayout.CENTER);

        verticalPanel.add(createCheckBoxWithHint(optimizeClassCodeCheckBox, "settings.optimize.class.code.hint"),
                          java.awt.BorderLayout.SOUTH);

        secondRowPanel.add(verticalPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(secondRowPanel, java.awt.BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * 创建模型配置面板
     * <p>
     * 用于构建包含模型选择下拉框和右侧按钮的面板，包含刷新模型按钮、测试连接按钮以及提示标签。
     *
     * @return 模型配置面板
     */
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

    /**
     * 创建API密钥输入面板
     * <p>
     * 初始化并返回一个包含API密钥输入字段的面板，使用BorderLayout布局。
     *
     * @return 包含API密钥输入字段的面板
     */
    private JPanel createApiKeyPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(apiKeyField, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建高级配置面板，包含一个带宽度限制的 JSpinner 和提示标签
     * <p>
     * 该方法用于构建一个布局面板，左侧放置一个设置宽度的 JSpinner 控件，右侧放置一个带有提示信息的标签。
     * 提示标签的字体大小和颜色会根据系统 UI 设置进行调整。
     *
     * @param spinner 用于配置的 JSpinner 控件
     * @param hintKey 提示信息的键，用于从资源文件中获取对应的提示文本
     * @return 包含 JSpinner 和提示标签的面板
     */
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

    /**
     * 创建包含复选框和提示文本的面板
     * <p>
     * 该方法用于创建一个包含复选框和提示文本的面板，提示文本通过指定的键从资源文件中获取，并设置为较暗的字体颜色和固定宽度。
     *
     * @param checkBox 要添加到面板中的复选框
     * @param hintKey  用于获取提示文本的资源键
     * @return 包含复选框和提示文本的面板
     */
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

    /**
     * 创建水平排列的复选框面板
     *
     * @param checkBoxes  复选框数组
     * @param hintKeys    对应的提示文本键数组
     * @param itemsPerRow 每行显示的复选框数量
     * @return 水平排列的复选框面板
     */
    private JPanel createHorizontalCheckBoxPanel(JBCheckBox[] checkBoxes, String[] hintKeys, int itemsPerRow) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

        // 设置间距 - 减少水平间距
        gbc.insets = JBUI.insets(5, 1);
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        for (int i = 0; i < checkBoxes.length; i++) {
            // 计算行和列
            int row = i / itemsPerRow;

            gbc.gridx = i % itemsPerRow;
            gbc.gridy = row;
            gbc.weightx = 1.0 / itemsPerRow; // 平均分配宽度

            // 创建单个复选框的面板
            JPanel checkBoxPanel = new JPanel(new BorderLayout(5, 0));
            checkBoxPanel.add(checkBoxes[i], BorderLayout.WEST);

            // 添加提示文本
            if (i < hintKeys.length && hintKeys[i] != null) {
                JBLabel hintLabel = new JBLabel(JavaDocBundle.message(hintKeys[i]));
                hintLabel.setFont(hintLabel.getFont().deriveFont(hintLabel.getFont().getSize() - 2.0f));
                hintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                checkBoxPanel.add(hintLabel, BorderLayout.CENTER);
            }

            mainPanel.add(checkBoxPanel, gbc);
        }

        return mainPanel;
    }

    /**
     * 创建用于显示提示配置的选项卡面板
     * <p>
     * 初始化一个包含多个提示配置选项卡的 JBTabbedPane，每个选项卡对应不同的提示类型，如系统提示、类提示、方法提示等。
     *
     * @return 包含提示配置选项卡的 JBTabbedPane 实例
     */
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

    /**
     * 创建提示信息标签页面板
     * <p>
     * 根据给定的文本区域和提示类型，创建一个包含文本区域和重置按钮的标签页面板。
     *
     * @param textArea   文本区域组件
     * @param promptType 提示类型，用于加载对应的提示信息和资源
     * @return 包含文本区域和重置按钮的面板
     */
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

    /**
     * 将指定类型的提示内容重置为默认模板
     * <p>
     * 根据传入的提示类型，获取对应的默认提示模板，并将其设置到指定的文本区域中。
     *
     * @param promptType 提示类型，如 "system"、"class"、"method" 等
     * @param textArea   要设置默认模板的文本区域组件
     */
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

    /**
     * 创建一个带有指定文本区域的滚动面板
     * <p>
     * 该方法用于创建一个 JScrollPane 实例，并设置其首选大小和滚动条策略。
     *
     * @param textArea 要放入滚动面板中的文本区域
     * @return 配置好的滚动面板实例
     */
    private JScrollPane createScrollPane(JTextArea textArea) {
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    /**
     * 初始化各种监听器，用于响应用户界面组件的变化
     * <p>
     * 该方法为各个输入组件添加动作监听器，当组件内容发生变化时，触发相应的更新或验证状态清除操作。
     * 包括提供商、Base URL、API Key、模型选择以及代码优化配置等变化的监听。
     *
     * @param 无
     * @return 无
     * @throws 无
     */
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

    /**
     * 更新模型列表，根据选择的提供商标识符加载对应的模型选项
     * <p>
     * 该方法首先获取用户选择的提供商显示名称，将其转换为对应的提供商标识符。
     * 然后根据该标识符获取对应的提供商类型，并加载该类型支持的所有模型。
     * 最后将用户之前选择的模型恢复到下拉框中，若为空则使用默认模型。
     * 同时设置模型输入框的提示文本。
     *
     * @param 无 参数
     * @return 无 返回值
     */
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

    /**
     * 更新默认值配置，根据选择的提供者设置对应的配置信息
     * <p>
     * 该方法首先获取当前选择的显示名称，将其转换为对应的提供者标识符。如果标识符有效，则获取对应的提供者类型。
     * 接着，尝试从已保存的配置中查找对应的配置信息，如果存在则使用该配置；否则使用提供者类型中的默认配置。
     * 最后，将配置信息填充到对应的 UI 控件中。
     *
     * @param 无 该方法不接受任何参数
     * @return 无 该方法无返回值
     */
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

    /**
     * 更新API密钥的可见性状态
     * <p>
     * 根据下拉框中选择的提供商显示名称，判断是否需要显示API密钥字段和测试连接按钮。
     * 如果选择的提供商为空，则禁用API密钥字段和测试连接按钮；否则根据提供商类型决定是否启用相关控件。
     */
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

    /**
     * 测试与 AI 服务提供商的连接
     * <p>
     * 该方法用于验证当前配置是否能够成功创建 AI 服务提供商，并测试其配置是否有效。
     * 在测试过程中，会临时允许创建未验证的提供商，测试完成后会根据结果更新配置状态。
     */
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

    /**
     * 获取主面板组件
     * <p>
     * 返回应用程序中主面板的引用，用于界面展示或操作。
     *
     * @return 主面板组件
     */
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

