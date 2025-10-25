package dev.dong4j.zeka.stack.idea.plugin.uniform.format.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JPanel;

import lombok.Data;

/**
 * 统一格式插件设置面板
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Data
public class UniformFormatSettingsPanel {

    private JPanel mainPanel;
    private JBCheckBox enableFileTemplatesCheckBox;
    private JBCheckBox enableLiveTemplatesCheckBox;
    private JBCheckBox enableCodeStyleCheckBox;
    private JBCheckBox enableStatisticsCheckBox;
    private JBLabel descriptionLabel;

    public UniformFormatSettingsPanel() {
        initializeComponents();
    }

    private void initializeComponents() {
        // 创建组件
        enableFileTemplatesCheckBox = new JBCheckBox("启用文件模板");
        enableLiveTemplatesCheckBox = new JBCheckBox("启用 Live Template");
        enableCodeStyleCheckBox = new JBCheckBox("启用代码风格配置");
        enableStatisticsCheckBox = new JBCheckBox("启用使用统计");

        descriptionLabel = new JBLabel("<html>" +
                                       "<p>Uniform Format 插件提供统一的代码格式化和模板管理功能。</p>" +
                                       "<ul>" +
                                       "<li><b>文件模板</b>：自动添加统一的文件头部注释</li>" +
                                       "<li><b>Live Template</b>：快速生成常用代码片段</li>" +
                                       "<li><b>代码风格</b>：自动配置统一的代码格式化规则</li>" +
                                       "<li><b>使用统计</b>：统计模板使用情况</li>" +
                                       "</ul>" +
                                       "</html>");

        // 设置默认值
        enableFileTemplatesCheckBox.setSelected(true);
        enableLiveTemplatesCheckBox.setSelected(true);
        enableCodeStyleCheckBox.setSelected(true);
        enableStatisticsCheckBox.setSelected(true);

        // 使用 FormBuilder 创建布局
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(descriptionLabel)
            .addSeparator()
            .addComponent(enableFileTemplatesCheckBox)
            .addComponent(enableLiveTemplatesCheckBox)
            .addComponent(enableCodeStyleCheckBox)
            .addComponent(enableStatisticsCheckBox)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    public boolean isModified(UniformFormatSettingsState settings) {
        return enableFileTemplatesCheckBox.isSelected() != settings.isEnableFileTemplates() ||
               enableLiveTemplatesCheckBox.isSelected() != settings.isEnableLiveTemplates() ||
               enableCodeStyleCheckBox.isSelected() != settings.isEnableCodeStyle() ||
               enableStatisticsCheckBox.isSelected() != settings.isEnableStatistics();
    }

    public void apply(UniformFormatSettingsState settings) {
        settings.setEnableFileTemplates(enableFileTemplatesCheckBox.isSelected());
        settings.setEnableLiveTemplates(enableLiveTemplatesCheckBox.isSelected());
        settings.setEnableCodeStyle(enableCodeStyleCheckBox.isSelected());
        settings.setEnableStatistics(enableStatisticsCheckBox.isSelected());
    }

    public void reset(UniformFormatSettingsState settings) {
        enableFileTemplatesCheckBox.setSelected(settings.isEnableFileTemplates());
        enableLiveTemplatesCheckBox.setSelected(settings.isEnableLiveTemplates());
        enableCodeStyleCheckBox.setSelected(settings.isEnableCodeStyle());
        enableStatisticsCheckBox.setSelected(settings.isEnableStatistics());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}