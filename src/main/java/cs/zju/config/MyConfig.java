package cs.zju.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyConfig {
    private static final Configuration config = getConfiguration();
    private static final boolean removeTestFiles = getBooleanProperty("remove_test");
    private static final String rootPath = getStringProperty("root_path");

    private static Configuration getConfiguration(){
        return getConfiguration("config.properties");
    }

    private static Configuration getProjectConfiguration(String project){
        if (project.equals("tmp"))
            return null;
        return getConfiguration("project_configs/" + project + ".config.properties");
    }

    private static Configuration getConfiguration(String fileName){
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(fileName)
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

            return builder.getConfiguration();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getStringProperty(String propertyName){
        return config.getString(propertyName);
    }

    private static boolean getBooleanProperty(String propertyName){
        return config.getBoolean(propertyName);
    }

    private static int getIntegerProperty(String propertyName){
        return config.getInt(propertyName);
    }

    private static String[] getStringArrayProperty(String propertyName){
        return config.getStringArray(propertyName);
    }

    public static boolean ifRemoveTestFiles(){
        return removeTestFiles;
    }

    public static String getRootPath(){
        return rootPath;
    }

    public static String getCloneUrl(String projectName){
        try {
            Configuration conf = getProjectConfiguration(projectName);
            return conf.getString("clone_url");
        } catch (Exception e){
            return "";
        }
    }

    private static String getCommitUrlPrefix(String projectName){
        Configuration conf = getProjectConfiguration(projectName);
        return conf.getString("github_commit_url_prefix");
    }

    public static String getCommitUrl(String projectName, String commitId){
        return getCommitUrlPrefix(projectName) + commitId;
    }

}
