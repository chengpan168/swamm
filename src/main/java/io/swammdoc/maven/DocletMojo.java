package io.swammdoc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Created by panwang.chengpw on 2017/11/15.
 */
@Mojo(name = "DocletMojo")
public class DocletMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("doclet 开始。。。");
    }
}
