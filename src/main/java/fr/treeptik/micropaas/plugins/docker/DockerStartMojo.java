package fr.treeptik.micropaas.plugins.docker;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import com.kpelykh.docker.client.DockerException;

@Mojo(name = "startContainer")
public class DockerStartMojo extends DockerMojo {

    public void execute() throws MojoExecutionException {
        try {
        	getLog().info("Trying to start container with name " + containerName );
        	if ( ! this.isContainerUp(containerName)){
        		startContainer();
        	}else{
        		getLog().warn("+-- Container " + containerName + " already started ");
        	}
        	getLog().info("Container with name " + containerName + " has been started");
        } catch (DockerException e) {
            throw new MojoExecutionException("Error starting container " +  containerName, e);
        }
    }
}
