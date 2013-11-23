package ch.codez.souvenirbooth.jai.sample;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.ImageLayout;
import javax.media.jai.OperationDescriptorImpl;

// A single class that is both an OperationDescriptor and
// a RenderedImageFactory along with the one OpImage it is
// capable of creating.  The operation implemented is a variation
// on threshold, although the code may be used as a template for
// a variety of other point operations.
public class SampleDescriptor extends OperationDescriptorImpl implements
        RenderedImageFactory {
    // The resource strings that provide the general documentation
    // and specify the parameter list for the "Sample" operation.
    private static final String[][] resources = {
            { "GlobalName", "Sample" },
            { "LocalName", "Sample" },
            { "Vendor", "com.mycompany" },
            { "Description", "A sample operation that thresholds source pixels" },
            { "DocURL", "http://www.mycompany.com/SampleDescriptor.html" },
            { "Version", "1.0" }, { "arg0Desc", "param1" },
            { "arg1Desc", "param2" } };

    // The parameter names for the "Sample" operation. Extenders may
    // want to rename them to something more meaningful. 
    private static final String[] paramNames = { "param1", "param2" };

    // The class types for the parameters of the "Sample" operation.  
    // User defined classes can be used here as long as the fully 
    // qualified name is used and the classes can be loaded.
    private static final Class[] paramClasses = { java.lang.Integer.class,
            java.lang.Integer.class };

    // The default parameter values for the "Sample" operation
    // when using a ParameterBlockJAI.
    private static final Object[] paramDefaults = { new Integer(0),
            new Integer(255) };

    // Constructor.
    public SampleDescriptor() {
        super(resources, 1, paramClasses, paramNames, paramDefaults);
    }

    // Creates a SampleOpImage with the given ParameterBlock if the 
    // SampleOpImage can handle the particular ParameterBlock.
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        if (!validateParameters(paramBlock)) {
            return null;
        }
        
        ImageLayout l = new ImageLayout();
        /*
        l.setColorModel(new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
                false,
                false,
                Transparency.OPAQUE,
                DataBuffer.TYPE_INT));
        */        
        return new SampleOpImage(paramBlock.getRenderedSource(0),
                l, (Integer) paramBlock.getObjectParameter(0),
                (Integer) paramBlock.getObjectParameter(1));
    }

    // Checks that all parameters in the ParameterBlock have the 
    // correct type before constructing the SampleOpImage
    public boolean validateParameters(ParameterBlock paramBlock) {
        for (int i = 0; i < this.getNumParameters(); i++) {
            Object arg = paramBlock.getObjectParameter(i);
            if (arg == null) {
                return false;
            }
            if (!(arg instanceof Integer)) {
                return false;
            }
        }
        return true;
    }
}
