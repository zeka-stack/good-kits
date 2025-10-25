package dev.dong4j.zeka.stack.idea.plugin.uniform.format.template.live;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * 统一 Live Template 上下文
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
public class UniformLiveTemplateContext extends TemplateContextType {

    protected UniformLiveTemplateContext() {
        super("UNIFORM", "Uniform", TemplateContextType.class);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return true; // 在所有上下文中都可用
    }
}
