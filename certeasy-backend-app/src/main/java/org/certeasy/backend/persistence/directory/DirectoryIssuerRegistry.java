package org.certeasy.backend.persistence.directory;

import org.certeasy.CertEasyContext;
import org.certeasy.Certificate;
import org.certeasy.backend.CertConstants;
import org.certeasy.backend.issuer.CertIssuer;
import org.certeasy.backend.persistence.IssuerDatastore;
import org.certeasy.backend.persistence.IssuerRegistry;
import org.certeasy.backend.persistence.IssuerRegistryException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


@ApplicationScoped
public class DirectoryIssuerRegistry implements IssuerRegistry {

    private File dataDirectory;
    private CertEasyContext context;
    private Map<String, CertIssuer> cache = new HashMap<>();
    private boolean scanned = false;
    private static final Logger LOGGER = Logger.getLogger(DirectoryIssuerRegistry.class);

    public DirectoryIssuerRegistry(@ConfigProperty(name = CertConstants.DATA_DIRECTORY_CONFIG) String dataDirectory, CertEasyContext context){
        if(dataDirectory==null || dataDirectory.isEmpty())
            throw new IllegalArgumentException("dataDirectory path cannot be null or empty");
        this.dataDirectory = new File(dataDirectory);
        if(!this.dataDirectory.exists() || !this.dataDirectory.isDirectory())
            throw new IllegalArgumentException("dataDirectory path must point to existing directory");
        this.context = context;
    }

    @Override
    public Collection<CertIssuer> list() throws IssuerRegistryException {
        this.scanIfNotYet();
        return cache.values();
    }

    private void scanIfNotYet() throws IssuerRegistryException {
        if(!scanned)
            this.scanDirectory();
    }

    private void scanDirectory(){
        LOGGER.info("Scanning data directory...");
        File[] files = this.dataDirectory.listFiles(it -> it.isDirectory() && !it.getName().equals(".") &&
                        !it.getName().equals(".."));
        if(files==null)
            return;
        for(File issuerDirectory : files){
            IssuerDatastore datastore = new DirectoryIssuerDatastore(issuerDirectory,context);
            CertIssuer issuer = new CertIssuer(issuerDirectory.getName(),datastore,context);
            if(!issuer.hasCertificate()){
                LOGGER.warn(String.format("Skipping directory: %s", issuerDirectory.getAbsolutePath()));
                continue;
            }
            LOGGER.info("Found issuer: " + issuerDirectory.getName());
            cache.put(issuerDirectory.getName(), issuer);
        }
        this.scanned = true;
    }

    @Override
    public CertIssuer add(String name, Certificate certificate) throws IssuerRegistryException {
        if(name==null || name.isEmpty())
            throw new IllegalArgumentException("name MUST not be null nor empty");
        if(certificate==null)
            throw new IllegalArgumentException("certificate MUST not be null");
        LOGGER.info("Adding issuer: " + name);
        File issuerDirectory = new File(dataDirectory, name);
        LOGGER.info("Creating issuer data directory: " + issuerDirectory.getAbsolutePath());
        try {
            Files.createDirectories(issuerDirectory.toPath());
        } catch (IOException ex) {
            throw new IssuerRegistryException("error creating issuer data directory: "+issuerDirectory.getAbsolutePath(),
                    ex);
        }
        IssuerDatastore datastore = new DirectoryIssuerDatastore(issuerDirectory, context);
        CertIssuer issuer = new CertIssuer(name, datastore, context, certificate);
        cache.put(name, issuer);
        LOGGER.info("Issuer added successfully: " + name);
        return issuer;
    }

    @Override
    public boolean exists(String name) throws IssuerRegistryException {
        if(name==null || name.isEmpty())
            throw new IllegalArgumentException("name MUST not be null");
        this.scanIfNotYet();
        return(cache.containsKey(name));
    }

    @Override
    public Optional<CertIssuer> getByName(String name) throws IssuerRegistryException {
        if(name==null || name.isEmpty())
            throw new IllegalArgumentException("name MUST not be null");
        this.scanIfNotYet();
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public void delete(CertIssuer issuer) throws IssuerRegistryException {
        if(issuer==null)
            throw new IllegalArgumentException("issuer MUST not be null");
        if(!issuer.isDisabled())
            issuer.disable();
        this.cache.remove(issuer.getId());
    }

}
