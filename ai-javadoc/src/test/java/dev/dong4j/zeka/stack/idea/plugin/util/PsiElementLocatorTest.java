package dev.dong4j.zeka.stack.idea.plugin.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.javadoc.PsiDocComment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PsiElementLocator 单元测试
 */
@DisplayName("PsiElementLocator 单元测试")
public class PsiElementLocatorTest {

    @Mock
    private Editor mockEditor;

    @Mock
    private PsiJavaFile mockPsiJavaFile;

    @Mock
    private PsiFile mockNonJavaFile;

    @Mock
    private PsiMethod mockMethod;

    @Mock
    private PsiField mockField;

    @Mock
    private PsiClass mockClass;

    @Mock
    private PsiElement mockElement;

    @Mock
    private PsiIdentifier mockIdentifier;

    @Mock
    private PsiModifierList mockModifierList;

    @Mock
    private PsiDocComment mockDocComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("测试定位非 Java 文件返回 null")
    void testLocateElement_nonJavaFile() {
        when(mockEditor.getCaretModel()).thenReturn(mock(com.intellij.openapi.editor.CaretModel.class));
        when(mockEditor.getCaretModel().getOffset()).thenReturn(100);

        PsiElementLocator.LocateResult result = PsiElementLocator.locateElement(mockEditor, mockNonJavaFile);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("测试定位偏移量处无元素时返回文件")
    void testLocateElementAtOffset_noElementAtOffset() {
        when(mockPsiJavaFile.findElementAt(100)).thenReturn(null);

        PsiElementLocator.LocateResult result = PsiElementLocator.locateElementAtOffset(mockPsiJavaFile, 100);

        assertThat(result).isNotNull();
        assertThat(result.element()).isEqualTo(mockPsiJavaFile);
        assertThat(result.type()).isEqualTo(PsiElementLocator.LocateType.FILE);
        assertThat(result.isWholeFile()).isTrue();
    }

    @Test
    @DisplayName("测试检查元素是否已有 JavaDoc - 有注释")
    void testHasJavaDoc_withDocComment() {
        PsiDocCommentOwner mockDocOwner = mock(PsiDocCommentOwner.class);
        when(mockDocOwner.getDocComment()).thenReturn(mockDocComment);

        boolean result = PsiElementLocator.hasJavaDoc(mockDocOwner);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试检查元素是否已有 JavaDoc - 无注释")
    void testHasJavaDoc_withoutDocComment() {
        PsiDocCommentOwner mockDocOwner = mock(PsiDocCommentOwner.class);
        when(mockDocOwner.getDocComment()).thenReturn(null);

        boolean result = PsiElementLocator.hasJavaDoc(mockDocOwner);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试检查元素是否已有 JavaDoc - 非 DocCommentOwner")
    void testHasJavaDoc_notDocCommentOwner() {
        boolean result = PsiElementLocator.hasJavaDoc(mockElement);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试获取元素描述 - 方法")
    void testGetElementDescription_method() {
        when(mockMethod.getName()).thenReturn("getUserName");

        String description = PsiElementLocator.getElementDescription(mockMethod);

        assertThat(description).isEqualTo("方法: getUserName()");
    }

    @Test
    @DisplayName("测试获取元素描述 - 字段")
    void testGetElementDescription_field() {
        when(mockField.getName()).thenReturn("username");

        String description = PsiElementLocator.getElementDescription(mockField);

        assertThat(description).isEqualTo("字段: username");
    }

    @Test
    @DisplayName("测试获取元素描述 - 类")
    void testGetElementDescription_class() {
        when(mockClass.getName()).thenReturn("UserService");

        String description = PsiElementLocator.getElementDescription(mockClass);

        assertThat(description).isEqualTo("类: UserService");
    }

    @Test
    @DisplayName("测试获取元素描述 - 文件")
    void testGetElementDescription_file() {
        when(mockPsiJavaFile.getName()).thenReturn("UserService.java");

        String description = PsiElementLocator.getElementDescription(mockPsiJavaFile);

        assertThat(description).isEqualTo("文件: UserService.java");
    }

    @Test
    @DisplayName("测试获取元素描述 - 其他元素")
    void testGetElementDescription_otherElement() {
        String description = PsiElementLocator.getElementDescription(mockElement);

        assertThat(description).contains("元素:");
        assertThat(description).contains("PsiElement");
    }

    @Test
    @DisplayName("测试获取类型描述 - METHOD")
    void testGetTypeDescription_method() {
        String description = PsiElementLocator.getTypeDescription(PsiElementLocator.LocateType.METHOD);

        assertThat(description).isEqualTo("方法");
    }

    @Test
    @DisplayName("测试获取类型描述 - FIELD")
    void testGetTypeDescription_field() {
        String description = PsiElementLocator.getTypeDescription(PsiElementLocator.LocateType.FIELD);

        assertThat(description).isEqualTo("字段");
    }

    @Test
    @DisplayName("测试获取类型描述 - CLASS")
    void testGetTypeDescription_class() {
        String description = PsiElementLocator.getTypeDescription(PsiElementLocator.LocateType.CLASS);

        assertThat(description).isEqualTo("类");
    }

    @Test
    @DisplayName("测试获取类型描述 - FILE")
    void testGetTypeDescription_file() {
        String description = PsiElementLocator.getTypeDescription(PsiElementLocator.LocateType.FILE);

        assertThat(description).isEqualTo("文件");
    }

    @Test
    @DisplayName("测试 LocateResult 的方法")
    void testLocateResult_methods() {
        PsiElementLocator.LocateResult result = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        assertThat(result.isMethod()).isTrue();
        assertThat(result.isField()).isFalse();
        assertThat(result.isClass()).isFalse();
        assertThat(result.isWholeFile()).isFalse();
    }

    @Test
    @DisplayName("测试 LocateResult - Field")
    void testLocateResult_field() {
        PsiElementLocator.LocateResult result = new PsiElementLocator.LocateResult(
            mockField,
            PsiElementLocator.LocateType.FIELD,
            false
        );

        assertThat(result.isMethod()).isFalse();
        assertThat(result.isField()).isTrue();
        assertThat(result.isClass()).isFalse();
    }

    @Test
    @DisplayName("测试 LocateResult - Class")
    void testLocateResult_class() {
        PsiElementLocator.LocateResult result = new PsiElementLocator.LocateResult(
            mockClass,
            PsiElementLocator.LocateType.CLASS,
            true
        );

        assertThat(result.isMethod()).isFalse();
        assertThat(result.isField()).isFalse();
        assertThat(result.isClass()).isTrue();
        assertThat(result.isWholeFile()).isTrue();
    }

    @Test
    @DisplayName("测试 LocateResult toString")
    void testLocateResult_toString() {
        when(mockMethod.getClass()).thenReturn((Class) PsiMethod.class);

        PsiElementLocator.LocateResult result = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        String toString = result.toString();
        assertThat(toString).contains("LocateResult");
        assertThat(toString).contains("METHOD");
        assertThat(toString).contains("false");
    }

    @Test
    @DisplayName("测试 LocateType 枚举所有值")
    void testLocateType_allValues() {
        PsiElementLocator.LocateType[] types = PsiElementLocator.LocateType.values();

        assertThat(types).contains(
            PsiElementLocator.LocateType.METHOD,
            PsiElementLocator.LocateType.FIELD,
            PsiElementLocator.LocateType.CLASS,
            PsiElementLocator.LocateType.FILE
                                  );
    }

    @Test
    @DisplayName("测试 LocateResult getter 方法")
    void testLocateResult_getters() {
        PsiElementLocator.LocateResult result = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        assertThat(result.element()).isEqualTo(mockMethod);
        assertThat(result.type()).isEqualTo(PsiElementLocator.LocateType.METHOD);
        assertThat(result.isWholeFile()).isFalse();
    }

    @Test
    @DisplayName("测试获取元素描述 - 方法名为 null")
    void testGetElementDescription_methodWithNullName() {
        when(mockMethod.getName()).thenReturn(null);

        String description = PsiElementLocator.getElementDescription(mockMethod);

        assertThat(description).isEqualTo("方法: null()");
    }

    @Test
    @DisplayName("测试获取元素描述 - 字段名为 null")
    void testGetElementDescription_fieldWithNullName() {
        when(mockField.getName()).thenReturn(null);

        String description = PsiElementLocator.getElementDescription(mockField);

        assertThat(description).isEqualTo("字段: null");
    }

    @Test
    @DisplayName("测试获取元素描述 - 类名为 null")
    void testGetElementDescription_classWithNullName() {
        when(mockClass.getName()).thenReturn(null);

        String description = PsiElementLocator.getElementDescription(mockClass);

        assertThat(description).isEqualTo("类: null");
    }

    @Test
    @DisplayName("测试 LocateResult 相等性")
    void testLocateResult_equality() {
        PsiElementLocator.LocateResult result1 = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        PsiElementLocator.LocateResult result2 = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        // Record 类会自动实现 equals
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("测试 LocateResult 不相等")
    void testLocateResult_notEqual() {
        PsiElementLocator.LocateResult result1 = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            false
        );

        PsiElementLocator.LocateResult result2 = new PsiElementLocator.LocateResult(
            mockMethod,
            PsiElementLocator.LocateType.METHOD,
            true  // 不同的 isWholeFile
        );

        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("测试所有类型的判断方法")
    void testLocateResult_allTypeChecks() {
        // Method
        PsiElementLocator.LocateResult methodResult = new PsiElementLocator.LocateResult(
            mockMethod, PsiElementLocator.LocateType.METHOD, false
        );
        assertThat(methodResult.isMethod()).isTrue();

        // Field
        PsiElementLocator.LocateResult fieldResult = new PsiElementLocator.LocateResult(
            mockField, PsiElementLocator.LocateType.FIELD, false
        );
        assertThat(fieldResult.isField()).isTrue();

        // Class
        PsiElementLocator.LocateResult classResult = new PsiElementLocator.LocateResult(
            mockClass, PsiElementLocator.LocateType.CLASS, false
        );
        assertThat(classResult.isClass()).isTrue();

        // File - 没有 isFile() 方法，只能通过 type() 判断
        PsiElementLocator.LocateResult fileResult = new PsiElementLocator.LocateResult(
            mockPsiJavaFile, PsiElementLocator.LocateType.FILE, true
        );
        assertThat(fileResult.type()).isEqualTo(PsiElementLocator.LocateType.FILE);
    }
}

