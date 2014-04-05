package fr.treeptik.micropaas.plugins.docker;

import com.kpelykh.docker.client.DockerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "startContainer", defaultPhase=LifecyclePhase.PREPARE_PACKAGE)
public class DockerStartMojo extends DockerMojo {

    public void execute() throws MojoExecutionException {
        try {
            startContainer();
        } catch (DockerException e) {
            throw new MojoExecutionException("Error starting container image " +  getContainerImage(), e);
        }
    }
}
