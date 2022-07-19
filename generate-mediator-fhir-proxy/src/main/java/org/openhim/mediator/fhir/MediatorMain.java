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
        URL url = new URL("http://localhost:7070/graphql");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJyZWdpc3RlciIsInBlcmZvcm1hbmNlIiwiY2VydGlmeSIsImRlbW8iXSwiaWF0IjoxNjU4MjMzNDQ3LCJleHAiOjE2NTg4MzgyNDcsImF1ZCI6WyJvcGVuY3J2czphdXRoLXVzZXIiLCJvcGVuY3J2czp1c2VyLW1nbnQtdXNlciIsIm9wZW5jcnZzOmhlYXJ0aC11c2VyIiwib3BlbmNydnM6Z2F0ZXdheS11c2VyIiwib3BlbmNydnM6bm90aWZpY2F0aW9uLXVzZXIiLCJvcGVuY3J2czp3b3JrZmxvdy11c2VyIiwib3BlbmNydnM6c2VhcmNoLXVzZXIiLCJvcGVuY3J2czptZXRyaWNzLXVzZXIiLCJvcGVuY3J2czpjb3VudHJ5Y29uZmlnLXVzZXIiLCJvcGVuY3J2czp3ZWJob29rcy11c2VyIiwib3BlbmNydnM6Y29uZmlnLXVzZXIiXSwiaXNzIjoib3BlbmNydnM6YXV0aC1zZXJ2aWNlIiwic3ViIjoiNjJiOTljZDk4ZjExM2E3MDBjYzE5YTFhIn0.aDY4VoePBjkgg-RgUEpnhbENV2MVh2PrIqrfRDepI1aPjrdBsCHAer3fmH2P1nKl7gJhSYB_vbPNAljF7lpOMQh2O6cP1djXZBXLp0VEtICdVxxqcGzJ9Cds3ev8UtvUhEhYpG7DBcSX_J4gHgj8W4WLeW-W0ovD51-dzsfzTECWjw9tO60gFfI22kje0-JQB48pi2QZB1gSaK6qOn-MJHlaVlTSuMj83endojYrpkWsWlBn4Ov8os_C89JUZa1zc-9D0ll6mk-VEJYAyS-z96DbYbmsRE9xHKYODnDlmM_HVR9pL_zPSJlZ5O0n4o4jTddv-SAxLnEtg9HFB_9rwg");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        String data = "{\"operationName\":\"searchEvents\",\"variables\":{\"locationIds\":null,\"sort\":\"DESC\",\"trackingId\":\"\",\"registrationNumber\":\"\",\"contactNumber\":\"\",\"name\":\"patel\"},\"query\":\"query searchEvents($sort: String, $trackingId: String, $contactNumber: String, $registrationNumber: String, $name: String, $locationIds: [String!]) {\\n  searchEvents(\\n    sort: $sort\\n    trackingId: $trackingId\\n    registrationNumber: $registrationNumber\\n    name: $name\\n    contactNumber: $contactNumber\\n    locationIds: $locationIds\\n  ) {\\n    totalItems\\n    results {\\n      id\\n      type\\n      registration {\\n        status\\n        contactNumber\\n        trackingId\\n        registrationNumber\\n        registeredLocationId\\n        duplicates\\n        assignment {\\n          userId\\n          firstName\\n          lastName\\n          officeName\\n          __typename\\n        }\\n        createdAt\\n        modifiedAt\\n        __typename\\n      }\\n      ... on BirthEventSearchSet {\\n        dateOfBirth\\n        childName {\\n          firstNames\\n          familyName\\n          use\\n          __typename\\n        }\\n        __typename\\n      }\\n      ... on DeathEventSearchSet {\\n        dateOfDeath\\n        deceasedName {\\n          firstNames\\n          familyName\\n          use\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

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
