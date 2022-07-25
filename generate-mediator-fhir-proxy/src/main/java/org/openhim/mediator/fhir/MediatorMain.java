/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator.fhir;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openhim.mediator.engine.*;
import scala.util.parsing.combinator.testing.Str;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class MediatorMain {

    private static RoutingTable buildRoutingTable() throws RoutingTable.RouteAlreadyMappedException {
        RoutingTable routingTable = new RoutingTable();
        routingTable.addRegexRoute(".*", FhirProxyHandler.class);
        return routingTable;
    }

    private static StartupActorsConfig buildStartupActorsConfig() {
        StartupActorsConfig startupActors = new StartupActorsConfig();
        startupActors.addActor("fhir-context", FhirContextActor.class);
        return startupActors;
    }

    private static MediatorConfig loadConfig(String configPath) throws IOException, RoutingTable.RouteAlreadyMappedException {
        MediatorConfig config = new MediatorConfig();

        if (configPath!=null) {
            Properties props = new Properties();
            File conf = new File(configPath);
            InputStream in = FileUtils.openInputStream(conf);
            props.load(in);
            IOUtils.closeQuietly(in);

            config.setProperties(props);
        } else {
            config.setProperties("mediator.properties");
        }

        config.setName(config.getProperty("mediator.name"));
        config.setServerHost(config.getProperty("mediator.host"));
        config.setServerPort( Integer.parseInt(config.getProperty("mediator.port")) );
        config.setRootTimeout(Integer.parseInt(config.getProperty("mediator.timeout")));


        config.setCoreHost(config.getProperty("core.host"));
        config.setCoreAPIUsername(config.getProperty("core.api.user"));
        config.setCoreAPIPassword(config.getProperty("core.api.password"));
        config.setCoreAPIScheme(config.getProperty("core.api.scheme"));
        if (config.getProperty("core.api.port") != null) {
            config.setCoreAPIPort(Integer.parseInt(config.getProperty("core.api.port")));
        }

        config.setRoutingTable(buildRoutingTable());
        config.setStartupActors(buildStartupActorsConfig());

        InputStream regInfo = MediatorMain.class.getClassLoader().getResourceAsStream("mediator-registration-info.json");
        RegistrationConfig regConfig = new RegistrationConfig(regInfo);
        config.setRegistrationConfig(regConfig);

        // Override registration config from environment
        for (Map.Entry<String, Object> entry : config.getDynamicConfig().entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String environmentKey = entry.getKey().toUpperCase().replace('-', '_');
            String environmentValue = System.getenv(environmentKey);
            if (environmentValue != null) {
                config.getDynamicConfig().put(entry.getKey(), environmentValue);
            }
        }

        if (config.getProperty("mediator.heartbeats")!=null && "true".equalsIgnoreCase(config.getProperty("mediator.heartbeats"))) {
            config.setHeartbeatsEnabled(true);
        }

        return config;
    }

    public static void postRequest() throws IOException {

        URL url = new URL("http://localhost:5001/fhir");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJyZWdpc3RlciIsInBlcmZvcm1hbmNlIiwiY2VydGlmeSIsImRlbW8iXSwiaWF0IjoxNjU4NzI1MjQwLCJleHAiOjE2NTkzMzAwNDAsImF1ZCI6WyJvcGVuY3J2czphdXRoLXVzZXIiLCJvcGVuY3J2czp1c2VyLW1nbnQtdXNlciIsIm9wZW5jcnZzOmhlYXJ0aC11c2VyIiwib3BlbmNydnM6Z2F0ZXdheS11c2VyIiwib3BlbmNydnM6bm90aWZpY2F0aW9uLXVzZXIiLCJvcGVuY3J2czp3b3JrZmxvdy11c2VyIiwib3BlbmNydnM6c2VhcmNoLXVzZXIiLCJvcGVuY3J2czptZXRyaWNzLXVzZXIiLCJvcGVuY3J2czpjb3VudHJ5Y29uZmlnLXVzZXIiLCJvcGVuY3J2czp3ZWJob29rcy11c2VyIiwib3BlbmNydnM6Y29uZmlnLXVzZXIiXSwiaXNzIjoib3BlbmNydnM6YXV0aC1zZXJ2aWNlIiwic3ViIjoiNjJiOTljZDk4ZjExM2E3MDBjYzE5YTFhIn0.uTMpyTLhyt7ucyhkIG-dqEBNvSZQe04AcxNVlva5DJSN5JGyUBQzeUQcMVZmbpQf-c-Y4QBtASChCuILh_UI0YggNOMdooTJOtJdzzY4nv-PxFL8UNa5hrUjYUPBeblGY8_3NBsBSC4oHaHF0z8I78lpYrl_ILJtQnq51bw6Ji14kcbvbpx7M2ugmTD4-H-yAiRcMBPaZvd3AVsAuELiTtvDhowNI2XWawGzd1xBuwRFqsWa3234MBLp31dGhpqrSF4W6Ir2lb7Mw4gOcqstXliywbyjlqdJfUD0RNa79h-IEamwEkGkNzPXcsj9JL11p1mE2tECY1YdnouHMspQvw");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        String data = "{\"resourceType\":\"Bundle\",\"type\":\"document\",\"entry\":[{\"fullUrl\":\"http://localhost:3447/fhir/Task/18cff33f-1a05-4bee-82e2-cb2eb874ef06/_history/8a1db5bb-7e23-4967-a26f-4c5449fcedf7\",\"resource\":{\"resourceType\":\"Task\",\"status\":\"requested\",\"code\":{\"coding\":[{\"system\":\"http://opencrvs.org/specs/types\",\"code\":\"BIRTH\"}]},\"focus\":{\"reference\":\"Composition/619ee636-a5bc-4f35-b7f3-b0dc887f4a1e\"},\"id\":\"18cff33f-1a05-4bee-82e2-cb2eb874ef06\",\"identifier\":[{\"system\":\"http://opencrvs.org/specs/id/draft-id\",\"value\":\"619ee636-a5bc-4f35-b7f3-b0dc887f4a1e\"},{\"system\":\"http://opencrvs.org/specs/id/birth-tracking-id\",\"value\":\"BORSWWY\"},{\"system\":\"http://opencrvs.org/specs/id/birth-registration-number\",\"value\":\"2022BORSWWY\"}],\"extension\":[{\"url\":\"http://opencrvs.org/specs/extension/contact-person\",\"valueString\":\"MOTHER\"},{\"url\":\"http://opencrvs.org/specs/extension/contact-person-phone-number\",\"valueString\":\"+260754684646\"},{\"url\":\"http://opencrvs.org/specs/extension/timeLoggedMS\",\"valueInteger\":13632},{\"url\":\"http://opencrvs.org/specs/extension/regLastLocation\",\"valueReference\":{\"reference\":\"Location/b09122df-81f8-41a0-b5c6-68cba4145cab\"}},{\"url\":\"http://opencrvs.org/specs/extension/regLastOffice\",\"valueString\":\"Ibombo District Office\",\"valueReference\":{\"reference\":\"Location/c9c4d6e9-981c-4646-98fe-4014fddebd5e\"}},{\"url\":\"http://opencrvs.org/specs/extension/regLastUser\",\"valueReference\":{\"reference\":\"Practitioner/be8462b5-36a2-4f21-8d34-b4b679f7d3b3\"}},{\"url\":\"http://opencrvs.org/specs/extension/configuration\",\"valueReference\":{\"reference\":\"IN_CONFIGURATION\"}},{\"url\":\"http://opencrvs.org/specs/extension/regAssigned\",\"valueString\":\"REGISTERED\"},{\"url\":\"http://opencrvs.org/specs/extension/regDownloaded\",\"valueString\":\"REGISTERED\"}],\"lastModified\":\"2022-07-07T16:15:08.127Z\",\"businessStatus\":{\"coding\":[{\"system\":\"http://opencrvs.org/specs/reg-status\",\"code\":\"REGISTERED\"}]},\"meta\":{\"lastUpdated\":\"2022-07-07T16:15:08.633+00:00\",\"versionId\":\"8a1db5bb-7e23-4967-a26f-4c5449fcedf7\"}},\"request\":{\"method\":\"PUT\",\"url\":\"Task/18cff33f-1a05-4bee-82e2-cb2eb874ef06\"}}],\"signature\":{\"type\":[{\"code\":\"downloaded\"}],\"when\":\"Thu Jul 07 2022 21:45:36 GMT+0530 (India Standard Time)\"}}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = connection.getOutputStream();
        stream.write(out);

        System.out.println("Response code" + connection.getResponseCode());

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            System.out.println("" + sb);
        } else {
            System.out.println("Response message" + connection.getResponseMessage());
        }
    }

    public static void main(String[] args) throws Exception {

        //setup actor system
        final ActorSystem system = ActorSystem.create("mediator");
        //setup logger for main
        final LoggingAdapter log = Logging.getLogger(system, "main");

        //setup actors
        log.info("Initializing mediator actors...");

        String configPath = null;
        if (args.length==2 && args[0].equals("--conf")) {
            configPath = args[1];
            log.info("Loading mediator configuration from '" + configPath + "'...");
        } else {
            log.info("No configuration specified. Using default properties...");
        }

        MediatorConfig config = loadConfig(configPath);
        final MediatorServer server = new MediatorServer(system, config);

        //setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Shutting down mediator");
                server.stop();
                system.shutdown();
            }
        });

        log.info("Starting mediator server...");
        server.start();

        log.info(String.format("%s listening on %s:%s", config.getName(), config.getServerHost(), config.getServerPort()));

        postRequest();

        Thread.currentThread().join();
    }
}
