package fr.treeptik.micropaas.plugins.docker;

import com.jcraft.jsch.*;
import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.*;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

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
	private String databaseName;
    
    @Parameter
    protected String containerId;

    
    @Parameter
    protected String[] exposedPorts;

    protected String getAbsolutePathWarFile() {
        MavenProject mavenProject = (MavenProject) getPluginContext().get("project");
        String directory = mavenProject.getBuild().getDirectory();
        String finalName = mavenProject.getBuild().getFinalName();
        String packaging =  mavenProject.getPackaging();
        String fullPath = directory + "/" + finalName + "." + packaging;
        getLog().debug("absolutePathWarFile : " + fullPath);
        return fullPath;
    }

    protected String getAbsoluteTargetDirectory() {
        MavenProject mavenProject = (MavenProject) getPluginContext().get("project");
        return mavenProject.getBuild().getDirectory();
    }
    
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
            
            // All caontainer images must have a script 'start-service.sh'
            String[] cmd = {"/bin/sh", "/start-service.sh", databaseName};
            containerConfig.setCmd(cmd);
            
            if (getContainerImage() != null) containerConfig.setImage(getContainerImage());
        }
        return containerConfig;
    }


    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    private DockerClient getDockerClient() {
        if (dockerClient == null) dockerClient = new DockerClient(urlDockerManager);
        return dockerClient;
    }

    public void setUrl(String url) {
        this.urlDockerManager = url;
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

    protected int executeShell(String ipDocker, String sshPort, String command, Map<String, String> configShell) throws Exception {

        Session session = null;
        Channel channel = null;
        Map<String, String> map = new HashMap<String, String> ();
        InputStream in = null;
        int exitCode;

        try {
            session = this.getSession("root", "root", ipDocker, sshPort);
            channel = session.openChannel("exec");

            ((ChannelExec) channel).setCommand(command);
            channel.connect();

            in = channel.getInputStream();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        System.out.print(new String(tmp, 0, i));
                        break;
                    }
                    map.put("Line " + i, new String(tmp, 0, i));
                    String str = new String(tmp, 0, i);
                    // displays the output of the command executed.
                    getLog().debug(str);
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {

                        int i = in.read(tmp, 0, 1024);
                        System.out.print(new String(tmp, 0, i));
                    }
                    exitCode = channel.getExitStatus();
                    System.out.println("exit-status: " + exitCode);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignore) {
                }
            }
            getLog().info("command " + command + " execute correctly");

            channel.disconnect();
            session.disconnect();
            in.close();

        } catch (Exception e) {
            throw new Exception("Error Docker I/O", e);
        } finally {
            try {
                if (in != null ) in.close();
            } catch(Exception ignore) {
            }
        }
        return exitCode;
    }

    protected Session getSession(String userName, String password,
                               String dockerIP, String sshPort) throws JSchException {

        getLog().debug("parameters - IP : " + dockerIP + ", port : "
                + sshPort + ", username : " + userName + ", password : "
                + password);

        JSch jSch = new JSch();
        Session session = jSch.getSession(userName, dockerIP,
                Integer.parseInt(sshPort));
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(password);
        session.connect();
        return session;
    }

    /**
     * Extract the IP from the input
     * @return
     */
    protected String getIpDocker() {
        String ipExtracted = null;
        String urlDocker = urlDockerManager;
        if (urlDocker != null) {
            if (urlDocker.contains("https://")) { ipExtracted = urlDocker.substring(8); }
            if (urlDocker.contains("http://")) { ipExtracted = urlDocker.substring(7); }
            int indexOfPort = ipExtracted.indexOf(":");
            if (indexOfPort != -1) {
                ipExtracted = ipExtracted.substring(0, indexOfPort);
            }
        }
        return ipExtracted;
    }


    protected String getForwardedPort(String portRequired) throws Exception {
        ContainerInspectResponse response = getDockerClient().inspectContainer(containerName);
        Map<String,Ports.Port> ports = response.getNetworkSettings().ports.getAllPorts();
        String forwardedPort = "UNDEF";
        for (String key : ports.keySet()) {
            Ports.Port p = ports.get(key);
            if(portRequired.equals(p.getPort())){
                forwardedPort = p.getHostPort();
                break;
            }
        }
        return forwardedPort;
    }

    protected void sendFile(File file, String sshPort,
                         String dockerManagerIP, String destParentDirectory)
            throws Exception {

        try {

            Channel channel = this.getSession("root", "root", dockerManagerIP,
                    sshPort).openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(new FileInputStream(file), destParentDirectory
                    + file.getName(), ChannelSftp.OVERWRITE);
            getLog().info("File send correctly");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error during file copying");
        }
    }

}
