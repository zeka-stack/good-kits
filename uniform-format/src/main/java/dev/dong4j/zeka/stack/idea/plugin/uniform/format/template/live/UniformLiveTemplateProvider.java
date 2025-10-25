package dev.dong4j.zeka.stack.idea.plugin.uniform.format.template.live;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

import org.jetbrains.annotations.Nullable;

/**
 * 统一 Live Template 提供者
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
public class UniformLiveTemplateProvider implements DefaultLiveTemplatesProvider {

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return new String[] {
            "liveTemplates/uniform-live-template"
        };
    }

    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return null;
    }
}
