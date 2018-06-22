package ~~ROOT_PACKAGE~~.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provide health endpoints for Kubernetes to determine if we are still alive
 * and ready to serve requests.
 *
 * @see https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/
 * https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
 */
@RestController
@Component
@RequestMapping(
        value = "/healthz",
        produces = "application/vnd.api+json")
public class HealthzController {

    /**
     * Are we functional? Or should our scheduler kill us and make another.
     * <p>
     * Note that this is a different question than "Are we ready to process messages?"
     * and should only check for hung state within our service.
     * <p>
     * Every service may have a slightly different method for determining if
     * it is "hung". Choose one that is light-weight.
     * <p>
     * Examples:
     * - Pump a synthetic message through your service, checking for success
     * excluding upstream partner liveness or readiness.
     * - Check all threads in the process for a hung state.
     * <p>
     * Note: there is tension between proving at least one thread is workable vs.
     * all threads are workable. Some systems will detect hung threads and
     * terminate them while others can gradually put every thread in the
     * pool into a hung state until the thread pool is exhausted.
     * <p>
     * Return 200 OK if we are functional, 503 otherwise with a json:api error
     * describing the fault.
     */
    @GetMapping(value = "/liveness")
    public void getLiveness() {
    }

    /**
     * Are we ready to serve requests?
     * <p>
     * Check that we have completed initialization and can connect to all upstream
     * components using the upstream component's ping endpoint or similar light-weight
     * read operation.
     * <p>
     * Return 200 OK if we are functional, 503 otherwise with a json:api error
     * describing the fault.
     */
    @GetMapping(value = "/readiness")
    public void getReadiness() {
    }

    /**
     * Can someone connect to us?
     * <p>
     * Light weight connectivity test for other service's readiness probes.
     * <p>
     * Return 200 OK if we got this far, framework will fail or not respond
     * otherwise
     */
    @GetMapping(value = "/ping")
    public void getPing() {
    }

}
