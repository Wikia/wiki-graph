package com.wikia.service;/**
 * Author: Artur Dwornik
 * Date: 13.04.13
 * Time: 11:05
 */

import com.google.common.base.Strings;
import com.wikia.reader.text.classifiers.CompositeClassifier;
import com.wikia.reader.text.data.InstanceSource;
import com.wikia.service.model.ClassificationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

@Path("/classifications")
public class ClassificationResource {
    private static Logger logger = LoggerFactory.getLogger(ClassificationResource.class);
    private static CompositeClassifier classifierManager;
    static {
        setClassifierManager(new CompositeClassifier());
    }

    public static CompositeClassifier getClassifierManager() {
        return classifierManager;
    }

    public static void setClassifierManager(CompositeClassifier classifierManager) {
        ClassificationResource.classifierManager = classifierManager;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{wikiName : .+}/{page}")
    public ClassificationViewModel getClassifications(@PathParam("wikiName") String wikiName,@PathParam("page")  String page) {
        logger.info(String.format("getClassifications(\"%s\",\"%s\")", wikiName, page));
        if( Strings.isNullOrEmpty(wikiName) || Strings.isNullOrEmpty(page) || page.startsWith("Special:") ) {
            return null;
        }
        try {
            // TODO: move this logic out as a strategy
            URL url;
            if( !wikiName.contains(".") ) {
                url = new URL("http://" +  wikiName + ".wikia.com");
            } else {
                url = new URL(wikiName);
            }
            InstanceSource source = new InstanceSource(url, page, null);
            return ClassificationViewModel.fromClassificationResult(getClassifierManager().classify(source));
        } catch (MalformedURLException e) {
            logger.error("Cannot parse url", e);
            return null;
        }
    }

}
