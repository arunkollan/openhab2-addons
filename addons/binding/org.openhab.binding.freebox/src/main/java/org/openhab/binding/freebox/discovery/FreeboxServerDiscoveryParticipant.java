/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.config.FreeboxServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxServerDiscoveryParticipant} is responsible for discovering
 * the Freebox Server (bridge) thing using mDNS discovery service
 *
 * @author Laurent Garnier
 */
public class FreeboxServerDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(FreeboxServerDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_fbx-api._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return FreeboxBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service != null) {
            if ((service.getType() != null) && service.getType().equals(getServiceType())
                    && (service.getPropertyString("uid") != null)) {
                return new ThingUID(FreeboxBindingConstants.FREEBOX_BRIDGE_TYPE_SERVER,
                        service.getPropertyString("uid").replaceAll("[^A-Za-z0-9_]", "_"));
            }
        }
        return null;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("createResult ServiceInfo: {}", service);
        DiscoveryResult result = null;
        String ip = null;
        if (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) {
            ip = service.getHostAddresses()[0];
        }
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null && ip != null) {
            logger.info("Created a DiscoveryResult for Freebox Server {} on IP {}", thingUID, ip);
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(FreeboxServerConfiguration.IP_ADDRESS, ip);
            if (service.getPropertyString("api_base_url") != null) {
                properties.put(FreeboxServerConfiguration.API_BASE_URL, service.getPropertyString("api_base_url"));
            }
            if (service.getPropertyString("api_version") != null) {
                properties.put(FreeboxServerConfiguration.API_VERSION, service.getPropertyString("api_version"));
            }
            result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(service.getName())
                    .build();
        }
        return result;
    }

}
