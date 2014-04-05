package fr.treeptik.micropaas.plugins.docker;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.HostConfig;

public abstract class DockerMojo extends AbstractMojo {

    private static final String DEFAULT_URL = "http://localhost:4243";

    private static final ThreadLocal<String> tlContainerId = new ThreadLocal<String>();

    private ContainerConfig containerConfig;
    private DockerClient dockerClient;

    @Parameter(required = true)
    private String containerImage;
    
    @Parameter(required = true)
    private String containerName;
    
    @Parameter(defaultValue = DEFAULT_URL)
    private String urlDockerManager;

    @Parameter
    private String containerId;
    
    @Parameter
    private String[] cmds;
    
    @Parameter
    private String[] exposedPorts;
    


    /**
     * Creates a container in Docker and stores the container id in a ThreadLocal variable
     * so that it can be accessed by other goals of the plugin.
     *
     * @throws DockerException
     */
    protected void createContainer() throws DockerException {
        getLog().debug(String.format("Creating new container"));

        final ContainerCreateResponse response = getDockerClient().createContainer(getContainerConfig(), containerName);
        final String containerId = response.getId();
        getLog().info(String.format("Created container with id %s", containerId));
        DockerMojo.tlContainerId.set(containerId);
    }

    protected void startContainer() throws DockerException {
    	
        getLog().debug(String.format("Trying to start container %s", getContainerId()));
        validateContainerId();
        
        HostConfig hostConfig = new HostConfig();
        hostConfig.setPublishAllPorts(true);
        
        getDockerClient().startContainer(getContainerId(), hostConfig);
    }

    protected void stopContainer() throws DockerException {
        getLog().debug(String.format("Trying to stop container %s", getContainerId()));
        validateContainerId();
        getDockerClient().stopContainer(getContainerId());
    }

    protected void removeContainer() throws DockerException {
        getLog().debug(String.format("Trying to remove container %s", getContainerId()));
        validateContainerId();
        final String containerId = this.getContainerId();
        getDockerClient().removeContainer(containerId);
        DockerMojo.tlContainerId.set(null);
        getLog().info(String.format("Container %s has been removed", containerId));
    }


    protected void restartContainer() throws DockerException {
        getLog().debug(String.format("Trying to restart container %s", getContainerId()));
        validateContainerId();
        final String containerId = this.getContainerId();
        getDockerClient().restart(containerId, 10000);
        getLog().info(String.format("Container %s has been restarted", getContainerId()));
    }


    private void validateContainerId() {
        if (getContainerId() == null) throw new IllegalStateException("There isn't any container id set.");
    }

    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           Getter and Setters                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ContainerConfig getContainerConfig() {
        if (containerConfig == null) {
            containerConfig = new ContainerConfig();
            if (getCmds() != null)  containerConfig.setCmd(getCmds());
            if (getContainerImage() != null) containerConfig.setImage(getContainerImage());
            
            
            getLog().debug("Container configuration: " + containerConfig.toString());
        }
        return containerConfig;
    }

    public static String getThreadLocalContainerId() {
        return DockerMojo.tlContainerId.get();
    }

    public String getContainerId() {
        return DockerMojo.tlContainerId.get() != null ? DockerMojo.tlContainerId.get() : containerId;
    }

    public String getContainerImage() {
        return containerImage;
    }

    private DockerClient getDockerClient() {
        if (dockerClient == null) dockerClient = new DockerClient(urlDockerManager);
        return dockerClient;
    }

    public void setUrl(String url) {
        this.urlDockerManager = url;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String[] getCmds() {
        return cmds;
    }

    public void setCmds(String[] cmds) {
        this.cmds = cmds;
    }

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String[] getExposedPorts() {
		return exposedPorts;
	}

	public void setExposedPorts(String[] exposedPorts) {
		this.exposedPorts = exposedPorts;
	}

  
}
