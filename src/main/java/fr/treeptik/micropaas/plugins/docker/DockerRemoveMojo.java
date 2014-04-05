package fr.treeptik.micropaas.plugins.docker;

import com.kpelykh.docker.client.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "removeContainer")
public class DockerRemoveMojo extends DockerMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	getLog().info("Trying to remove container with name " + containerName);
        	if (isContainerExist(containerName)){
        		removeContainer();
        	}else{
        		getLog().warn("+-- Container " + containerName + " does not exist ");
        	}
        	getLog().info("Container with name " + containerName + " has been remove");
        } catch (DockerException e) {
            throw new MojoExecutionException("Error while trying to remove container " +  containerName, e);
        }
    }
}
