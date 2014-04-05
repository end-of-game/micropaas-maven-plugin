package fr.treeptik.micropaas.plugins.docker;

import com.kpelykh.docker.client.DockerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "createContainer", defaultPhase=LifecyclePhase.VALIDATE)
public class DockerCreateMojo extends DockerMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createContainer();
        } catch (DockerException e) {
            throw new MojoExecutionException("Error while trying to remove container " + getContainerId(), e);
        }
    }
}
