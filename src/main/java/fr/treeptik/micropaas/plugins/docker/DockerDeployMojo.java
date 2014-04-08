package fr.treeptik.micropaas.plugins.docker;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.FilenameFilter;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DockerDeployMojo extends DockerMojo {

    /**
     * Méthode principale du MOJO
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        try {
            
        	if ( ! isContainerExist(containerName)){
        		createContainer();
        	}
        	
        	if (! isContainerUp(containerName)){
        		startContainer();
        		// Wait container / service start
        		Thread.sleep(2000);
        	}
        	
        	
        	String absolutePathWarFile = getAbsolutePathWarFile();
            getLog().debug("absolutePathWarFile : " + absolutePathWarFile);
            if (absolutePathWarFile != null) {
                String warFileName = absolutePathWarFile.substring(absolutePathWarFile.lastIndexOf("/")+1);
                
                getLog().debug("warFileName : " + warFileName);
                File file = new File(absolutePathWarFile);
                // Test if file exist. Maybe a plugin change the file name (ex : maven-war-plugin)
                // And search for Ear or War
                if ( ! file.exists()){
                	getLog().warn("File not found : " + absolutePathWarFile);
                	getLog().warn("Search other files (ear and war) in " + getAbsoluteTargetDirectory());
                	File directory = new File(getAbsoluteTargetDirectory());
                	File[] files = directory.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File file, String name) {
							return (name.endsWith(".ear") || name.endsWith(".war")) ? true : false;
						}
					});
                	
                	if (files.length > 1){
                		getLog().warn("Found more than one application, search ear file");
                		for (File f : files){
                			if (f.getName().endsWith(".ear")){
                				getLog().info("Found pplication : " + f.getName());
                				file = f;
                				break;
                			}
                		}
                	} else {
                		file = files[0];
                		getLog().info("Found application : " + file.getName());
                	}
                	
                	
                	
                	 
                	
                }
                
                String applicationName = file.getName().substring(0, file.getName().lastIndexOf("."));
                
                String sshForwardedPort = getForwardedPort("22");
                String ipDocker = getIpDocker();
                // Do not remove the final slash
                sendFile(file, sshForwardedPort, ipDocker, "/deploy/");
                // All caontainer images must have a script 'deploy.sh'
                executeShell(ipDocker, sshForwardedPort, "/bin/sh /deploy.sh "  + file.getName(), null);

                String tomcatForwardPort = getForwardedPort("8080");
                StringBuilder msgInfo = new StringBuilder(1024);
                msgInfo.append("\n******************************************\n");
                msgInfo.append("******************************************\n");
                msgInfo.append("********** APPLICATION ACCESS ************\n");
                msgInfo.append("******************************************\n");
                msgInfo.append("******************************************\n");
                msgInfo.append("\nURL : http://").append(ipDocker).append(":").append(tomcatForwardPort)
                        .append("/").append(applicationName).append("\n");
                getLog().info(msgInfo);

            } else {
                getLog().error("Cannot find the war file from this project to deploy it.");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error deploying application " + getContainerImage(), e);
        }
    }

}

