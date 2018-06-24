package ~~ROOT_PACKAGE~~

import io.prometheus.client.hotspot.DefaultExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
class Application {

    static void main(String[] args) {
        DefaultExports.initialize()
        SpringApplication.run(Application, args)
    }
}
