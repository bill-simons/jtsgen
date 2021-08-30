
package dz.jtsgen.processor.jtp.conv;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractTypeVisitor8;

import dz.jtsgen.annotations.TypeScriptExecutable;
import dz.jtsgen.processor.jtp.conv.visitors.JavaTypeConverter;
import dz.jtsgen.processor.jtp.info.TSProcessingInfo;
import dz.jtsgen.processor.model.TSExecutableMemberBuilder;
import dz.jtsgen.processor.model.TSMember;
import dz.jtsgen.processor.model.TSRegularMember;
import dz.jtsgen.processor.model.TSRegularMemberBuilder;
import dz.jtsgen.processor.model.TSTargetType;

class PreserveExecutablesJavaTypeElementExtractingVisitor extends JavaTypeElementExtractingVisitor {

  PreserveExecutablesJavaTypeElementExtractingVisitor(TypeElement typeElementToConvert, TSProcessingInfo visitorParam, JavaTypeConverter javaTypeConverter) {
    super(typeElementToConvert,visitorParam,javaTypeConverter);
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Void notcalled) {
    Element enclosingElement = e.getEnclosingElement();
    if ((enclosingElement == null) || (enclosingElement.getAnnotation(TypeScriptExecutable.class) == null)) {
      // This executable element is not part of an interface/class that is annotated with @TypeScriptExecutable
      // Treat it as a standard @TypeScript annotated class (only bean properties are converted to typescript properties)
      return super.visitExecutable(e,notcalled);
    } else {
      return visitExecutableWorker(e);
    }
  }

  public Void visitExecutableWorker(ExecutableElement e) {
    LOG.fine(() -> String.format("JTExV visiting executable %s", e.toString()));
    final String rawName = e.getSimpleName().toString();  //  nameOfMethod(e).orElse("");
    final String name = mappedName(rawName);
    final boolean isPublic = e.getModifiers().contains(Modifier.PUBLIC);
    final boolean isIgnored = isIgnored(e);
    final boolean isReadOnly = readOnlyAnnotation(e) || readOnlyAnnotation(this.typeElementToConvert);
    final boolean isOptional = optionalAnnotation(e) || optionalAnnotation(this.typeElementToConvert);
    final boolean isInit = rawName.startsWith("<");
    if (!isPublic ||  isIgnored || isInit) return null; // return early for not converting private types
    final TSTargetType returnType = convertTypeMirrorToTsType(e, tsProcessingInfo);
    LOG.fine(() -> "is getter or setter: " + (isPublic ? "public " : " ") + e.getSimpleName() + " -> " + name +"(" + rawName+ ")" + ":" + returnType + " " +(isIgnored?"(ignored)":""));


    final List<TSRegularMember> paramMembers = new ArrayList<>();
    final List<? extends VariableElement> functionParams = e.getParameters();
    for (VariableElement functionParam : functionParams) {
      final String paramName = functionParam.getSimpleName().toString();
      TSMember tsMember = members.get(paramName);
      if(tsMember instanceof TSRegularMember) {
        TSRegularMember paramMember = (TSRegularMember) tsMember;
        paramMembers.add(paramMember);
      } else {
        final TSTargetType paramType = convertTypeMirrorOfMemberToTsType(functionParam, tsProcessingInfo);
        paramMembers.add(TSRegularMemberBuilder.of(paramName, paramType, false, isOptional));
      }
    }

    final TSRegularMember [] parameters = paramMembers.toArray(new TSRegularMember[0]);
    final Optional<String> comment = Optional.ofNullable(this.tsProcessingInfo.getpEnv().getElementUtils().getDocComment(e));
    members.put(name, TSExecutableMemberBuilder
        .of(name, returnType, isReadOnly, isOptional, parameters)
        .withComment(comment)
    );

    extractableMembers.add(name);
    return null;
  }

  protected TSTargetType convertTypeMirrorToTsType(ExecutableElement theElement, TSProcessingInfo tsProcessingInfo) {
    AbstractTypeVisitor8<TSTargetType, Void> visitor = new MirrorTypeToTSConverterVisitor(theElement, tsProcessingInfo, javaTypeConverter);
    return visitor.visit(theElement.getReturnType());
  }

}