package fr.treeptik.micropaas.plugins.docker;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DockerDeployMojo extends DockerMojo {

    /**
     * Méthode principale du MOJO
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        try {
            String absolutePathWarFile = getAbsolutePathWarFile();
            getLog().debug("absolutePathWarFile : " + absolutePathWarFile);
            if (absolutePathWarFile != null) {
                String warFileName = absolutePathWarFile.substring(absolutePathWarFile.lastIndexOf("/")+1);
                String applicationName = warFileName.substring(0, warFileName.lastIndexOf("."));
                getLog().debug("warFileName : " + warFileName);
                File file = new File(absolutePathWarFile);
                String sshForwardedPort = getForwardedPort("22");
                String ipDocker = getIpDocker();
                // Do not remove the final slash
                sendFile(file, sshForwardedPort, ipDocker, "/deploy/");
                executeShell(ipDocker, sshForwardedPort, "/bin/sh /deploy.sh "  + warFileName, null);

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

