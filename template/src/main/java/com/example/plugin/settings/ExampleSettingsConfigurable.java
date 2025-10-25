package com.example.plugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 插件设置配置界面
 */
public class ExampleSettingsConfigurable implements Configurable {

    private ExampleSettingsPanel settingsPanel;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Example Plugin";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsPanel = new ExampleSettingsPanel();
        return settingsPanel.getMainPanel();
    }

    @Override
    public boolean isModified() {
        SettingsState settings = SettingsState.getInstance();
        return settingsPanel.isModified(settings);
    }

    @Override
    public void apply() throws ConfigurationException {
        SettingsState settings = SettingsState.getInstance();
        settingsPanel.apply(settings);
    }

    @Override
    public void reset() {
        SettingsState settings = SettingsState.getInstance();
        settingsPanel.reset(settings);
    }

    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }
}
