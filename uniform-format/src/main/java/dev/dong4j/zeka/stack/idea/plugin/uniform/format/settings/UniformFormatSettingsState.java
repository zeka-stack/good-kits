package dev.dong4j.zeka.stack.idea.plugin.uniform.format.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Data;

/**
 * 统一格式插件设置状态
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@Data
@State(
    name = "UniformFormatSettingsState",
    storages = @Storage("uniform-format-settings.xml")
)
public class UniformFormatSettingsState implements PersistentStateComponent<UniformFormatSettingsState> {

    /** 是否启用文件模板 */
    private boolean enableFileTemplates = true;

    /** 是否启用 Live Template */
    private boolean enableLiveTemplates = true;

    /** 是否启用代码风格配置 */
    private boolean enableCodeStyle = true;

    /** 是否启用使用统计 */
    private boolean enableStatistics = true;

    @Nullable
    @Override
    public UniformFormatSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull UniformFormatSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static UniformFormatSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(UniformFormatSettingsState.class);
    }
}
