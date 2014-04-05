package fr.treeptik.micropaas.plugins.docker;

import com.kpelykh.docker.client.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stopContainer")
public class DockerStopMojo extends DockerMojo{

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	getLog().info("Trying to stop container with name " + containerName );
        	if (isContainerUp(containerName)){
        		stopContainer();
        	} else {
        		getLog().warn("+-- Container " + containerName + " already stopped ");
        	}
        	getLog().info("Container with name " + containerName + " has been stoped");
        } catch (DockerException e) {
            throw new MojoExecutionException("Error while trying to stop container " + containerName, e);
        }
    }
}
