package fr.treeptik.micropaas.plugins.docker;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import com.kpelykh.docker.client.DockerException;

@Mojo(name = "reInitContainer")
public class DockerReInitMojo extends DockerMojo {

    public void execute() throws MojoExecutionException {
        try {
        	getLog().info("Trying to re-init (remove/create) container with name " + containerName);
        	if (this.isContainerExist(containerName)){
        		if (this.isContainerUp(containerName)){
        			stopContainer();
        		}
            	removeContainer();
            	createContainer();
        	}
        	getLog().info("Container  with name " + containerName + " has been re-init (remove/create) ");
        } catch (DockerException e) {
            throw new MojoExecutionException("Error re-init container image " +  containerName, e);
        }
    }
}
