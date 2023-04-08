package edu.puj.samplers;

import edu.puj.client.Client;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.Serializable;

public class ClientSampler extends AbstractJavaSamplerClient implements Serializable {

    @Override
    public void setupTest(JavaSamplerContext javaSamplerContext) {
        super.setupTest(javaSamplerContext);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();

        try {
            // Start a new sample
            result.sampleStart();
            // Create a new Client
            long start = System.currentTimeMillis();
            Client.main(new String[]{
                    "-p", javaSamplerContext.getParameter("serverPort"),
                    "-a", javaSamplerContext.getParameter("serverAddress"),
                    "-f", javaSamplerContext.getParameter("clientFile"),
                    "-r", javaSamplerContext.getParameter("clientRetries"),
                    "-t", javaSamplerContext.getParameter("clientTimeout"),
            });
            long stop = System.currentTimeMillis();
            // Set successfull request
            result.setSuccessful(true);
            result.setResponseMessage(String.format("Finalized in: %d ms", (stop - start)));
        } catch (Exception e) {
            e.printStackTrace();
            result.setResponseMessage(e.toString());
            result.setSuccessful(false);
        } finally {
            // Sample failed
            result.sampleEnd();
        }

        return result;
    }

    @Override
    public void teardownTest(JavaSamplerContext javaSamplerContext) {
        super.teardownTest(javaSamplerContext);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("serverAddress", "localhost");
        arguments.addArgument("serverPort", "5555");
        arguments.addArgument("clientFile", "");
        arguments.addArgument("clientRetries", "3");
        arguments.addArgument("clientTimeout", "1500");
        return arguments;
    }
}
