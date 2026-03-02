//package com.toannq.core.processor;
//
//import com.google.auto.service.AutoService;
//import com.squareup.javapoet.MethodSpec;
//import com.toannq.core.annotation.DbProvider;
//import com.toannq.core.db.DbMetadataProvider;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.lang.model.element.*;
//import javax.tools.Diagnostic;
//import java.util.Set;
//
//@AutoService(Processor.class)
//public class DbProviderProcessor extends AbstractProcessor {
//  @Override
//  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//    if (annotations.isEmpty()) return false;
//    var initMethodBuilder = MethodSpec.methodBuilder("init")
//        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
//    for (var element : roundEnv.getElementsAnnotatedWith(DbProvider.class)) {
//      if (!(element instanceof TypeElement typeElement)) continue;
//      if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
//        error(typeElement, "Class %s must be public!", typeElement.getSimpleName());
//        continue;
//      }
//      if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
//        error(typeElement, "Class %s không được để abstract!", typeElement.getSimpleName());
//        continue;
//      }
//      boolean hasNoArgsResource = typeElement.getEnclosedElements().stream()
//          .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
//          .anyMatch(e -> ((ExecutableElement) e).getParameters().isEmpty());
//      if (!hasNoArgsResource) {
//        error(typeElement, "Class %s thiếu constructor không tham số. Framework không thể khởi tạo tự động được!", typeElement.getSimpleName());
//        continue;
//      }
//      if (!isSubtypeOf(typeElement, DbMetadataProvider.class)) {
//        error(typeElement, "Class %s phải implement/extend MetadataProvider!", typeElement.getSimpleName());
//        continue;
//      }
//      var dbType = typeElement.getAnnotation(DbProvider.class).value();
//      initMethodBuilder.addStatement("$T.register($S, new $L())", ProviderRegistry.class, dbType, typeElement.getQualifiedName().toString());
//    }
//
//    // ... (Đoạn ghi file JavaFile.builder giống như trước) ...
//    return true;
//  }
//
//  private void error(Element e, String msg, Object... args) {
//    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e); // Truyền 'e' vào để khi người dùng click vào lỗi, nó nhảy thẳng tới dòng code sai
//  }
//
//  private boolean isSubtypeOf(TypeElement element, Class<? extends DbMetadataProvider> superClass) {
//    var superType = processingEnv.getElementUtils().getTypeElement(superClass).asType();
//    return processingEnv.getTypeUtils().isAssignable(element.asType(), superType);
//  }
//}
