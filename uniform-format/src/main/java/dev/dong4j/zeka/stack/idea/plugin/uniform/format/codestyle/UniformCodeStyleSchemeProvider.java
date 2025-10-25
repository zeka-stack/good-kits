package dev.dong4j.zeka.stack.idea.plugin.uniform.format.codestyle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.SchemeImportException;
import com.intellij.openapi.options.SchemeImportUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemeImpl;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一代码风格方案提供者
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2024.12.19 15:30
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class UniformCodeStyleSchemeProvider {

    /** UNIFORM_CODE_STYLE_NAME */
    public static final String UNIFORM_CODE_STYLE_NAME = "uniform-code-style";
    private static final String UNIFORM_CODE_STYLE_FILE = UNIFORM_CODE_STYLE_NAME + ".xml";

    /**
     * 为项目提供统一代码风格方案
     */
    public static void provideUniformCodeStyleScheme(Project project) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                CodeStyleSchemes codeStyleSchemes = CodeStyleSchemes.getInstance();

                // 检查是否已经存在该方案
                CodeStyleScheme existingScheme = codeStyleSchemes.findSchemeByName(UNIFORM_CODE_STYLE_NAME);
                if (existingScheme != null) {
                    log.info("Uniform code style '{}' already exists, setting as current", UNIFORM_CODE_STYLE_NAME);
                    codeStyleSchemes.setCurrentScheme(existingScheme);
                    return;
                }

                // 从资源文件获取 VirtualFile
                URL resource = UniformCodeStyleSchemeProvider.class.getClassLoader().getResource(UNIFORM_CODE_STYLE_FILE);
                if (resource == null) {
                    log.error("Code style file not found: {}", UNIFORM_CODE_STYLE_FILE);
                    return;
                }

                VirtualFile vFile = VfsUtil.findFileByURL(resource);
                if (vFile == null) {
                    log.error("Failed to find virtual file for: {}", UNIFORM_CODE_STYLE_FILE);
                    return;
                }

                // 导入代码样式方案
                importScheme(project, vFile);

                log.info("Uniform code style '{}' imported and set as default for project: {}",
                         UNIFORM_CODE_STYLE_NAME, project.getName());

            } catch (Exception e) {
                log.error("Failed to provide uniform code style scheme", e);
            }
        });
    }

    /**
     * 导入代码样式方案
     */
    private static void importScheme(@NotNull Project project, @NotNull VirtualFile selectedFile) throws SchemeImportException {
        Element rootElement = SchemeImportUtil.loadSchemeDom(selectedFile);
        Element schemeRoot = findSchemeRoot(rootElement);

        CodeStyleScheme derivedScheme = CodeStyleSchemes
            .getInstance()
            .createNewScheme(UNIFORM_CODE_STYLE_NAME, null);

        readSchemeFromDom(schemeRoot, derivedScheme);

        CodeStyleSchemes.getInstance().addScheme(derivedScheme);
        CodeStyleSchemesImpl.getSchemeManager().setCurrent(derivedScheme);
        CodeStyleSettingsManager.getInstance(project).PREFERRED_PROJECT_CODE_STYLE = derivedScheme.getName();
    }

    /**
     * 查找方案根元素
     */
    @NotNull
    private static Element findSchemeRoot(@NotNull Element rootElement) throws SchemeImportException {
        String rootName = rootElement.getName();

        // Project code style 172.x and earlier
        if ("project".equals(rootName)) {
            Element child = rootElement.getChild("component");
            if (child != null && "ProjectCodeStyleSettingsManager".equals(child.getAttributeValue("name"))) {
                child = child.getChild("option");
                if (child != null && "PER_PROJECT_SETTINGS".equals(child.getAttributeValue("name"))) {
                    child = child.getChild("value");
                    if (child != null) {
                        return child;
                    }
                }
            }
            throw new SchemeImportException("Invalid scheme root: " + rootName);
        }
        // Project code style 173.x and later
        else if ("component".equals(rootName)) {
            if ("ProjectCodeStyleConfiguration".equals(rootElement.getAttributeValue("name"))) {
                Element child = rootElement.getChild("code_scheme");
                if (child != null) {
                    return child;
                }
            }
            throw new SchemeImportException("Invalid scheme root: " + rootName);
        }
        return rootElement;
    }

    /**
     * 从 DOM 读取方案
     */
    private static void readSchemeFromDom(@NotNull Element rootElement, @NotNull CodeStyleScheme scheme)
        throws SchemeImportException {
        CodeStyleSettings newSettings = new CodeStyleSettings();
        loadSettings(rootElement, newSettings);
        newSettings.resetDeprecatedFields();
        ((CodeStyleSchemeImpl) scheme).setCodeStyleSettings(newSettings);
    }

    /**
     * 加载设置
     */
    private static void loadSettings(@NotNull Element rootElement, @NotNull CodeStyleSettings settings) throws SchemeImportException {
        try {
            settings.readExternal(findSchemeRoot(rootElement));
        } catch (InvalidDataException e) {
            throw new SchemeImportException("Failed to load code style settings: " + e.getMessage());
        }
    }

    /**
     * 检查是否已提供统一代码风格方案
     */
    public static boolean isUniformCodeStyleSchemeProvided(Project project) {
        try {
            CodeStyleSchemes codeStyleSchemes = CodeStyleSchemes.getInstance();
            CodeStyleScheme scheme = codeStyleSchemes.findSchemeByName(UNIFORM_CODE_STYLE_NAME);

            if (scheme != null) {
                // 检查是否为当前默认方案
                CodeStyleScheme currentScheme = codeStyleSchemes.getCurrentScheme();
                return currentScheme != null && UNIFORM_CODE_STYLE_NAME.equals(currentScheme.getName());
            }

            return false;

        } catch (Exception e) {
            log.warn("Failed to check uniform code style scheme status", e);
            return false;
        }
    }
}