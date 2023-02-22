package group.msg.at.cloud.common.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * This AutoConfiguration is required to allow applications using this library activating provided features.
 */
@AutoConfiguration
@ComponentScan(basePackageClasses = CommonObservability.class)
public class CommonObservabilityAutoConfiguration {

}
