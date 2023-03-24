package com.github.f466162.inboxmove.integration;

import com.github.f466162.inboxmove.Configuration;
import com.github.f466162.inboxmove.Constants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class Initialization implements ApplicationListener<ContextRefreshedEvent> {
    private final Configuration config;
    private final IntegrationFlowContext flowContext;

    @Autowired
    public Initialization(Configuration config, IntegrationFlowContext flowContext) {
        this.config = config;
        this.flowContext = flowContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, IntegrationFlow> inFlows = new HashMap<>(config.getInbound().size());
        Map<String, IntegrationFlow> outFlows = new HashMap<>(config.getOutbound().size());
        Map<String, Configuration.Outbound> outConfig = new HashMap<>(config.getOutbound().size());
        Set<String> outUsage = new HashSet<>(config.getOutbound().size());

        config.getOutbound().forEach(o -> {
            outConfig.put(o.getId(), o);
            outFlows.put(o.getId(), Flows.createOutboundFlow(o));
        });

        config.getInbound().forEach(i -> {
            Configuration.Outbound outboundConfig = outConfig.get(i.getOutboundReference());

            if (outboundConfig == null) {
                MDC.put(Constants.INBOUND_ID, i.getId());
                MDC.put(Constants.OUTBOUND_REFERENCE, i.getOutboundReference());
                String message = "Referenced outbound config does not exist";
                log.error(message);
                throw new RuntimeException(message);
            }

            inFlows.put(i.getId(), Flows.createInboundFlow(i, outboundConfig));
            outUsage.add(i.getOutboundReference());
        });

        outUsage.forEach(reference -> {
            MDC.put(Constants.OUTBOUND_FLOW, reference);
            log.info("Starting outbound flow");

            registerFlow(Flows.getFlowName(Flows.Direction.OUT, reference), outFlows.get(reference));

            outFlows.remove(reference);
            MDC.remove(Constants.OUTBOUND_FLOW);
        });

        inFlows.forEach((reference, integrationFlow) -> {
            MDC.put(Constants.INBOUND_FLOW, reference);
            log.info("Starting inbound flow");

            registerFlow(Flows.getFlowName(Flows.Direction.IN, reference), inFlows.get(reference));

            MDC.remove(Constants.INBOUND_FLOW);
        });

        if (!outFlows.isEmpty()) {
            MDC.put(Constants.UNUSED_OUTBOUND_FLOWS, outFlows.keySet().toString());
            log.warn("The configuration contains unused outbound flows. They are not started.");
            MDC.remove(Constants.UNUSED_OUTBOUND_FLOWS);
        }
    }

    private void registerFlow(String flowId, IntegrationFlow flow) {
        IntegrationFlowContext.IntegrationFlowRegistration registration = flowContext.getRegistrationById(flowId);

        if (registration != null) {
            log.info("Destroying registration of existing flow");
            registration.destroy();
        }

        flowContext.registration(flow).id(flowId).register();
    }
}
