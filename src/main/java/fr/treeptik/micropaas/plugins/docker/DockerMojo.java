package fr.treeptik.micropaas.plugins.docker;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.HostConfig;

public abstract class DockerMojo extends AbstractMojo {

    protected static final String DEFAULT_URL = "http://localhost:4243";

    protected ContainerConfig containerConfig;
    protected DockerClient dockerClient;

    @Parameter(required = true)
    protected String containerImage;
    
    @Parameter(required = true)
    protected String containerName;
    
    @Parameter(defaultValue = DEFAULT_URL)
    protected String urlDockerManager;

    @Parameter
    protected String containerId;
    
    @Parameter
    protected String[] cmds;
    
    @Parameter
    protected String[] exposedPorts;
    

    protected void createContainer() throws DockerException {

        final ContainerCreateResponse response = getDockerClient().createContainer(getContainerConfig(), containerName);
        getLog().info("Created container with name " + containerName + " and ID " + response.getId());
    }

    protected void startContainer() throws DockerException {
    	
        
        HostConfig hostConfig = new HostConfig();
        hostConfig.setPublishAllPorts(true);
        
        getDockerClient().startContainer(containerName, hostConfig);
        
    }

    protected void stopContainer() throws DockerException {
        getDockerClient().stopContainer(containerName);
    }

    protected void removeContainer() throws DockerException {
        getDockerClient().removeContainer(containerName);
    }


    protected void restartContainer() throws DockerException {
        getDockerClient().restart(containerName, 0);
    }

    protected Boolean isContainerExist(String containerName)throws DockerException {
    	List<Container> listContainers = getDockerClient().listContainers(true);
    	for (Container container : listContainers) {
    		List<String> containerNames = Arrays.asList(container.getNames());
    		if (containerNames.contains("/"+containerName)){
    			return true;
    		}
		}
    	
    	return false;
    }

    protected Boolean isContainerUp(String containerName)throws DockerException {
    	ContainerInspectResponse inspectContainer = getDockerClient().inspectContainer(containerName);
    	return inspectContainer.getState().running;
    	
    }

    
    public ContainerConfig getContainerConfig() {
        if (containerConfig == null) {
            containerConfig = new ContainerConfig();
            if (getCmds() != null)  containerConfig.setCmd(getCmds());
            if (getContainerImage() != null) containerConfig.setImage(getContainerImage());
        }
        return containerConfig;
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
