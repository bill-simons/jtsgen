package dz.jtsgen.processor.renderer;

import dz.jtsgen.processor.model.TypeScriptModel;
import dz.jtsgen.processor.nsmap.NameSpaceMapperFactory;
import dz.jtsgen.processor.nsmap.NameSpaceModelMapper;
import dz.jtsgen.processor.renderer.model.TypeScriptRenderModel;
import dz.jtsgen.processor.renderer.module.ModuleGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.logging.Logger;


public final class TSRenderer {

    private static Logger LOG = Logger.getLogger(TSRenderer.class.getName());

    private final TypeScriptModel model;
    private final NameSpaceModelMapper nameSpaceModelMapper;
    private final ProcessingEnvironment env;

    public TSRenderer(ProcessingEnvironment env, TypeScriptModel model) {
        this.env=env;
        this.model=model;
        this.nameSpaceModelMapper = NameSpaceMapperFactory.createNameSpaceMapper(model.getModuleInfo());
    }


    public void writeFiles() {
        LOG.finest("-- Name Space Mapper --");
        TypeScriptModel mappedModel = nameSpaceModelMapper.mapNameSpacesOfModel(this.model);
        TypeScriptRenderModel renderModel = new TypeScriptRenderModel(mappedModel);
        LOG.finest("-- Renderer --");
        new ModuleGenerator(renderModel, env).writeModule();
    }
}
