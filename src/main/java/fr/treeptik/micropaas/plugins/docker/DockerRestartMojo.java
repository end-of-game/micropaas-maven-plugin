package fr.treeptik.micropaas.plugins.docker;

import com.kpelykh.docker.client.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name= "restartContainer")
public class DockerRestartMojo extends DockerMojo{

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	getLog().info("Trying to restart container " + containerName );
            restartContainer();
            getLog().info("Container with name " + containerName + " has been restarted");
        } catch (DockerException e) {
            throw new MojoExecutionException("Error restarting the container " + containerName, e);
        }
    }
}
