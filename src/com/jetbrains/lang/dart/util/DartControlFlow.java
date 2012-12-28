package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartExecutionScope;
import com.jetbrains.lang.dart.psi.DartReference;
import gnu.trove.THashSet;

import java.util.List;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartControlFlow {

  private List<DartComponentName> myParameters;
  private final List<DartComponentName> myReturnValues;

  protected DartControlFlow(List<DartComponentName> inComponentNames, List<DartComponentName> outDeclarations) {
    myParameters = inComponentNames;
    myReturnValues = outDeclarations;
  }

  public List<DartComponentName> getParameters() {
    return myParameters;
  }

  public List<DartComponentName> getReturnValues() {
    return myReturnValues;
  }

  public static DartControlFlow analyze(PsiElement[] elements) {
    final PsiElement scope = PsiTreeUtil.getTopmostParentOfType(elements[0], DartExecutionScope.class);
    final PsiElement lastElement = elements[elements.length - 1];
    final int lastElementEndOffset = lastElement.getTextOffset() + lastElement.getTextLength();
    final int firstElementStartOffset = elements[0].getTextOffset();

    // find out params
    assert scope != null;
    final LocalSearchScope localSearchScope = new LocalSearchScope(scope);
    final List<DartComponentName> outDeclarations = ContainerUtil.filter(
      DartControlFlowUtil.getSimpleDeclarations(elements, null, false),
      new Condition<DartComponentName>() {
        @Override
        public boolean value(DartComponentName componentName) {
          for (PsiReference usage : ReferencesSearch.search(componentName, localSearchScope, false).findAll()) {
            if (usage.getElement().getTextOffset() > lastElementEndOffset) {
              return true;
            }
          }
          return false;
        }
      });

    // find params
    final DartReferenceVisitor dartReferenceVisitor = new DartReferenceVisitor();
    for (PsiElement element : elements) {
      element.accept(dartReferenceVisitor);
    }
    final List<DartComponentName> inComponentNames = ContainerUtil.filter(
      dartReferenceVisitor.getComponentNames(), new Condition<DartComponentName>() {
      @Override
      public boolean value(DartComponentName componentName) {
        final int offset = componentName.getTextOffset();
        final boolean declarationInElements = firstElementStartOffset <= offset && offset < lastElementEndOffset;
        return !declarationInElements;
      }
    });


    return new DartControlFlow(inComponentNames, outDeclarations);
  }

  public void filterParams(Condition<? super DartComponentName> condition) {
    myParameters = ContainerUtil.filter(myParameters, condition);
  }

  public String getReplaceStatementText(String functionName) {
    final StringBuilder result = new StringBuilder();
    if (!myReturnValues.isEmpty()) {
      final DartComponentName componentName = myReturnValues.iterator().next();
      result.append("var ");
      result.append(componentName.getName());
      result.append(" = ");
    }
    result.append(getSignature(functionName, false));
    return result.toString();
  }

  private void appendFirstReturnTypeName(StringBuilder result) {
    final DartComponentName componentName = myReturnValues.iterator().next();
    final DartClassResolveResult resolveResult = DartResolveUtil.getDartClassResolveResult(componentName);
    final DartClass dartClass = resolveResult.getDartClass();
    if (dartClass != null) {
      result.append(DartPresentableUtil.buildClassText(dartClass, resolveResult.getSpecialization()));
      result.append(" ");
    }
  }

  public String getSignature(String functionName) {
    return getSignature(functionName, true);
  }

  private String getSignature(String functionName, boolean declaration) {
    final StringBuilder result = new StringBuilder();
    if (declaration && !myReturnValues.isEmpty()) {
      appendFirstReturnTypeName(result);
    }
    result.append(functionName);
    result.append("(");
    for (int i = 0; i < myParameters.size(); i++) {
      if (i > 0) result.append(", ");
      DartComponentName componentName = myParameters.get(i);
      final DartClassResolveResult resolveResult = DartResolveUtil.getDartClassResolveResult(componentName);
      final DartClass dartClass = resolveResult.getDartClass();
      if (declaration && dartClass != null) {
        result.append(dartClass.getName()).append(" ");
      }
      result.append(componentName.getName());
    }
    result.append(")");
    return result.toString();
  }

  private static class DartReferenceVisitor extends PsiRecursiveElementVisitor {
    private final Set<DartComponentName> myComponentNames = new THashSet<DartComponentName>();

    public Set<DartComponentName> getComponentNames() {
      return myComponentNames;
    }

    @Override
    public void visitElement(PsiElement element) {
      if (element instanceof DartReference && DartResolveUtil.aloneOrFirstInChain((DartReference)element)) {
        final PsiReference at = element.getContainingFile().findReferenceAt(element.getTextOffset());
        final PsiElement resolve = at == null ? null : at.resolve();
        if (resolve instanceof DartComponentName) {
          myComponentNames.add((DartComponentName)resolve);
        }
      }
      super.visitElement(element);
    }
  }
}