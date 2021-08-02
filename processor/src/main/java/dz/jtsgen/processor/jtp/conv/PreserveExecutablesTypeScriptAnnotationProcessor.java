package dz.jtsgen.processor.jtp.conv;


import java.util.Optional;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.SimpleElementVisitor8;

import dz.jtsgen.processor.helper.Sets;
import dz.jtsgen.processor.jtp.conv.visitors.JavaTypeConverter;
import dz.jtsgen.processor.jtp.conv.visitors.TSAVisitor;
import dz.jtsgen.processor.jtp.info.TSProcessingInfo;
import dz.jtsgen.processor.model.TSType;

import static dz.jtsgen.processor.jtp.helper.RoundEnvHelper.getFilteredTypeScriptExecutableElements;

public class PreserveExecutablesTypeScriptAnnotationProcessor extends TypeScriptAnnotationProcessor {

  public PreserveExecutablesTypeScriptAnnotationProcessor(TSProcessingInfo processingInfo) {
    super(processingInfo, new PreserveExecutablesJavaTypeConverter(processingInfo));
  }

  @Override
  public void processAnnotations(RoundEnvironment roundEnv) {
    this.processElements(
        Sets.union(
            this.processingInfo.additionalTypesToConvert(),
            getFilteredTypeScriptExecutableElements(roundEnv)
        ));
  }

  protected SimpleElementVisitor8<Optional<TSType>, JavaTypeConverter> makeVisitor() {
    return new TSAVisitor();
  }
}