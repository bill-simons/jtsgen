package dz.jtsgen.processor.jtp.conv;


import java.util.Optional;
import javax.lang.model.util.SimpleElementVisitor8;

import dz.jtsgen.processor.jtp.conv.visitors.JavaTypeConverter;
import dz.jtsgen.processor.jtp.conv.visitors.TSAVisitor;
import dz.jtsgen.processor.jtp.info.TSProcessingInfo;
import dz.jtsgen.processor.model.TSType;

public class PreserveExecutablesTypeScriptAnnotationProcessor extends TypeScriptAnnotationProcessor {

  public PreserveExecutablesTypeScriptAnnotationProcessor(TSProcessingInfo processingInfo) {
    super(processingInfo, new PreserveExecutablesJavaTypeConverter(processingInfo));
  }

  protected SimpleElementVisitor8<Optional<TSType>, JavaTypeConverter> makeVisitor() {
    return new TSAVisitor();
  }
}