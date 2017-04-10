package edu.utdallas.project3.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Node;


public class MutexConfig {
    private static final Logger logger = LogManager.getLogger(MutexConfig.class.getName());
    
    public static final String CONFIG_FILE_NAME = "config.file.name";
    public static final String CONFIG_FILE_DIRECTORY ="config.file.directory";
    public static final String NEIGHBORS = "neighbors";
    
    public static final String NUM_NODES = "nums.nodes";                
    public static final String MEAN_INTER_REQUEST_DELAY = "mean.inter.request.delay";       
    public static final String MEAN_CS_EXECUTION = "mean.cs.execution";       
    public static final String NUM_REQUEST = "num.request";    
    
    
    private String configFileName;
    private String configFileDirectory;
    
    private int myId;
    private int numberOfNodes;
    private int meanInterRequestDelay;
    private int meanCSExecution;
    private int numberOfRequest;
    
    private List<Node> neighbors;
    
    
    public MutexConfig(int myId){
        configFileName = "";
        configFileDirectory = "";
        
        this.myId = myId;
        numberOfNodes = 0;
        meanInterRequestDelay = 0;
        meanCSExecution = 0;
        numberOfRequest = 0;
        
        neighbors = new ArrayList<>();
    }
    
    public void loadConfigFromRelativePath(String relativePath){
        Path file = Paths.get(relativePath).toAbsolutePath();
        
        // System.out.println(file.toString());
        loadConfigFromAbsolutePath(file.toString());
    }
    
    public void loadConfigFromAbsolutePath(String absolutePath) {
        Path file = Paths.get(absolutePath);
        
        // Store file name
        configFileName = file.getFileName().toString();
        configFileDirectory = file.getParent().toString();
        

        logger.info("{} {}", CONFIG_FILE_NAME, configFileName);
        logger.info("{} {}", CONFIG_FILE_DIRECTORY, configFileDirectory);
        
        Charset charset = Charset.forName("UTF-8");
        try {
            loadConfigurationFromBufferedReader(Files.newBufferedReader(file, charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadConfigurationFromInputStream(InputStream in, String fileName){
        logger.info("{} {}", CONFIG_FILE_NAME, fileName);
        loadConfigurationFromBufferedReader(
                new BufferedReader(
                        new InputStreamReader(in)));
    }

    public void loadConfigurationFromBufferedReader(BufferedReader reader) {
        List<Node> hosts = new LinkedList<>();
        StringBuilder logger = new StringBuilder();
        logger.append(String.format("[Node %d] [Config Loader] :\n", myId));
        
        try {
            String line = null;
            int n = 0;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","");  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)             // Skip empty lines
                    continue;
                String[] params = line.split("\\s+"); // Split to read parameters
                /*
                 * 0 NUM_NODES,                     // "Number of Nodes";
                 * 1 MEAN_INTER_REQUEST_DELAY,      // "Mean value for inter-request delay";
                 * 2 MEAN_CS_EXECUTION,             // "Mean value for cs-execution time";
                 * 3 NUM_REQUEST,                   // "Number of requests each node should generate";
                 */
                numberOfNodes = Integer.parseInt(params[0]);
                meanInterRequestDelay = Integer.parseInt(params[1]);
                meanCSExecution = Integer.parseInt(params[2]);
                numberOfRequest = Integer.parseInt(params[3]);
                
                logger.append(String.format("%s = %s\n", NUM_NODES, params[0]));
                logger.append(String.format("%s = %s\n", MEAN_INTER_REQUEST_DELAY, params[1]));
                logger.append(String.format("%s = %s\n", MEAN_CS_EXECUTION, params[2]));
                logger.append(String.format("%s = %s\n", NUM_REQUEST, params[3]));
                break;
            }

            n = numberOfNodes;
            
            // Load host list.
            while (n != 0 && (line = reader.readLine()) != null) {
                line = line.replaceAll("#.*","").trim();  // Erase everything after a comment.
                line = line.trim();                // Trim leading and trailing spaces.
                if(line.length() == 0)
                    continue;
                
                //System.out.println(String.format("[host info: %s]", line));
                String[] hostInfo = line.split("\\s+");
                
                // hostInfo[0] - node id, hostInfo[1] - host addr, hostInfo[2] - host port
                Node host = new Node(Integer.parseInt(hostInfo[0]), hostInfo[1] + ".utdallas.edu", hostInfo[2]);
                if (host.getNodeId() != myId)
                    hosts.add(host);
                n--;
            }
            validateConfigurationFile(n);
            
            doConfigure(hosts);
            
            System.out.println(logger.toString());
            
        } catch (IOException e){
            e.printStackTrace();
        }
        
    }
    
    public void doConfigure(List<Node> neighbors){
        Collections.sort(neighbors);
        neighbors = new ArrayList<>(neighbors);
        this.neighbors = neighbors;
    }
    
    private void validateConfigurationFile(int n) throws IOException{
        if(n != 0){
            throw new IOException("Insufficent valid lines in config file.");
        }
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFileDirectory() {
        return configFileDirectory;
    }

    public void setConfigFileDirectory(String configFileDirectory) {
        this.configFileDirectory = configFileDirectory;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getMeanInterRequestDelay() {
        return meanInterRequestDelay;
    }

    public void setMeanInterRequestDelay(int meanInterRequestDelay) {
        this.meanInterRequestDelay = meanInterRequestDelay;
    }

    public int getMeanCSExecution() {
        return meanCSExecution;
    }

    public void setMeanCSExecution(int meanCSExecution) {
        this.meanCSExecution = meanCSExecution;
    }

    public int getNumberOfRequest() {
        return numberOfRequest;
    }

    public void setNumberOfRequest(int numberOfRequest) {
        this.numberOfRequest = numberOfRequest;
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Node> neighbors) {
        this.neighbors = neighbors;
    }

    public static MutexConfig loadFromConfigurationFile(String configurationFilePath, int myId) {
        MutexConfig config = new MutexConfig(myId);
        config.loadConfigFromAbsolutePath(configurationFilePath);
        return config;
    }
    public static MutexConfig loadFromConfigurationFile(InputStream in, String fileName, int myId) {
        MutexConfig config = new MutexConfig(myId);
        config.loadConfigurationFromInputStream(in, fileName);
        return config;
    }
}
