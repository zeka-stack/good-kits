package dev.dong4j.zeka.stack.idea.plugin.uniform.format.settings;

import com.intellij.openapi.options.Configurable;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * 统一格式插件设置配置
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
public class UniformFormatSettingsConfigurable implements Configurable {

    private UniformFormatSettingsPanel settingsPanel;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Uniform Format";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsPanel = new UniformFormatSettingsPanel();
        return settingsPanel.getMainPanel();
    }

    @Override
    public boolean isModified() {
        UniformFormatSettingsState settings = UniformFormatSettingsState.getInstance();
        return settingsPanel != null && settingsPanel.isModified(settings);
    }

    @Override
    public void apply() {
        if (settingsPanel != null) {
            UniformFormatSettingsState settings = UniformFormatSettingsState.getInstance();
            settingsPanel.apply(settings);
        }
    }

    @Override
    public void reset() {
        if (settingsPanel != null) {
            UniformFormatSettingsState settings = UniformFormatSettingsState.getInstance();
            settingsPanel.reset(settings);
        }
    }

    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }
}
